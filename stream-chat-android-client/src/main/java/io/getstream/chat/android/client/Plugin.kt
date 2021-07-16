package io.getstream.chat.android.client

import android.content.Context

public interface Plugin {
    public val name: String

    public fun init(appContext: Context, chatClient: ChatClient)
}
