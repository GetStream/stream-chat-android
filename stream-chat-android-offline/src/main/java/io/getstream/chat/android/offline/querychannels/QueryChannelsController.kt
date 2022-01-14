package io.getstream.chat.android.offline.querychannels

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.CidEvent
import io.getstream.chat.android.client.events.MarkAllReadEvent
import io.getstream.chat.android.client.events.UserPresenceChangedEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserStopWatchingEvent
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.ChatDomainImpl
import io.getstream.chat.android.offline.experimental.querychannels.logic.QueryChannelsLogic
import io.getstream.chat.android.offline.experimental.querychannels.state.QueryChannelsMutableState
import io.getstream.chat.android.offline.extensions.users
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.request.toAnyChannelPaginationRequest
import io.getstream.chat.android.offline.request.toQueryChannelsRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val INITIAL_CHANNEL_OFFSET = 0
private const val CHANNEL_LIMIT = 30

@OptIn(ExperimentalStreamChatApi::class)
public class QueryChannelsController internal constructor(
    private val domainImpl: ChatDomainImpl,
    private val mutableState: QueryChannelsMutableState,
    private val queryChannelsLogic: QueryChannelsLogic,
) {
    public val recoveryNeeded: MutableStateFlow<Boolean> by mutableState::recoveryNeeded

    public val filter: FilterObject by mutableState::filter
    public val sort: QuerySort<Channel> by mutableState::sort

    /**
     * Instance of [ChatEventHandler] that handles logic of event handling for this [QueryChannelsController].
     */
    public var chatEventHandler: ChatEventHandler? by mutableState::chatEventHandler

    internal val queryChannelsSpec: QueryChannelsSpec = mutableState.queryChannelsSpec

    private val _channels = mutableState._channels

    public val loading: StateFlow<Boolean> = mutableState.loading
    public val loadingMore: StateFlow<Boolean> = mutableState.loadingMore
    public val endOfChannels: StateFlow<Boolean> = mutableState.endOfChannels
    public val channels: StateFlow<List<Channel>> = mutableState.channels

    public val channelsState: StateFlow<ChannelsState> = mutableState.channelsStateData.map { state ->
        when (state) {
            io.getstream.chat.android.offline.experimental.querychannels.state.ChannelsStateData.Loading -> ChannelsState.Loading
            io.getstream.chat.android.offline.experimental.querychannels.state.ChannelsStateData.NoQueryActive -> ChannelsState.NoQueryActive
            io.getstream.chat.android.offline.experimental.querychannels.state.ChannelsStateData.OfflineNoResults -> ChannelsState.OfflineNoResults
            is io.getstream.chat.android.offline.experimental.querychannels.state.ChannelsStateData.Result -> ChannelsState.Result(
                state.channels
            )
        }
    }.stateIn(domainImpl.scope, SharingStarted.Eagerly, ChannelsState.NoQueryActive)

    private val logger = ChatLogger.get("ChatDomain QueryChannelsController")

    internal fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): QueryChannelsPaginationRequest {
        return QueryChannelsPaginationRequest(
            sort,
            mutableState.channelsOffset.value,
            channelLimit,
            messageLimit,
            memberLimit
        )
    }

    /**
     * Updates the collection of channels by some channel. If the channels passes filter it's added to collection,
     * otherwise it gets removed.
     */
    internal suspend fun updateQueryChannelCollectionByNewChannel(channel: Channel) {
        if (queryChannelsLogic.channelFilter(channel.cid, filter)) {
            addChannel(channel)
        } else {
            removeChannel(channel.cid)
        }
    }

    internal suspend fun handleEvents(events: List<ChatEvent>) {
        for (event in events) {
            handleEvent(event)
        }
    }

    /** Handles updates by WS events. Keeps synchronized data of [QueryChannelsMutableState]. */
    internal suspend fun handleEvent(event: ChatEvent) {
        // update the info for that channel from the channel repo
        logger.logI("received channel event $event")

        val cachedChannel = if (event is CidEvent) {
            domainImpl.getCachedChannel(event.cid)
        } else null

        val handlingResult = mutableState.eventHandler.handleChatEvent(event, filter, cachedChannel)
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
     * Refreshes all channels returned in this query.
     * Supports use cases like marking all channels as read.
     */
    private fun refreshAllChannels() {
        refreshChannels(queryChannelsSpec.cids)
    }

    /**
     * refreshes a single channel
     * Note that this only refreshes channels that are already matching with the query
     * It retrieves the data from the current channelController object
     *
     * @param cId The channel to update.
     *
     * If you want to add to the list of channels use the addToQueryResult method
     *
     * @see addToQueryResult
     */
    public fun refreshChannel(cId: String) {
        refreshChannels(listOf(cId))
    }

    private fun refreshMembersStateForUser(newUser: User) {
        val userId = newUser.id

        val affectedChannels = _channels.value
            .filter { (_, channel) -> channel.users().any { it.id == userId } }
            .mapValues { (_, channel) ->
                channel.copy(
                    members = channel.members.map { member ->
                        member.copy(user = member.user.takeUnless { it.id == userId } ?: newUser)
                    }
                )
            }

        _channels.value += affectedChannels
    }

    /**
     * Refreshes multiple channels on this query
     * Note that it retrieves the data from the current channelController object
     *
     * @param cIds The channels to refresh.
     * @see ChannelController
     */
    internal fun refreshChannels(cIds: Collection<String>) {
        mutableState._channels.value += queryChannelsSpec.cids
            .intersect(cIds)
            .map { it to domainImpl.channel(it).toChannel() }
            .toMap()
    }

    private suspend fun addChannel(channel: Channel) = queryChannelsLogic.addChannel(channel)

    private suspend fun removeChannel(cid: String) = queryChannelsLogic.removeChannel(cid)

    internal suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        val request = pagination.toQueryChannelsRequest(filter, domainImpl.userPresence)
        mutableState._currentRequest.value = request
        val offlineResult = queryChannelsLogic.queryOffline(pagination.toAnyChannelPaginationRequest())

        if (offlineResult.isError) {
            return offlineResult
        }
        val onlineResult = runQueryOnline(request)

        return onlineResult.takeIf(Result<List<Channel>>::isSuccess) ?: offlineResult
    }

    public suspend fun query(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): Result<List<Channel>> {
        mutableState.channelsOffset.value = INITIAL_CHANNEL_OFFSET
        return runQuery(
            QueryChannelsPaginationRequest(
                sort,
                INITIAL_CHANNEL_OFFSET,
                channelLimit,
                messageLimit,
                memberLimit,
            )
        )
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
    ) = queryChannelsLogic.updateOnlineChannels(channels, isFirstPage)

    internal suspend fun runQueryOnline(request: QueryChannelsPaginationRequest): Result<List<Channel>> =
        runQueryOnline(request.toQueryChannelsRequest(filter, domainImpl.userPresence))

    private suspend fun runQueryOnline(request: QueryChannelsRequest) = queryChannelsLogic.runQueryOnline(request)

    public sealed class ChannelsState {
        /** The QueryChannelsController is initialized but no query is currently running.
         * If you know that a query will be started you typically want to display a loading icon.
         */
        public object NoQueryActive : ChannelsState()

        /** Indicates we are loading the first page of results.
         * We are in this state if QueryChannelsController.loading is true
         * For seeing if we're loading more results have a look at QueryChannelsController.loadingMore
         *
         * @see QueryChannelsController.loadingMore
         * @see QueryChannelsController.loading
         */
        public object Loading : ChannelsState()

        /** If we are offline and don't have channels stored in offline storage, typically displayed as an error condition. */
        public object OfflineNoResults : ChannelsState()

        /** The list of channels, loaded either from offline storage or an API call.
         * Observe chatDomain.online to know if results are currently up to date
         * @see ChatDomainImpl.connectionState
         */
        public data class Result(val channels: List<Channel>) : ChannelsState()
    }
}
