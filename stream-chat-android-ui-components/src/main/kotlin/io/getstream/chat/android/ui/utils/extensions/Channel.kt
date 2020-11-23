package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.utils.ModelType
import java.util.Date

internal fun Channel.getUsers(excludeCurrentUser: Boolean = true): List<User> =
    members
        .map { it.user }
        .let { users ->
            when {
                excludeCurrentUser -> users.withoutCurrentUser()
                else -> users
            }
        }

internal fun Channel.getDisplayName(): String = name.takeIf { it.isNotEmpty() }
    ?: getUsers().joinToString { it.name }

internal fun Channel.getLastMessage(): Message? =
    messages.filter {
        (it.createdAt != null || it.createdLocallyAt != null) &&
            it.deletedAt == null &&
            it.type == ModelType.message_regular
    }.maxByOrNull { it.createdAt ?: it.createdLocallyAt!! }

internal fun Channel.getCurrentUserLastMessage(): Message? = getLastMessageByUserId(getCurrentUser().id)

internal fun Channel.getLastMessageByUserId(userId: String): Message? =
    messages.lastOrNull {
        it.user.id == userId &&
            !it.isFailed() &&
            !it.isDeleted() &&
            !it.isInThread() &&
            !it.isEphemeral()
    }

internal fun Channel.getUnreadUsers(): List<ChannelUserRead> =
    read.filter { it.lastRead?.before(lastMessageAt) == true }

internal fun Channel.getLastMessageReadCount(): Int =
    read.filter { userRead ->
        lastMessageAt?.let { lastMessage ->
            userRead.lastRead?.before(lastMessage)
        } == false
    }.count()

internal fun Channel.currentUserHasReadLastMessage(): Boolean =
    getCurrentUserRead()?.lastRead?.let { lastRead ->
        getLastMessage()?.createdAt?.let { lastMessageDate ->
            !lastMessageDate.after(lastRead)
        }
    } ?: false

internal fun Channel.getLastMessageTime(): Date? = getLastMessage()?.let {
    it.createdAt ?: it.createdLocallyAt
}

internal fun Channel.getCurrentUser(): User = ChatDomain.instance().currentUser

internal fun Channel.getCurrentUserRead(): ChannelUserRead? =
    read.firstOrNull { it.user.id == getCurrentUser().id }

internal fun Channel.getCurrentUserUnreadCount(): Int = getCurrentUserRead()?.lastRead?.let { currentRead ->
    messages.count { message ->
        message.createdAt?.after(currentRead) == true
    }
} ?: 0

internal fun Channel.diff(other: Channel): ChannelDiff =
    ChannelDiff(
        nameChanged = name != other.name,
        avatarViewChanged = getUsers() != other.getUsers(),
        readStateChanged = getLastMessageReadCount() != other.getLastMessageReadCount(),
        lastMessageChanged = getLastMessage() != other.getLastMessage()
    )

internal fun Channel.currentUserLastMessageWasRead(): Boolean {
    return getCurrentUserLastMessage()?.let { currentUserLastMessage ->
        read.any { channelRead ->
            channelRead.lastRead?.before(currentUserLastMessage.createdAt)?.not() ?: false
        }
    } ?: false
}
