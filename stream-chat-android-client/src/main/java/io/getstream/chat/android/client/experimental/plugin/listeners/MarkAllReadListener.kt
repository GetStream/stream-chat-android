package io.getstream.chat.android.client.experimental.plugin.listeners

import io.getstream.chat.android.client.ChatClient

/**
 * Listener for [ChatClient.markAllRead] requests.
 */
public interface MarkAllReadListener {

    /**
     * Register this side effect to run just before actual [ChatClient.markAllRead] request is launched.
     */
    public suspend fun onMarkAllReadRequest()
}
