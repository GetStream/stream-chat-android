package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.channel.list.adapter.diff.ChannelDiff
import io.getstream.chat.android.ui.utils.ModelType

internal fun Channel.getUsers(excludeCurrentUser: Boolean = true): List<User> =
    members
        .map { it.user }
        .let { users ->
            when {
                excludeCurrentUser -> users.withoutCurrentUser()
                else -> users
            }
        }

public fun Channel.getDisplayName(context: Context): String =
    name.takeIf { it.isNotEmpty() }
        ?: getUsers()
            .joinToString { it.name }
            .takeIf { it.isNotEmpty() }
        ?: context.getString(R.string.stream_ui_stream_channel_unknown_title)

internal fun Channel.getLastMessage(): Message? =
    messages.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { !it.shadowed }
        .filter { it.type == ModelType.message_regular }
        .maxByOrNull { it.getCreatedAtOrThrow() }

internal fun Channel.getLastMessageByUserId(userId: String): Message? =
    messages.lastOrNull {
        it.user.id == userId &&
            !it.isFailed() &&
            !it.isDeleted() &&
            !it.isInThread() &&
            !it.isEphemeral()
    }

internal fun Channel.diff(other: Channel): ChannelDiff =
    ChannelDiff(
        nameChanged = name != other.name,
        avatarViewChanged = getUsers() != other.getUsers(),
        readStateChanged = read != other.read,
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
        R.plurals.stream_ui_message_list_header_group_member_count,
        allUsers.size,
        allUsers.size
    )

    return if (onlineUsers > 0) {
        context.getString(
            R.string.stream_ui_message_list_header_group_member_count_with_online,
            groupMembers,
            onlineUsers
        )
    } else {
        groupMembers
    }
}

internal fun Channel.isMessageRead(message: Message): Boolean {
    val currentUser = ChatDomain.instance().currentUser
    return read.filter { it.user.id != currentUser.id }
        .mapNotNull { it.lastRead }
        .any { it.time >= message.getCreatedAtOrThrow().time }
}

internal fun Channel.isDirectMessaging(): Boolean = getUsers().size == 1

// None of the strings used to assemble the preview message are translatable - concatenation here should be fine
internal fun Channel.getLastMessagePreviewText(
    context: Context,
    isDirectMessaging: Boolean = false
): SpannableStringBuilder? {
    return getLastMessage()?.let { message ->
        val sender = message.getSenderDisplayName(context, isDirectMessaging)

        // bold mentions of the current user
        val currentUserMention = ChatDomain.instance().currentUser.asMention(context)
        val previewText: SpannableString = message.text.trim().bold(currentUserMention.singletonList(), ignoreCase = true)

        val attachments: SpannableString? = message.attachments
            .takeIf { it.isNotEmpty() }
            ?.mapNotNull { attachment ->
                attachment.title?.let { title ->
                    val prefix = getAttachmentPrefix(attachment)
                    if (prefix != null) {
                        "$prefix $title"
                    } else {
                        title
                    }
                } ?: attachment.name
            }
            ?.joinToString()
            ?.italicize()

        listOf(sender, previewText, attachments)
            .filterNot { it.isNullOrEmpty() }
            .joinTo(SpannableStringBuilder(), ": ")
    }
}

private fun getAttachmentPrefix(attachment: Attachment): String? =
    when (attachment.type) {
        ModelType.attach_giphy -> "/giphy"
        else -> null
    }
