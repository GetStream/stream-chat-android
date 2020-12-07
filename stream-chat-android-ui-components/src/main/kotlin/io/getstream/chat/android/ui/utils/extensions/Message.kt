package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.text.Spanned
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.ModelType
import java.util.Date

internal fun Message.getSenderDisplayName(context: Context): String =
    when {
        user.isCurrentUser() -> getSelfDisplayName(context)
        else -> getUserAsMention(context, user)
    }

internal fun Message.getPreviewText(context: Context): Spanned =
    context
        .getString(
            R.string.stream_ui_channel_item_last_message_template,
            getSenderDisplayName(context),
            text
        )
        .bold(getPreviewTextMentions(context))

private fun Message.getPreviewTextMentions(context: Context): List<String> =
    mentionedUsers
        .map { getUserAsMention(context, it) }
        .plus(getSenderDisplayName(context))
        .filter { it != getSelfDisplayName(context) }

private fun getUserAsMention(
    context: Context,
    it: User
) = context.getString(R.string.stream_ui_mention_user_name_template, it.name)

private fun getSelfDisplayName(context: Context) =
    context.getString(R.string.stream_ui_channel_display_name_self)

internal fun Message.isDeleted(): Boolean = deletedAt != null

internal fun Message.isFailed(): Boolean {
    return this.syncStatus == SyncStatus.FAILED_PERMANENTLY || this.type == ModelType.message_error
}

internal fun Message.isInThread(): Boolean = !parentId.isNullOrEmpty()

internal fun Message.hasNoAttachments(): Boolean = attachments.isEmpty()

internal fun Message.isEphemeral(): Boolean = type == ModelType.message_ephemeral

internal fun Message.getCreatedDate(): Date? = createdAt ?: createdLocallyAt
