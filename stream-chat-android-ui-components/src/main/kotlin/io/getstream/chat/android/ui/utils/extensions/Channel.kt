package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
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
            it.type == "regular"
    }.maxByOrNull { it.createdAt ?: it.createdLocallyAt!! }

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

internal fun Channel.getReadStatusDrawable(context: Context): Drawable? =
    when (getLastMessageReadCount()) {
        0 -> ContextCompat.getDrawable(context, R.drawable.stream_ic_icon_check)
        else -> ContextCompat.getDrawable(context, R.drawable.stream_ic_icon_check_all)
        // wip - need to figure out message pending status for clock icon
    }

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
