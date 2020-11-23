package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import android.text.Spanned
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.ModelType
import java.util.Date

internal fun Message.getSenderDisplayName(context: Context): String =
    when {
        user.isCurrentUser() -> context.getString(R.string.stream_channel_display_name_self)
        else -> context.getString(R.string.stream_mention_user_name_template, user.name)
    }

internal fun Message.getPreviewText(context: Context): Spanned =
    context
        .getString(
            R.string.stream_channel_item_last_message_template,
            getSenderDisplayName(context),
            text
        )
        .boldMentions()

internal fun Message.isDeleted(): Boolean = deletedAt != null

internal fun Message.isFailed(): Boolean {
    return this.syncStatus == SyncStatus.FAILED_PERMANENTLY || this.type == ModelType.message_error
}

internal fun Message.isInThread(): Boolean = !parentId.isNullOrEmpty()

internal fun Message.hasNoAttachments(): Boolean = attachments.isEmpty()

internal fun Message.isEphemeral(): Boolean = type == ModelType.message_ephemeral

// @IdRes
// internal fun getActiveContentViewResId(message: Message, binding: StreamItemMessageBinding): Int {
//     return when {
//         message.attachments.isNotEmpty() -> binding.attachmentview.id
//         else -> binding.tvText.id
//     }
// }

internal fun Message.getCreatedDate(): Date? = createdAt ?: createdLocallyAt
