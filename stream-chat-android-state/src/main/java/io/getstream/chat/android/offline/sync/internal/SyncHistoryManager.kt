package io.getstream.chat.android.offline.sync.internal

import io.getstream.chat.android.client.events.ChatEvent

internal interface SyncHistoryManager {

    fun setListener(listener: Listener?)

    suspend fun handleEvent(event: ChatEvent)

    suspend fun sync()

    fun interface Listener {
        suspend fun onHistorySyncCompleted(events: List<ChatEvent>)
    }
}