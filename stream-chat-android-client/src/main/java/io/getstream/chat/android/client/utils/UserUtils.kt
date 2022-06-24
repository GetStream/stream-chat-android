package io.getstream.chat.android.client.utils

import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Partially merges [that] user data into [this] user data.
 */
@InternalStreamChatApi
public fun User.mergePartially(that: User): User {
    this.role = that.role
    this.createdAt = that.createdAt
    this.updatedAt = that.updatedAt
    this.lastActive = that.lastActive
    this.banned = that.banned
    this.name = that.name
    this.image = that.image
    return this
}
