package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.core.internal.InternalStreamChatApi

@InternalStreamChatApi
public fun Channel.isDirectMessaging(): Boolean {
    return members.size == 2 && includesCurrentUser()
}

private fun Channel.includesCurrentUser(): Boolean {
    val currentUserId = ChatClient.instance().getCurrentUser()?.id ?: return false
    return members.any { it.user.id == currentUserId }
}
