package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import io.getstream.chat.android.offline.request.QueryChannelsPaginationRequest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import io.getstream.chat.android.offline.querychannels.QueryChannelsController as QueryChannelsControllerStateFlow
import io.getstream.chat.android.offline.querychannels.QueryChannelsController.ChannelsState as OfflineChannelState

private const val MESSAGE_LIMIT = 10
private const val MEMBER_LIMIT = 30
private const val CHANNEL_LIMIT = 30

internal class QueryChannelsControllerImpl(private val queryChannels: QueryChannelsControllerStateFlow) :
    QueryChannelsController {

    override val filter: FilterObject
        get() = queryChannels.filter

    override val sort: QuerySort<Channel>
        get() = queryChannels.sort

    override var newChannelEventFilter: (Channel, FilterObject) -> Boolean
        get() = { channel, filter -> runBlocking { queryChannels.newChannelEventFilter(channel, filter) } }
        set(filter) {
            queryChannels.newChannelEventFilter = { channel: Channel, filterObject: FilterObject ->
                withContext(DispatcherProvider.IO) { filter(channel, filterObject) }
            }
        }

    override var checkFilterOnChannelUpdatedEvent: Boolean = false
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

    override val channelsState = queryChannels.channelsState.map {
        when (it) {
            OfflineChannelState.Loading -> QueryChannelsController.ChannelsState.Loading
            OfflineChannelState.NoQueryActive -> QueryChannelsController.ChannelsState.NoQueryActive
            OfflineChannelState.OfflineNoResults -> QueryChannelsController.ChannelsState.OfflineNoResults
            is OfflineChannelState.Result -> QueryChannelsController.ChannelsState.Result(it.channels)
        }
    }.asLiveData()

    override val mutedChannelIds: LiveData<List<String>> = queryChannels.mutedChannelIds.asLiveData()

    fun loadMoreRequest(
        channelLimit: Int = CHANNEL_LIMIT,
        messageLimit: Int = MESSAGE_LIMIT,
        memberLimit: Int = MEMBER_LIMIT,
    ): QueryChannelsPaginationRequest {
        return queryChannels.loadMoreRequest(channelLimit, messageLimit, memberLimit)
    }

    internal suspend fun handleEvents(events: List<ChatEvent>) {
        queryChannels.handleEvents(events)
    }

    internal suspend fun handleEvent(event: ChatEvent) {
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
}
