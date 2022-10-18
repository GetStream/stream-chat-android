package io.getstream.chat.android.offline.repository.realm.entity

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.repository.realm.utils.toDate
import io.getstream.chat.android.offline.repository.realm.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import io.realm.kotlin.types.RealmObject

@Suppress("VariableNaming")
internal class ChannelUserReadEntityRealm : RealmObject {
    var user: UserEntityRealm? = null
    var last_read: RealmInstant? = null
    var unread_messages: Int? = null
    var last_message_seen_date: RealmInstant? = null
}

internal fun ChannelUserReadEntityRealm.toDomain(): ChannelUserRead =
    ChannelUserRead(
        user = user?.toDomain() ?: User(),
        lastRead = last_read?.toDate(),
        unreadMessages = unread_messages ?: 0,
        lastMessageSeenDate = last_message_seen_date?.toDate(),
    )

internal fun ChannelUserRead.toRealm(): ChannelUserReadEntityRealm {
    val thisChannelRead: ChannelUserRead = this

    return ChannelUserReadEntityRealm().apply {
        user = thisChannelRead.user.toRealm()
        last_read = thisChannelRead.lastRead?.toRealmInstant()
        unread_messages = thisChannelRead.unreadMessages
        last_message_seen_date = thisChannelRead.lastMessageSeenDate?.toRealmInstant()
    }
}
