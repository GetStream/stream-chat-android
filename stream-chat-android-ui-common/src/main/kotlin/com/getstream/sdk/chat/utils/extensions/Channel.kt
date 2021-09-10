package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun Channel.isDirectMessaging(): Boolean = getUsersExcludingCurrent().size == 1
