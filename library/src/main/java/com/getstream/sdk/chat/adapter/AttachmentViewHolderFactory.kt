package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.model.ModelType

open class AttachmentViewHolderFactory {
    lateinit var listenerContainer: ListenerContainer

    open fun getAttachmentViewType(
        attachmentItem: AttachmentListItem
    ): Int {
        return when (attachmentItem.attachment.type) {
            null ->
                MessageViewHolderFactory.GENERIC_ATTACHMENT
            ModelType.attach_video ->
                MessageViewHolderFactory.VIDEO_ATTACHMENT
            ModelType.attach_image, ModelType.attach_giphy ->
                MessageViewHolderFactory.IMAGE_ATTACHMENT
            ModelType.attach_file ->
                MessageViewHolderFactory.FILE_ATTACHMENT
            else ->
                MessageViewHolderFactory.GENERIC_ATTACHMENT
        }
    }

    open fun createAttachmentViewHolder(
        adapter: AttachmentListItemAdapter,
        parent: ViewGroup,
        viewType: Int
    ): BaseAttachmentViewHolder {
        return when (viewType) {
            MessageViewHolderFactory.VIDEO_ATTACHMENT, MessageViewHolderFactory.IMAGE_ATTACHMENT ->
                AttachmentViewHolderMedia(
                    R.layout.stream_item_attach_media,
                    parent,
                    listenerContainer.giphySendListener
                )
            MessageViewHolderFactory.FILE_ATTACHMENT ->
                AttachmentViewHolderFile(R.layout.stream_item_attachment_file, parent)
            else ->
                AttachmentViewHolder(R.layout.stream_item_attachment, parent)
        }
    }
}
