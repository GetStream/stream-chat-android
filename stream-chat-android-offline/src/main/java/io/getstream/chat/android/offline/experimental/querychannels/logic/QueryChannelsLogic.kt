package io.getstream.chat.android.offline.experimental.querychannels.logic

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.await
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.experimental.plugin.listeners.QueryChannelsListener
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.internal.toggle.ToggleService
import io.getstream.chat.android.client.utils.map
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.channel.state.ChannelState
import io.getstream.chat.android.offline.experimental.extensions.state
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.extensions.applyPagination
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.model.ChannelConfig
import io.getstream.chat.android.offline.querychannels.ChannelFilterRequest
import io.getstream.chat.android.offline.querychannels.EventHandlingResult
import io.getstream.chat.android.offline.request.AnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest
import kotlinx.coroutines.flow.MutableStateFlow

@ExperimentalStreamChatApi
internal class QueryChannelsLogic(
    private val mutableState: QueryChannelsMutableState,
    private val chatDomainImpl: ChatDomainImpl,
    private val client: ChatClient,
) : QueryChannelsListener {

    private val logger = ChatLogger.get("QueryChannelsLogic")

    internal val channelFilter: suspend (cid: String, FilterObject) -> Boolean = { cid, filter ->
        ChannelFilterRequest.filter(client, cid, filter)
            .map { channels -> channels.any { it.cid == cid } }
            .let { filteringResult -> filteringResult.isSuccess && filteringResult.data() }
    }

    /**
     * Conditional check before queryChannels API call is invoked.
     *
     * Returns [Result.success] if there is no ongoing request otherwise [Result.error] to terminate the request.
     */
    override suspend fun onQueryChannelsPrecondition(request: QueryChannelsRequest): Result<Unit> {
        val loader = loadingForCurrentRequest()
        return if (loader.value) {
            logger.logI("Another request to load channels is in progress. Ignoring this request.")
            Result.error(ChatError("Another request to load messages is in progress. Ignoring this request."))
        } else {
            Result.success(Unit)
        }
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
        val queryChannelsSpec = mutableState.queryChannelsSpec
        val query =
            chatDomainImpl.repos.selectBy(queryChannelsSpec.filter, queryChannelsSpec.querySort) ?: return emptyList()

        return chatDomainImpl.repos.selectChannels(query.cids.toList(), pagination)
            .applyPagination(pagination)
            .also { logger.logI("found ${it.size} channels in offline storage") }
            .also { addChannels(it) }
    }

    internal suspend fun addChannel(channel: Channel) {
        addChannels(listOf(channel))
        chatDomainImpl.channel(channel).updateDataFromChannel(channel)
    }

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

    private suspend fun onOnlineQueryResult(result: Result<List<Channel>>, request: QueryChannelsRequest) {
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
     * Updates the state based on the channels collection we received from the API.
     *
     * @param channels The list of channels to update.
     * @param isFirstPage If it's the first page we set/replace the list of results. if it's not the first page we add to the list.
     */
    internal suspend fun updateOnlineChannels(
        channels: List<Channel>,
        isFirstPage: Boolean,
    ) {
        if (isFirstPage) {
            (mutableState._channels.value - channels.map { it.cid }).values
                .map(Channel::cid)
                .filterNot { cid -> channelFilter(cid, mutableState.filter) }
                .let { removeChannels(it) }
        }
        mutableState.channelsOffset.value += channels.size
        channels.forEach { chatDomainImpl.channel(it).updateDataFromChannel(it) }
        addChannels(channels)
    }

    internal suspend fun removeChannel(cid: String) = removeChannels(listOf(cid))

    private suspend fun removeChannels(cidList: List<String>) {
        mutableState.queryChannelsSpec.cids = mutableState.queryChannelsSpec.cids - cidList
        chatDomainImpl.repos.insertQueryChannels(mutableState.queryChannelsSpec)
        mutableState._channels.value = mutableState._channels.value - cidList
    }

    /**
     * Handles events received from the socket.
     *
     * @see [handleEvent]
     */
    internal suspend fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    /**
     * Handles event received from the socket.
     * Responsible for synchronizing [QueryChannelsMutableState].
     */
    internal suspend fun handleEvent(event: ChatEvent) {
        // update the info for that channel from the channel repo
        logger.logI("Received channel event $event")

        val cachedChannel = if (event is CidEvent) {
            chatDomainImpl.getCachedChannel(event.cid)
        } else null

        val handlingResult = mutableState.eventHandler.handleChatEvent(event, mutableState.filter, cachedChannel)
        when (handlingResult) {
            is EventHandlingResult.Add -> addChannel(handlingResult.channel)
            is EventHandlingResult.Remove -> removeChannel(handlingResult.cid)
            is EventHandlingResult.Skip -> Unit
        }

        if (event is MarkAllReadEvent) {
            refreshAllChannels()
        }

        if (event is CidEvent) {
            // skip events that are typically not impacting the query channels overview
            if (event is UserStartWatchingEvent || event is UserStopWatchingEvent) {
                return
            }
            refreshChannel(event.cid)
        }

        if (event is UserPresenceChangedEvent) {
            refreshMembersStateForUser(event.user)
        }
    }

    /**
     * Refreshes multiple channels in this query.
     * Note that it retrieves the data from the current [ChannelState] object.
     *
     * @param cidList The channels to refresh.
     */
    // TODO: Make private after removing QueryChannelsController
    internal fun refreshChannels(cidList: Collection<String>) {
        mutableState._channels.value += mutableState.queryChannelsSpec.cids
            .intersect(cidList)
            .associateWith { cid ->
                val (channelType, channelId) = cid.cidToTypeAndId()
                if (ToggleService.isEnabled(ToggleService.TOGGLE_KEY_OFFLINE)) {
                    client.state.channel(channelType, channelId).toChannel()
                } else {
                    chatDomainImpl.channel(channelType, channelId).toChannel()
                }
            }
    }

    /**
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    // TODO: Make private after removing QueryChannelsController
    internal fun refreshAllChannels() {
        refreshChannels(mutableState.queryChannelsSpec.cids)
    }

    /**
     * Refreshes a single channel.
     * @see [refreshChannels]
     *
     * @param cid The channel's cid to update.
     *
     */
    internal fun refreshChannel(cid: String) {
        refreshChannels(listOf(cid))
    }

    /**
     * Refreshes member state in all channels from this query.
     *
     * @param newUser The user to refresh.
     */
    // TODO: Make private after removing QueryChannelsController
    internal fun refreshMembersStateForUser(newUser: User) {
        val userId = newUser.id

        val affectedChannels = mutableState._channels.value
            .filter { (_, channel) -> channel.users().any { it.id == userId } }
            .mapValues { (_, channel) ->
                channel.copy(
                    members = channel.members.map { member ->
                        member.copy(user = member.user.takeUnless { it.id == userId } ?: newUser)
                    }
                )
            }

        mutableState._channels.value += affectedChannels
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
