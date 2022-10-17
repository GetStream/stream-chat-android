package io.getstream.chat.android.offline.repository.realm.entity

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.realm.kotlin.types.RealmObject
import java.util.Date

internal class ChannelUserReadEntityRealm : RealmObject {
    var user_id: String? = null
    var last_read: Date? = null
    var unread_messages: Int? = null
    var last_message_seen_date: Date? = null
}

internal suspend fun ChannelUserReadEntityRealm.toDomain(getUser: suspend (String) -> User): ChannelUserRead =
    ChannelUserRead(
        user = getUser(user_id!!),
        lastRead = last_read,
        unreadMessages = unread_messages ?: 0,
        lastMessageSeenDate = last_message_seen_date,
    )

internal fun ChannelUserRead.toRealm(): ChannelUserReadEntityRealm {
    val thisChannelRead: ChannelUserRead = this

    return ChannelUserReadEntityRealm().apply {
        user_id = thisChannelRead.user.id
        last_read = thisChannelRead.lastRead
        unread_messages = thisChannelRead.unreadMessages
        last_message_seen_date = thisChannelRead.lastMessageSeenDate
    }
}
