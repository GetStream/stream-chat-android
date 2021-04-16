package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import com.getstream.sdk.chat.utils.extensions.getUsers
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.common.extensions.isRegular
import io.getstream.chat.android.ui.common.extensions.isSystem
import io.getstream.chat.android.ui.common.internal.ModelType

internal fun Channel.getLastMessage(): Message? =
    messages.asSequence()
        .filter { it.createdAt != null || it.createdLocallyAt != null }
        .filter { it.deletedAt == null }
        .filter { !it.silent }
        .filter { it.user.isCurrentUser() || !it.shadowed }
        .filter { it.isRegular() || it.isSystem() }
        .maxByOrNull { it.getCreatedAtOrThrow() }

internal fun Channel.diff(other: Channel): ChannelListPayloadDiff =
    ChannelListPayloadDiff(
        nameChanged = name != other.name,
        avatarViewChanged = getUsers() != other.getUsers(),
        readStateChanged = read != other.read,
        lastMessageChanged = getLastMessage() != other.getLastMessage(),
        unreadCountChanged = unreadCount != other.unreadCount,
    )

internal fun Channel.isMessageRead(message: Message): Boolean {
    val currentUser = ChatDomain.instance().currentUser
    return read.filter { it.user.id != currentUser.id }
        .mapNotNull { it.lastRead }
        .any { it.time >= message.getCreatedAtOrThrow().time }
}

// None of the strings used to assemble the preview message are translatable - concatenation here should be fine
internal fun Channel.getLastMessagePreviewText(
    context: Context,
    isDirectMessaging: Boolean = false,
): SpannableStringBuilder? {
    return getLastMessage()?.let { message ->
        if (message.isSystem()) {
            SpannableStringBuilder(message.text.trim().italicize())
        } else {
            val sender = message.getSenderDisplayName(context, isDirectMessaging)

            // bold mentions of the current user
            val currentUserMention = ChatDomain.instance().currentUser.asMention(context)
            val previewText: SpannableString =
                message.text.trim().bold(currentUserMention.singletonList(), ignoreCase = true)

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
}

private fun getAttachmentPrefix(attachment: Attachment): String? =
    when (attachment.type) {
        ModelType.attach_giphy -> "/giphy"
        else -> null
    }
