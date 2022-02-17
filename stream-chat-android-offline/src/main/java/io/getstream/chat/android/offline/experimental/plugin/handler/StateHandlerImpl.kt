package io.getstream.chat.android.offline.experimental.plugin.handler

import io.getstream.chat.android.client.experimental.plugin.handler.StateHandler

internal class StateHandlerImpl : StateHandler {

    private val clearStateListeners: MutableList<() -> Unit> = mutableListOf()

    override fun registerClearStateListener(listener: () -> Unit) {
        clearStateListeners.add(listener)
    }

    override fun clearState() {
        clearStateListeners.forEach { listener -> listener() }
        clearStateListeners.clear()
    }
}
