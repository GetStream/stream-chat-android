package com.getstream.sdk.chat.adapter.viewholder.message

import androidx.annotation.IdRes
import com.getstream.sdk.chat.databinding.StreamItemMessageBinding
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus

internal fun Message.isDeleted(): Boolean = deletedAt != null

internal fun Message.isFailed(): Boolean {
    return this.syncStatus == SyncStatus.FAILED_PERMANENTLY || this.type == ModelType.message_error
}

internal fun Message.isInThread(): Boolean = !parentId.isNullOrEmpty()

internal fun Message.hasNoAttachments(): Boolean = attachments.isEmpty()

internal fun Message.isEphemeral(): Boolean = type == ModelType.message_ephemeral

@IdRes
internal fun getActiveContentViewResId(message: Message, binding: StreamItemMessageBinding): Int {
    return when {
        message.attachments.isNotEmpty() -> binding.attachmentview.id
        else -> binding.tvText.id
    }
}
