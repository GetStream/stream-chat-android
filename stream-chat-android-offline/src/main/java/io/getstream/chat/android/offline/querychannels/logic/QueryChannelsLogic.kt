package io.getstream.chat.android.offline.querychannels.logic

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.extensions.applyPagination
import io.getstream.chat.android.offline.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.repository.RepositoryFacade
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.isFirstPage
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest
import kotlinx.coroutines.flow.MutableStateFlow

internal class QueryChannelsLogic(
    private val mutableState: QueryChannelsMutableState,
    private val repositoryFacade: RepositoryFacade,
) : QueryChannelsListener {

    private val logger = ChatLogger.get("QueryChannelsLogic")

    override suspend fun onQueryChannelsRequest(request: QueryChannelsRequest) {
        mutableState._currentRequest.value = request
        queryOffline(request.toPagination())
    }

    internal suspend fun queryOffline(pagination: AnyChannelPaginationRequest): Result<List<Channel>> {
        val loading = if (pagination.isFirstPage()) mutableState._loading else mutableState._loadingMore

        if (loading.value) {
            logger.logI("Another query channels request is in progress. Ignoring this request.")
            throw IllegalStateException("Another query channels request is in progress. Ignoring this request.")
        }

        loading.value = true

        return fetchChannelsFromCache(pagination)
            .also { loading.value = it.isEmpty() }
            .let { Result.success(it) }
    }

    internal suspend fun fetchChannelsFromCache(pagination: AnyChannelPaginationRequest): List<Channel> {
        val query = repositoryFacade.selectById(mutableState.queryChannelsSpec.id) ?: return emptyList()

        return repositoryFacade.selectChannels(query.cids.toList(), pagination)
            .applyPagination(pagination)
            .also { logger.logI("found ${it.size} channels in offline storage") }
            .also { addChannels(it) }
    }

    internal suspend fun addChannels(channels: List<Channel>) {
        mutableState.queryChannelsSpec.cids += channels.map { it.cid }
        repositoryFacade.insertQueryChannels(mutableState.queryChannelsSpec)
        mutableState._channels.value = mutableState._channels.value + channels.map { it.cid to it }
    }

    override suspend fun onQueryChannelsResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
        super.onQueryChannelsResult(result, request)
    }

    internal fun loadingForCurrentRequest(): MutableStateFlow<Boolean> {
        return mutableState._currentRequest.value?.isFirstPage?.let { isFirstPage ->
            if (isFirstPage) mutableState._loading else mutableState._loadingMore
        } ?: mutableState._loading
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
