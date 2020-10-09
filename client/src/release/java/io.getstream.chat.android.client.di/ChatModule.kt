package io.getstream.chat.android.client.di

import android.content.Context
import io.getstream.chat.android.client.api.ChatClientConfig

internal class ChatModule(appContext: Context, config: ChatClientConfig) : BaseChatModule(appContext, config)
