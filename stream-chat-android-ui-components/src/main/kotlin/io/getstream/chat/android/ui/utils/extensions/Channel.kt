package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
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
    messages
        .asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { it.type == ModelType.message_regular }
        .maxByOrNull { it.createdAt ?: it.createdLocallyAt!! }

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

internal fun Channel.getLastMessageTime(): Date? = getLastMessage()?.let { it.createdAt ?: it.createdLocallyAt }

internal fun Channel.getCurrentUser(): User = ChatDomain.instance().currentUser

internal fun Channel.getCurrentUserRead(): ChannelUserRead? =
    read.firstOrNull { it.user.id == getCurrentUser().id }

internal fun Channel.diff(other: Channel): ChannelDiff =
    ChannelDiff(
        nameChanged = name != other.name,
        avatarViewChanged = getUsers() != other.getUsers(),
        readStateChanged = unreadCount != other.unreadCount,
        lastMessageChanged = getLastMessage() != other.getLastMessage()
    )

internal fun Channel.getOnlineStateSubtitle(context: Context): String {
    val users = getUsers()
    if (users.isEmpty()) return String.EMPTY

    if (users.size == 1) {
        return users.first().getLastSeenText(context)
    }

    return getGroupSubtitle(context)
}

internal fun Channel.getGroupSubtitle(context: Context): String {
    val allUsers = members.map { it.user }
    val onlineUsers = allUsers.count { it.online }
    val groupMembers = context.resources.getQuantityString(
        R.plurals.stream_message_list_header_group_member_count,
        allUsers.size,
        allUsers.size
    )

    return if (onlineUsers > 0) {
        context.getString(R.string.stream_message_list_header_group_member_count_with_online, groupMembers, onlineUsers)
    } else {
        groupMembers
    }
}

internal fun Channel.lastMessageByCurrentUserWasRead(): Boolean {
    return getCurrentUserLastMessage()?.let { currentUserLastMessage ->
        read.any { channelRead ->
            val lastMessageCreatedAt = currentUserLastMessage.createdAt
            if (lastMessageCreatedAt != null) {
                channelRead.lastRead?.before(lastMessageCreatedAt)?.not() ?: false
            } else {
                false
            }
        }
    } ?: false
}
