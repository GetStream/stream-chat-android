package io.getstream.chat.android.offline.experimental.querychannels.logic

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.extensions.applyPagination
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.isFirstPage
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalStreamChatApi
internal class QueryChannelsLogic(
    private val mutableState: QueryChannelsMutableState,
    private val chatDomainImpl: ChatDomainImpl,
) : QueryChannelsListener {

    private val logger = ChatLogger.get("QueryChannelsLogic")

    var newChannelEventFilter: suspend (Channel, FilterObject) -> Boolean = { channel, filterObject ->
        chatDomainImpl.client.queryChannelsInternal(
            QueryChannelsRequest(
                filter = Filters.and(
                    filterObject,
                    Filters.eq("cid", channel.cid)
                ),
                offset = 0,
                limit = 1,
                messageLimit = 0,
                memberLimit = 0,
            )
        ).await()
            .map { channels -> channels.any { it.cid == channel.cid } }
            .let { it.isSuccess && it.data() }
    }

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        mutableState._currentRequest.value = request
        queryOffline(request.toPagination())
    }

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest): Result<List<Channel>> {
        val loading = if (mutableState.channels.value.isEmpty()) mutableState._loading else mutableState._loadingMore

        if (loading.value) {
            logger.logI("Another query channels request is in progress. Ignoring this request.")
            return Result(ChatError("Another query channels request is in progress. Ignoring this request."))
        }

        loading.value = true

        return fetchChannelsFromCache(pagination)
            .also { loading.value = it.isEmpty() }
            .let { Result.success(it) }
    }

    private suspend fun fetchChannelsFromCache(pagination: AnyChannelPaginationRequest): List<Channel> {
        val query = chatDomainImpl.repos.selectById(mutableState.queryChannelsSpec.id) ?: return emptyList()

        return chatDomainImpl.repos.selectChannels(query.cids.toList(), pagination)
            .applyPagination(pagination)
            .also { logger.logI("found ${it.size} channels in offline storage") }
            .also { addChannels(it) }
    }

    internal suspend fun addChannel(channel: Channel) = addChannels(listOf(channel))

    private suspend fun addChannels(channels: List<Channel>) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        chatDomainImpl.repos.insertQueryChannels(mutableState.queryChannelsSpec)
        mutableState._channels.value = mutableState._channels.value + channels.map { it.cid to it }
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        onOnlineQueryResult(result, request)
        if (result.isSuccess) {
            updateOnlineChannels(result.data(), request.isFirstPage)
        }
        val loading = loadingForCurrentRequest()
        loading.value = false
    }

    internal suspend fun runQueryOnline(request: QueryChannelsRequest): Result<List<Channel>> {
        return chatDomainImpl.client.queryChannelsInternal(request).await()
            .also { onQueryChannelsResult(it, request) }
    }

    internal suspend fun onOnlineQueryResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        if (result.isSuccess) {
            mutableState.recoveryNeeded.value = false

            // store the results in the database
            val channelsResponse = result.data().toSet()
            if (channelsResponse.size < request.limit) {
                mutableState._endOfChannels.value = true
            }
            // first things first, store the configs
            val channelConfigs = channelsResponse.map { ChannelConfig(it.type, it.config) }
            chatDomainImpl.repos.insertChannelConfigs(channelConfigs)
            logger.logI("api call returned ${channelsResponse.size} channels")
            chatDomainImpl.storeStateForChannels(channelsResponse)
        } else {
            logger.logI("Query with filter ${request.filter} failed, marking it as recovery needed")
            mutableState.recoveryNeeded.value = true
            chatDomainImpl.addError(result.error())
        }
    }

    internal fun loadingForCurrentRequest(): MutableStateFlow<Boolean> {
        return mutableState._currentRequest.value?.isFirstPage?.let { isFirstPage ->
            if (isFirstPage) mutableState._loading else mutableState._loadingMore
        } ?: mutableState._loading
    }

    /**
     * Updates the state on the channelController based on the channel object we received from the API.
     *
     * @param channels The list of channels to update.
     * @param isFirstPage If it's the first page we set/replace the list of results. if it's not the first page we add to the list.
     *
     */
    internal suspend fun updateOnlineChannels(
        channels: List<Channel>,
        isFirstPage: Boolean,
    ) {
        if (isFirstPage) {
            (mutableState._channels.value - channels.map { it.cid }).values
                .filterNot { newChannelEventFilter(it, mutableState.filter) }
                .map { it.cid }
                .let { removeChannels(it) }
        }
        mutableState.channelsOffset.value += channels.size
        channels.forEach { chatDomainImpl.channel(it).updateDataFromChannel(it) }
        addChannels(channels)
    }

    internal suspend fun removeChannel(cid: String) = removeChannels(listOf(cid))

    private suspend fun removeChannels(cids: List<String>) {
        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cids
        chatDomainImpl.repos.insertQueryChannels(mutableState.queryChannelsSpec)
        mutableState._channels.value = mutableState._channels.value - cids
    }

    private companion object {
        private fun QueryChannelsRequest.toPagination(): AnyChannelPaginationRequest =
            QueryChannelsPaginationRequest(
                sort = querySort,
                channelLimit = limit,
                channelOffset = offset,
                messageLimit = messageLimit,
                memberLimit = memberLimit
            ).toAnyChannelPaginationRequest()

        private val QueryChannelsRequest.isFirstPage: Boolean
            get() = offset == 0
    }
}
