package com.getstream.sdk.chat.utils.extensions

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.livedata.ChatDomain

internal val Channel.isDraft: Boolean
    get() = getExtraValue("draft", false)

@InternalStreamChatApi
public fun Channel.isDirectMessaging(): Boolean = getUsers().size == 1

@InternalStreamChatApi
public fun Channel.getUsers(excludeCurrentUser: Boolean = true): List<User> =
    members
        .map { it.user }
        .let { users ->
            when {
                excludeCurrentUser -> users.withoutCurrentUser()
                else -> users
            }
        }

private fun List<User>.withoutCurrentUser(): List<User> {
    return if (ChatDomain.isInitialized) {
        val currentUser = ChatDomain.instance().currentUser
        filter { it.id != currentUser.id }
    } else {
        this
    }
}
