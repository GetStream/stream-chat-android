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
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import io.getstream.chat.android.offline.querychannels.QueryChannelsController as QueryChannelsControllerStateFlow

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val CHANNEL_LIMIT = 30

internal class QueryChannelsControllerImpl(private val queryChannels: QueryChannelsControllerStateFlow) :
    QueryChannelsController {

    internal constructor(
        filter: FilterObject,
        sort: QuerySort<Channel>,
        client: ChatClient,
        domainImpl: ChatDomainImpl,
    ) : this(QueryChannelsControllerStateFlow(filter, sort, client, domainImpl.chatDomainStateFlow))

    override val filter: FilterObject
        get() = queryChannels.filter

    override val sort: QuerySort<Channel>
        get() = queryChannels.sort

    override var newChannelEventFilter: (Channel, FilterObject) -> Boolean
        get() = queryChannels.newChannelEventFilter
        set(value) {
            queryChannels.newChannelEventFilter = value
        }

    override var recoveryNeeded: Boolean
        get() = queryChannels.recoveryNeeded
        set(value) {
            queryChannels.recoveryNeeded = value
        }
    val queryChannelsSpec: QueryChannelsSpec = queryChannels.queryChannelsSpec

    override val endOfChannels: LiveData<Boolean> = queryChannels.endOfChannels.asLiveData()

    // Keep the channel list locally sorted
    override val channels: LiveData<List<Channel>>
        get() = queryChannels.channels.asLiveData()

    override val loading: LiveData<Boolean> = queryChannels.loading.asLiveData()

    override val loadingMore: LiveData<Boolean> = queryChannels.loadingMore.asLiveData()

    override val channelsState = queryChannels.channelsState.asLiveData()

    override val mutedChannelIds: LiveData<List<String>> = queryChannels.mutedChannelIds.asLiveData()

    fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): QueryChannelsPaginationRequest {
        return queryChannels.loadMoreRequest(channelLimit, messageLimit, memberLimit)
    }

    /**
     * Members of a channel receive the
     *
     * @see NotificationAddedToChannelEvent
     *
     * We allow you to specify a newChannelEventFilter callback to determine if this query matches the given channel
     */
    internal fun addChannelIfFilterMatches(channel: Channel) {
        queryChannels.addChannelIfFilterMatches(channel)
    }

    internal fun handleEvents(events: List<ChatEvent>) {
        queryChannels.handleEvents(events)
    }

    internal fun handleEvent(event: ChatEvent) {
        queryChannels.handleEvent(event)
    }

    suspend fun loadMore(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
    ): Result<List<Channel>> {
        return queryChannels.loadMore(channelLimit, messageLimit)
    }

    suspend fun query(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): Result<List<Channel>> {
        return queryChannels.query(channelLimit, messageLimit, memberLimit)
    }

    suspend fun runQueryOffline(pagination: QueryChannelsPaginationRequest): List<Channel>? {
        return queryChannels.runQueryOffline(pagination)
    }

    suspend fun runQueryOnline(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        return queryChannels.runQueryOnline(pagination)
    }

    suspend fun runQuery(pagination: QueryChannelsPaginationRequest): Result<List<Channel>> {
        return queryChannels.runQuery(pagination)
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
        return queryChannels.updateChannelsAndQueryResults(channels, isFirstPage)
    }

    internal suspend fun removeChannel(cid: String) {
        queryChannels.removeChannel(cid)
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
        queryChannels.refreshChannels(cIds)
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
        queryChannels.addToQueryResult(cIds)
    }
}
