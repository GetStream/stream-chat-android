package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import io.getstream.chat.android.offline.querychannels.QueryChannelsController as QueryChannelsControllerStateFlow
import io.getstream.chat.android.offline.querychannels.QueryChannelsController.ChannelsState as OfflineChannelState

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
        get() = queryChannels.recoveryNeeded.value
        set(value) {
            queryChannels.recoveryNeeded.value = value
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
}
