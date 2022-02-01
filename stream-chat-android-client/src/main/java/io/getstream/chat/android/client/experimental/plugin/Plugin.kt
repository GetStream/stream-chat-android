package io.getstream.chat.android.client.experimental.plugin

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Plugin is an extension for [ChatClient].
 */
@ExperimentalStreamChatApi
public interface Plugin {
    public val name: String

    /**
     * Initialize a plugin. Do not implement heavy work here. It is invoked when build ChatClient.
     */
    public fun init(appContext: Context, chatClient: ChatClient)
}
