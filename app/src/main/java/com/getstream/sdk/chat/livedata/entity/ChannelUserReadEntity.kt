package com.getstream.sdk.chat.livedata.entity

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import java.util.*

/**
 * Efficiently store the channel user read info
 *
 */
data class ChannelUserReadEntity(var userId: String) {
    /** how far this user had read */
    var lastRead: Date? = null

    constructor(r: ChannelUserRead): this(r.getUserId()) {
        lastRead = r.lastRead
    }

    /** converts the entity into channel user read */
    fun toChannelUserRead(userMap: Map<String, User>): ChannelUserRead {
        val r = ChannelUserRead()
        // TODO: get rid of the ugly !!
        r.user = userMap.get(userId)!!
        r.lastRead = lastRead

        return r

    }
}