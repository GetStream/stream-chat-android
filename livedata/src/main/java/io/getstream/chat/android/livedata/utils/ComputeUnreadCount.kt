package io.getstream.chat.android.livedata.utils

import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User

fun computeUnreadCount(currentUser: User, read: ChannelUserRead?, messages: List<Message>?): Int? {
    var unreadMessageCount: Int? = null
    if (messages != null) {
        unreadMessageCount = 0
        val lastRead = read?.lastRead
        val lastReadTime = lastRead?.time ?: 0
        val currentUserId = currentUser.id
        for (m in messages.reversed()) {
            if (m.user.id == currentUserId) continue
            if (m.deletedAt != null) continue
            if (m.silent) continue
            if ((m.createdAt ?: m.createdLocallyAt)?.time ?: lastReadTime > lastReadTime) unreadMessageCount++
        }
    }
    return unreadMessageCount
}
