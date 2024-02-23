package io.getstream.chat.docs.kotlin.cookbook.utils

import android.content.Context
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory

fun initChatClient(apiKey: String, context: Context) {
    val statePluginFactory = StreamStatePluginFactory(config = StatePluginConfig(), appContext = context)

    ChatClient.Builder(apiKey, context).withPlugins(statePluginFactory).build()
}