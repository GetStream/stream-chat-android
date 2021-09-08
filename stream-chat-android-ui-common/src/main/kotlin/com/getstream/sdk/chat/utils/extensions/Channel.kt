package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain

@InternalStreamChatApi
public fun Channel.isDirectMessaging(currentUserId: String = currentUserId()): Boolean = getUsersExcludingCurrent(currentUserId).size == 1

private fun currentUserId(): String = ChatDomain.instance().user.value?.id ?: ""
