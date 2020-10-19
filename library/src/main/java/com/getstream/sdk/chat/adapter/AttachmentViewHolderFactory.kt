package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.viewholder.attachment.AttachmentViewHolder
import com.getstream.sdk.chat.adapter.viewholder.attachment.AttachmentViewHolderFile
import com.getstream.sdk.chat.adapter.viewholder.attachment.AttachmentViewHolderMedia
import com.getstream.sdk.chat.adapter.viewholder.attachment.BaseAttachmentViewHolder
import com.getstream.sdk.chat.model.ModelType
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle

/**
 * Allows you to easily customize attachment rendering
 */
public open class AttachmentViewHolderFactory {

    public companion object {
        public const val GENERIC_ATTACHMENT: Int = 1
        public const val IMAGE_ATTACHMENT: Int = 2
        public const val VIDEO_ATTACHMENT: Int = 3
        public const val FILE_ATTACHMENT: Int = 4
    }

    public lateinit var listenerContainer: ListenerContainer
        internal set
    public lateinit var bubbleHelper: MessageListView.BubbleHelper
        internal set

    public open fun getAttachmentViewType(
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

    public open fun createAttachmentViewHolder(
        parent: ViewGroup,
        viewType: Int,
        style: MessageListViewStyle,
        messageItem: MessageListItem.MessageItem
    ): BaseAttachmentViewHolder {
        return when (viewType) {
            VIDEO_ATTACHMENT, IMAGE_ATTACHMENT ->
                AttachmentViewHolderMedia(
                    parent,
                    style,
                    bubbleHelper,
                    messageItem,
                    listenerContainer.giphySendListener,
                    listenerContainer.attachmentClickListener,
                    listenerContainer.messageLongClickListener
                )
            FILE_ATTACHMENT ->
                AttachmentViewHolderFile(
                    parent,
                    style,
                    bubbleHelper,
                    messageItem,
                    listenerContainer.attachmentClickListener,
                    listenerContainer.messageLongClickListener
                )
            else ->
                AttachmentViewHolder(
                    parent,
                    style,
                    bubbleHelper,
                    messageItem,
                    listenerContainer.attachmentClickListener,
                    listenerContainer.messageLongClickListener
                )
        }
    }
}
