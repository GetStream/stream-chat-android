package io.getstream.chat.docs.kotlin.cookbook.utils

import android.content.Context
import io.getstream.chat.android.client.ChatClient

fun initChatClient(apiKey: String, context: Context) {
    ChatClient.Builder(apiKey, context).build()
}