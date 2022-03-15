package io.getstream.chat.android.client.experimental.plugin

import io.getstream.chat.android.client.ChatClient

/**
 * Plugin is an extension for [ChatClient].
 */
public interface Plugin {
    /**
     * Name of this plugin.
     */
    public val name: String
}
