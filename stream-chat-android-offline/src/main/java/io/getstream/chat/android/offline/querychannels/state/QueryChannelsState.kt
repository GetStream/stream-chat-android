package io.getstream.chat.android.offline.querychannels.state

import io.getstream.chat.android.client.api.models.FilterObject
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Channel
import kotlinx.coroutines.flow.StateFlow

public interface QueryChannelsState {
    public val filter: FilterObject
    public val sort: QuerySort<Channel>
    public val currentRequest: StateFlow<QueryChannelsRequest?>
    public val nextRequest: StateFlow<QueryChannelsRequest?>
    public val loading: StateFlow<Boolean>
    public val loadingMore: StateFlow<Boolean>
    public val endOfChannels: StateFlow<Boolean>
    public val channels: StateFlow<List<Channel>>
    public val mutedChannelIds: StateFlow<List<String>>
    public val channelsState: StateFlow<ChannelsState>
}
