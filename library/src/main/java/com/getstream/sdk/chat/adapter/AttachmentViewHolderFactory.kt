package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.viewholder.attachment.AttachmentViewHolder
import com.getstream.sdk.chat.adapter.viewholder.attachment.AttachmentViewHolderFile
import com.getstream.sdk.chat.adapter.viewholder.attachment.AttachmentViewHolderMedia
import com.getstream.sdk.chat.adapter.viewholder.attachment.BaseAttachmentViewHolder
import com.getstream.sdk.chat.model.ModelType

/**
 * Allows you to easily customize attachment rendering
 */
open class AttachmentViewHolderFactory {

    companion object {
        const val GENERIC_ATTACHMENT = 1
        const val IMAGE_ATTACHMENT = 2
        const val VIDEO_ATTACHMENT = 3
        const val FILE_ATTACHMENT = 4
    }

    lateinit var listenerContainer: ListenerContainer
        @JvmName("setListenerContainerInternal")
        internal set

    open fun getAttachmentViewType(
        attachmentItem: AttachmentListItem
    ): Int {
        return when (attachmentItem.attachment.type) {
            null ->
                GENERIC_ATTACHMENT
            ModelType.attach_video ->
                VIDEO_ATTACHMENT
            ModelType.attach_image, ModelType.attach_giphy ->
                IMAGE_ATTACHMENT
            ModelType.attach_file ->
                FILE_ATTACHMENT
            else ->
                GENERIC_ATTACHMENT
        }
    }

    open fun createAttachmentViewHolder(
        adapter: AttachmentListItemAdapter,
        parent: ViewGroup,
        viewType: Int
    ): BaseAttachmentViewHolder {
        return when (viewType) {
            VIDEO_ATTACHMENT, IMAGE_ATTACHMENT ->
                AttachmentViewHolderMedia(
                    R.layout.stream_item_attach_media,
                    parent,
                    listenerContainer.giphySendListener
                )
            FILE_ATTACHMENT ->
                AttachmentViewHolderFile(R.layout.stream_item_attachment_file, parent)
            else ->
                AttachmentViewHolder(R.layout.stream_item_attachment, parent)
        }
    }
}
