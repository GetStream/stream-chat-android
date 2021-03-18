package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomainImpl
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val CHANNEL_LIMIT = 30

internal class QueryChannelsControllerImpl(
    filter: FilterObject,
    sort: QuerySort<Channel>,
    client: ChatClient,
    domainImpl: ChatDomainImpl,
) : QueryChannelsController {
    private val controllerStateFlow = QueryChannelsControllerStateFlow(filter, sort, client, domainImpl)

    override val filter: FilterObject
        get() = controllerStateFlow.filter

    override val sort: QuerySort<Channel>
        get() = controllerStateFlow.sort

    override var newChannelEventFilter: (Channel, FilterObject) -> Boolean =
        controllerStateFlow.newChannelEventFilter

    override var recoveryNeeded: Boolean =
        controllerStateFlow.recoveryNeeded

    val queryChannelsSpec: QueryChannelsSpec = controllerStateFlow.queryChannelsSpec

    override val endOfChannels: LiveData<Boolean> = controllerStateFlow._endOfChannels.asLiveData()

    // Keep the channel list locally sorted
    override var channels: LiveData<List<Channel>> = controllerStateFlow._sortedChannels.asLiveData()

    override val loading: LiveData<Boolean> = controllerStateFlow._loading.asLiveData()

    override val loadingMore: LiveData<Boolean> = controllerStateFlow._loadingMore.asLiveData()

    override val channelsState = controllerStateFlow._channelsState.asLiveData()

    fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): QueryChannelsPaginationRequest {
        return controllerStateFlow.loadMoreRequest(channelLimit, messageLimit, memberLimit)
    }

    /**
     * Members of a channel receive the
     *
     * @see NotificationAddedToChannelEvent
     *
     * We allow you to specify a newChannelEventFilter callback to determine if this query matches the given channel
     */
    internal fun addChannelIfFilterMatches(channel: Channel) {
        controllerStateFlow.addChannelIfFilterMatches(channel)
    }

    internal fun handleEvents(events: List<ChatEvent>) {
        controllerStateFlow.handleEvents(events)
    }

    internal fun handleEvent(event: ChatEvent) {
        controllerStateFlow.handleEvent(event)
    }

    suspend fun loadMore(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
    ): Result<List<Channel>> {
        return controllerStateFlow.loadMore(channelLimit, messageLimit)
    }

    suspend fun query(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): Result<List<Channel>> {
        return controllerStateFlow.query(channelLimit, messageLimit, memberLimit)
    }

    suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        return controllerStateFlow.runQueryOffline(pagination)
    }

    suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        return controllerStateFlow.runQueryOnline(pagination)
    }

    suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        return controllerStateFlow.runQuery(pagination)
    }

    /**
     * Updates the state on the channelController based on the channel object we received
     * This is used for both the online and offline query flow
     *
     * @param channels the list of channels to update
     * @param isFirstPage if it's the first page we set/replace the list of results. if it's not the first page we add to the list
     *
     */
    internal fun updateChannelsAndQueryResults(channels: List<Channel>?, isFirstPage: Boolean) {
        return controllerStateFlow.updateChannelsAndQueryResults(channels, isFirstPage)
    }

    internal suspend fun removeChannel(cid: String) {
        controllerStateFlow.removeChannel(cid)
    }

    /**
     * refreshes a single channel
     * Note that this only refreshes channels that are already matching with the query
     * It retrieves the data from the current channelController object
     *
     * @param cId the channel to update
     *
     * If you want to add to the list of channels use the addToQueryResult method
     *
     * @see addToQueryResult
     */
    fun refreshChannel(cId: String) {
        refreshChannels(listOf(cId))
    }

    /**
     * Refreshes multiple channels on this query
     * Note that it retrieves the data from the current channelController object
     *
     * @param cIds the channels to refresh
     * @see ChannelController
     */
    private fun refreshChannels(cIds: List<String>) {
        controllerStateFlow.refreshChannels(cIds)
    }

    /**
     * Adds the list of channels to the current query.
     * Channels are sorted based on the specified QuerySort
     * Triggers a refresh of these channels based on the current state on the ChannelController
     *
     * @param cIds the list of channel ids to add to the query result
     *
     * @see QuerySort
     * @see ChannelController
     */
    private fun addToQueryResult(cIds: List<String>) {
        controllerStateFlow.addToQueryResult(cIds)
    }
}
