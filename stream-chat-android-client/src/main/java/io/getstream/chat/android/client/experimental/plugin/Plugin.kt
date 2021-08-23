package io.getstream.chat.android.client.experimental.plugin

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.experimental.plugin.listeners.OperationListenersFacade
import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Plugin is an extension for [ChatClient]. It extends [OperationListenersFacade] and provides additionally
 * name attribute and init method.
 */
@ExperimentalStreamChatApi
public interface Plugin : OperationListenersFacade {
    public val name: String

    /**
     * Initialize a plugin. Do not implement heavy work here. It is invoked when build ChatClient.
     */
    public fun init(appContext: Context, chatClient: ChatClient)
}
