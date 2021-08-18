package io.getstream.chat.android.ui.common.internal

import android.content.Context
import androidx.startup.Initializer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.header.VersionPrefixHeader
import io.getstream.chat.android.ui.ChatUI

public class ChatUIInitializer : Initializer<ChatUI> {
    override fun create(context: Context): ChatUI {
        ChatClient.VERSION_PREFIX_HEADER = VersionPrefixHeader.UI_COMPONENTS
        ChatUI.appContext = context
        return ChatUI
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
