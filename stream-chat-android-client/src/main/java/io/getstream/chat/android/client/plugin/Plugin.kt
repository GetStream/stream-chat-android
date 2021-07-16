package io.getstream.chat.android.client.plugin

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.plugin.listeners.QueryChannelsListener

public interface Plugin : QueryChannelsListener {
    public val name: String

    public fun init(appContext: Context, chatClient: ChatClient)
}
