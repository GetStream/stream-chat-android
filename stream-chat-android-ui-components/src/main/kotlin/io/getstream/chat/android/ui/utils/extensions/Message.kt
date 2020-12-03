package io.getstream.chat.android.ui.utils.extensions

import android.content.Context
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.ModelType
import java.util.Date

internal fun Message.getSenderDisplayName(context: Context, isDirectMessaging: Boolean = false): String? =
    when {
        user.isCurrentUser() -> getSelfDisplayName(context)
        isDirectMessaging -> null
        else -> user.asMention(context)
    }

private fun getSelfDisplayName(context: Context) =
    context.getString(R.string.stream_ui_channel_display_name_self)

internal fun Message.isDeleted(): Boolean = deletedAt != null

internal fun Message.isFailed(): Boolean {
    return this.syncStatus == SyncStatus.FAILED_PERMANENTLY || this.type == ModelType.message_error
}

internal fun Message.isInThread(): Boolean = !parentId.isNullOrEmpty()

internal fun Message.hasNoAttachments(): Boolean = attachments.isEmpty()

internal fun Message.isEphemeral(): Boolean = type == ModelType.message_ephemeral

internal fun Message.getCreatedAtOrNull(): Date? = createdAt ?: createdLocallyAt

internal fun Message.getCreatedAtOrThrow(): Date = checkNotNull(getCreatedAtOrNull()) {
    "a message needs to have a non null value for either createdAt or createdLocallyAt"
}
