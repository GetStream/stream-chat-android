package io.getstream.chat.android.ui.message.list.adapter.internal

import androidx.recyclerview.widget.DiffUtil
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.message.list.adapter.AttachmentItemPayloadDiff

internal object AttachmentItemDiffCallback : DiffUtil.ItemCallback<Attachment>() {
    override fun areItemsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Attachment, newItem: Attachment): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Attachment, newItem: Attachment): Any {
        return AttachmentItemPayloadDiff(
            oldItem.authorName != newItem.authorName,
            oldItem.authorLink != newItem.authorLink,
            oldItem.titleLink != newItem.titleLink,
            oldItem.thumbUrl != newItem.thumbUrl,
            oldItem.imageUrl != newItem.imageUrl,
            oldItem.assetUrl != newItem.assetUrl,
            oldItem.ogUrl != newItem.ogUrl,
            oldItem.mimeType != newItem.mimeType,
            oldItem.fileSize != newItem.fileSize,
            oldItem.title != newItem.title,
            oldItem.text != newItem.text,
            oldItem.type != newItem.type,
            oldItem.image != newItem.image,
            oldItem.url != newItem.url,
            oldItem.name != newItem.name,
            oldItem.upload != newItem.upload,
            oldItem.uploadState != newItem.uploadState
        )
    }
}
