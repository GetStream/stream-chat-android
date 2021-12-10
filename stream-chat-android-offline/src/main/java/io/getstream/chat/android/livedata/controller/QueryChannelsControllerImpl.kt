package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.offline.querychannels.ChatEventHandler
import io.getstream.chat.android.offline.querychannels.QueryChannelsSpec
import kotlinx.coroutines.flow.map
import io.getstream.chat.android.offline.querychannels.QueryChannelsController as QueryChannelsControllerStateFlow
import io.getstream.chat.android.offline.querychannels.QueryChannelsController.ChannelsState as OfflineChannelState

internal class QueryChannelsControllerImpl(private val queryChannels: QueryChannelsControllerStateFlow) :
    QueryChannelsController {

    override val filter: FilterObject by queryChannels::filter

    override val sort: QuerySort<Channel> by queryChannels::sort
    override var recoveryNeeded: Boolean by queryChannels.recoveryNeeded::value

    val queryChannelsSpec: QueryChannelsSpec by queryChannels::queryChannelsSpec

    override var chatEventHandler: ChatEventHandler? by queryChannels::chatEventHandler

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

    @Suppress("DEPRECATION_ERROR")
    override val mutedChannelIds: LiveData<List<String>> = queryChannels.mutedChannelIds.asLiveData()
}
