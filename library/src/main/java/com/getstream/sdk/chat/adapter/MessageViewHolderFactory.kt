package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.DateSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment

/**
 * Allows you to easily customize message rendering or message attachment rendering
 */
open class MessageViewHolderFactory(
    protected val listenerContainer: ListenerContainer
) {
    companion object {
        const val MESSAGEITEM_DATE_SEPARATOR = 1
        const val MESSAGEITEM_MESSAGE = 2
        const val MESSAGEITEM_TYPING = 3
        const val MESSAGEITEM_THREAD_SEPARATOR = 4
        const val MESSAGEITEM_NOT_FOUND = 5

        const val GENERIC_ATTACHMENT = 1
        const val IMAGE_ATTACHMENT = 2
        const val VIDEO_ATTACHMENT = 3
        const val FILE_ATTACHMENT = 4
    }

    open fun getMessageViewType(messageListItem: MessageListItem?): Int {
        return when (messageListItem) {
            is DateSeparatorItem -> MESSAGEITEM_DATE_SEPARATOR
            is TypingItem -> MESSAGEITEM_TYPING
            is MessageItem -> MESSAGEITEM_MESSAGE
            is ThreadSeparatorItem -> MESSAGEITEM_THREAD_SEPARATOR
            else -> MESSAGEITEM_NOT_FOUND
        }
    }

    open fun getAttachmentViewType(
        attachment: Attachment
    ): Int {
        return when (attachment.type) {
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

    open fun createMessageViewHolder(
        adapter: MessageListItemAdapter,
        parent: ViewGroup,
        viewType: Int
    ): BaseMessageListItemViewHolder<*> {
        return when (viewType) {
            MESSAGEITEM_DATE_SEPARATOR ->
                DateSeparatorViewHolder(R.layout.stream_item_date_separator, parent)
            MESSAGEITEM_MESSAGE ->
                MessageListItemViewHolder(
                    R.layout.stream_item_message,
                    parent,
                    listenerContainer
                ).apply {
                    setMessageLongClickListener(adapter.messageLongClickListener)
                    setAttachmentClickListener(adapter.attachmentClickListener)
                    setReactionViewClickListener(adapter.reactionViewClickListener)
                    setUserClickListener(adapter.userClickListener)
                    setReadStateClickListener(adapter.readStateClickListener)
                    setGiphySendListener(adapter.giphySendListener)
                }
            MESSAGEITEM_TYPING ->
                TypingIndicatorViewHolder(R.layout.stream_item_type_indicator, parent)
            MESSAGEITEM_THREAD_SEPARATOR ->
                ThreadSeparatorViewHolder(R.layout.stream_item_thread_separator, parent)
            else ->
                throw IllegalArgumentException("Unhandled viewType ($viewType)")
        }
    }

    open fun createAttachmentViewHolder(
        adapter: AttachmentListItemAdapter,
        parent: ViewGroup,
        viewType: Int
    ): BaseAttachmentViewHolder {
        return when (viewType) {
            VIDEO_ATTACHMENT, IMAGE_ATTACHMENT ->
                AttachmentViewHolderMedia(R.layout.stream_item_attach_media, parent).apply {
                    setGiphySendListener(adapter.giphySendListener)
                }
            FILE_ATTACHMENT ->
                AttachmentViewHolderFile(R.layout.stream_item_attachment_file, parent)
            else ->
                AttachmentViewHolder(R.layout.stream_item_attachment, parent)
        }
    }

    enum class Position {
        TOP, MIDDLE, BOTTOM
    }
}
