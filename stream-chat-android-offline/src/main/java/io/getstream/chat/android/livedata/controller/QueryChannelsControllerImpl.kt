package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.NotificationAddedToChannelEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.QueryChannelsControllerImpl as NewQueryChannelsControllerImpl

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val CHANNEL_LIMIT = 30

internal class QueryChannelsControllerImpl(
    private val delegate: NewQueryChannelsControllerImpl,
) : QueryChannelsController {

    override val filter: FilterObject
        get() = delegate.filter

    override val sort: QuerySort<Channel>
        get() = delegate.sort

    override var newChannelEventFilter: (Channel, FilterObject) -> Boolean
        get() = delegate.newChannelEventFilter
        set(value) {
            delegate.newChannelEventFilter = value
        }

    override val recoveryNeeded: Boolean
        get() = delegate.recoveryNeeded

    val queryChannelsSpec: QueryChannelsSpec
        get() = delegate.queryChannelsSpec

    override val endOfChannels: LiveData<Boolean> = delegate.endOfChannels.asLiveData()

    // Keep the channel list locally sorted
    override var channels: LiveData<List<Channel>> = delegate.channels.asLiveData()

    override val loading: LiveData<Boolean> = delegate.loading.asLiveData()

    override val loadingMore: LiveData<Boolean> = delegate.loadingMore.asLiveData()

    override val channelsState = delegate.channelsState.asLiveData()

    fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): QueryChannelsPaginationRequest = delegate.loadMoreRequest(channelLimit, messageLimit, memberLimit)

    /**
     * Members of a channel receive the
     *
     * @see NotificationAddedToChannelEvent
     *
     * We allow you to specify a newChannelEventFilter callback to determine if this query matches the given channel
     */
    internal fun addChannelIfFilterMatches(channel: Channel) = delegate.addChannelIfFilterMatches(channel)

    internal suspend fun handleEvents(events: List<ChatEvent>) = delegate.handleEvents(events)

    internal suspend fun handleEvent(event: ChatEvent) = delegate.handleEvent(event)

    suspend fun loadMore(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
    ): Result<List<Channel>> = delegate.loadMore(channelLimit, messageLimit)

    suspend fun query(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): Result<List<Channel>> = delegate.query(channelLimit, messageLimit, memberLimit)

    suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? =
        delegate.runQueryOffline(pagination)

    suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> =
        delegate.runQueryOnline(pagination)

    suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> =
        delegate.runQuery(pagination)

    /**
     * Updates the state on the channelController based on the channel object we received
     * This is used for both the online and offline query flow
     *
     * @param channels the list of channels to update
     * @param isFirstPage if it's the first page we set/replace the list of results. if it's not the first page we add to the list
     *
     */
    internal fun updateChannelsAndQueryResults(channels: List<Channel>?, isFirstPage: Boolean) =
        delegate.updateChannelsAndQueryResults(channels, isFirstPage)

    internal suspend fun removeChannel(cid: String) = delegate.removeChannel(cid)

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
    fun refreshChannel(cId: String) = delegate.refreshChannel(cId)
}
