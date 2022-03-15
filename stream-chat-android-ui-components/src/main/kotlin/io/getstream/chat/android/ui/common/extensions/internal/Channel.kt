package io.getstream.chat.android.ui.common.extensions.internal

import android.content.Context
import android.text.SpannableString
import android.text.SpannableStringBuilder
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.extensions.getUsersExcludingCurrent
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.experimental.extensions.globalState
import io.getstream.chat.android.ui.channel.list.adapter.ChannelListPayloadDiff
import io.getstream.chat.android.ui.common.extensions.getCreatedAtOrThrow
import io.getstream.chat.android.ui.common.extensions.getLastMessage
import io.getstream.chat.android.ui.common.extensions.isSystem

internal fun Channel.diff(other: Channel): ChannelListPayloadDiff {
    val usersChanged = getUsersExcludingCurrent() != other.getUsersExcludingCurrent()
    return ChannelListPayloadDiff(
        nameChanged = name != other.name,
        avatarViewChanged = usersChanged,
        usersChanged = usersChanged,
        readStateChanged = read != other.read,
        lastMessageChanged = getLastMessage() != other.getLastMessage(),
        unreadCountChanged = unreadCount != other.unreadCount,
        extraDataChanged = extraData != other.extraData
    )
}

internal fun Channel.isMessageRead(message: Message): Boolean {
    val currentUser = ChatClient.instance().getCurrentUser()
    return read.filter { it.user.id != currentUser?.id }
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
            val currentUserMention = ChatClient.instance().globalState.user.value?.asMention(context)
            val previewText: SpannableString =
                message.text.trim().bold(currentUserMention?.singletonList(), ignoreCase = true)

            val attachmentsText: SpannableString? = message.getAttachmentsText()

            listOf(sender, previewText, attachmentsText)
                .filterNot { it.isNullOrEmpty() }
                .joinTo(SpannableStringBuilder(), ": ")
        }
    }
}

internal const val EXTRA_DATA_MUTED: String = "mutedChannel"

internal var Channel.isMuted: Boolean
    get() = extraData[EXTRA_DATA_MUTED] as Boolean? ?: false
    set(value) {
        extraData[EXTRA_DATA_MUTED] = value
    }
