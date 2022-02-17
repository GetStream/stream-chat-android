package io.getstream.chat.android.client.experimental.plugin.handler

public interface StateHandler {

    public fun registerClearStateListener(listener: () -> Unit)

    public fun clearState()
}
