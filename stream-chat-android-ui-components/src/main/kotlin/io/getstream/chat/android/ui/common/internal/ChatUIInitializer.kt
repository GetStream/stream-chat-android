package io.getstream.chat.android.ui.common.internal

import android.content.Context
import androidx.startup.Initializer
import io.getstream.chat.android.ui.ChatUI

public class ChatUIInitializer : Initializer<ChatUI> {
    override fun create(context: Context): ChatUI {
        ChatUI.appContext = context
        return ChatUI
    }
    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}
