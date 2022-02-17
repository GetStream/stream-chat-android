package io.getstream.chat.android.client.experimental.plugin

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Plugin is an extension for [ChatClient].
 */
@ExperimentalStreamChatApi
public interface Plugin {
    /**
     * Name of this plugin.
     */
    public val name: String
}
