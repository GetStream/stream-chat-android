package io.getstream.chat.android.offline.experimental.plugin.listener

import io.getstream.chat.android.client.experimental.plugin.listeners.MarkAllReadListener
import io.getstream.chat.android.offline.experimental.plugin.logic.LogicRegistry
import kotlinx.coroutines.awaitAll

internal class MarkAllReadListenerImpl(private val logic: LogicRegistry) : MarkAllReadListener {
    override suspend fun onMarkAllReadRequest() {
        logic.getActiveChannelsLogic().map { channel ->
            channel.markReadAsync()
        }.awaitAll()
    }
}
