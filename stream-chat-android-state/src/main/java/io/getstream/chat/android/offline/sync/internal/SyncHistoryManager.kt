package io.getstream.chat.android.offline.sync.internal

import io.getstream.chat.android.client.events.ChatEvent
import kotlinx.coroutines.flow.Flow

internal interface SyncHistoryManager {

    val syncedEvents: Flow<List<ChatEvent>>

    fun start()

    suspend fun sync()

    suspend fun awaitSyncing()

    fun stop()
}