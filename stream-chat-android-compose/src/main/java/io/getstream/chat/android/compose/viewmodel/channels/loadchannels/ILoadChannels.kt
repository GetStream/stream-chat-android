package io.getstream.chat.android.compose.viewmodel.channels.loadchannels

import io.getstream.chat.android.client.api.models.QueryChannelsRequest

internal interface ILoadChannels {
    suspend fun load(queryChannelsRequest: QueryChannelsRequest)
    suspend fun loadMore()
}

