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

public fun Message.isDeleted(): Boolean = deletedAt != null

public fun Message.isFailed(): Boolean {
    return this.syncStatus == SyncStatus.FAILED_PERMANENTLY || this.type == ModelType.message_error
}

public fun Message.isInThread(): Boolean = !parentId.isNullOrEmpty()

public fun Message.hasNoAttachments(): Boolean = attachments.isEmpty()

public fun Message.isEphemeral(): Boolean = type == ModelType.message_ephemeral

public fun Message.isGiphyEphemeral(): Boolean = isEphemeral() && command == ModelType.attach_giphy

public fun Message.isGiphyNotEphemeral(): Boolean = isEphemeral().not() && command == ModelType.attach_giphy

public fun Message.getCreatedAtOrNull(): Date? = createdAt ?: createdLocallyAt

public fun Message.getUpdatedAtOrNull(): Date? = updatedAt ?: updatedLocallyAt

public fun Message.getCreatedAtOrThrow(): Date = checkNotNull(getCreatedAtOrNull()) {
    "a message needs to have a non null value for either createdAt or createdLocallyAt"
}

public fun Message.hasSingleReaction(): Boolean {
    return reactionCounts.size == 1
}

public fun Message.isReply(): Boolean = replyTo != null

public fun Message.hasText(): Boolean = text.isNotEmpty()
