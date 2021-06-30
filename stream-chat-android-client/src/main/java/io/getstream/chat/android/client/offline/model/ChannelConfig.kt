package io.getstream.chat.android.client.offline.model

import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public data class ChannelConfig(val type: String, val config: Config)
