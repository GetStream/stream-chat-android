package io.getstream.chat.android.livedata.entity

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import java.util.Date

/**
 * Efficiently store the channel user read info
 *
 */
internal data class ChannelUserReadEntity(var userId: String) {
    /** how far this user had read */
    var lastRead: Date? = null

    constructor(r: ChannelUserRead) : this(r.getUserId()) {
        lastRead = r.lastRead
    }

    /** converts the entity into channel user read */
    fun toChannelUserRead(userMap: Map<String, User>): ChannelUserRead {
        val user = userMap[userId]
            ?: error("userMap doesnt contain the user $userId for the channel read")

        return ChannelUserRead(user, lastRead)
    }
}
