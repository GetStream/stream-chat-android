package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.R
import com.getstream.sdk.chat.adapter.MessageListItem.DateSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle

/**
 * Allows you to easily customize message rendering or message attachment rendering
 */
open class MessageViewHolderFactory {
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

    lateinit var listenerContainer: ListenerContainer
        @JvmName("setListenerContainerInternal")
        internal set
    lateinit var attachmentViewHolderFactory: AttachmentViewHolderFactory
        @JvmName("setAttachmentViewHolderFactoryInternal")
        internal set
    lateinit var bubbleHelper: MessageListView.BubbleHelper
        @JvmName("setBubbleHelperInternal")
        internal set

    open fun getMessageViewType(messageListItem: MessageListItem?): Int {
        return when (messageListItem) {
            is DateSeparatorItem -> MESSAGEITEM_DATE_SEPARATOR
            is TypingItem -> MESSAGEITEM_TYPING
            is MessageItem -> MESSAGEITEM_MESSAGE
            is ThreadSeparatorItem -> MESSAGEITEM_THREAD_SEPARATOR
            else -> MESSAGEITEM_NOT_FOUND
        }
    }

    open fun createMessageViewHolder(
        parent: ViewGroup,
        viewType: Int,
        style: MessageListViewStyle
    ): BaseMessageListItemViewHolder<*> {
        return when (viewType) {
            MESSAGEITEM_DATE_SEPARATOR ->
                DateSeparatorViewHolder(R.layout.stream_item_date_separator, parent, style)
            MESSAGEITEM_MESSAGE ->
                MessageListItemViewHolder(
                    R.layout.stream_item_message,
                    parent,
                    style,
                    attachmentViewHolderFactory,
                    bubbleHelper,
                    listenerContainer.messageClickListener,
                    listenerContainer.messageLongClickListener,
                    listenerContainer.attachmentClickListener,
                    listenerContainer.reactionViewClickListener,
                    listenerContainer.userClickListener,
                    listenerContainer.readStateClickListener,
                    listenerContainer.giphySendListener
                )
            MESSAGEITEM_TYPING ->
                TypingIndicatorViewHolder(R.layout.stream_item_type_indicator, parent, style)
            MESSAGEITEM_THREAD_SEPARATOR ->
                ThreadSeparatorViewHolder(R.layout.stream_item_thread_separator, parent)
            else ->
                throw IllegalArgumentException("Unhandled viewType ($viewType)")
        }
    }

    enum class Position {
        TOP, MIDDLE, BOTTOM
    }
}
