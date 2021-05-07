package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.offline.ChatDomain

@InternalStreamChatApi
public val Channel.isDraft: Boolean
    get() = getExtraValue("draft", false)

@InternalStreamChatApi
public fun Channel.isDirectMessaging(currentUserId: String = currentUserId()): Boolean = getUsers(currentUserId).size == 1

@InternalStreamChatApi
public fun Channel.getUsers(excludeUserId: String = currentUserId()): List<User> =
    members.map { it.user }.filterNot { it.id == excludeUserId }

private fun currentUserId(): String = ChatDomain.instance().user.value?.id ?: ""
