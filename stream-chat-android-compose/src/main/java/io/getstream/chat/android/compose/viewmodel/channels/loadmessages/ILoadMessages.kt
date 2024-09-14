package io.getstream.chat.android.compose.viewmodel.channels.loadmessages

internal interface ILoadMessages {
    suspend fun load(query: String)
    suspend fun loadMore()
}

