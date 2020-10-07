package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem.DateSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import com.getstream.sdk.chat.adapter.viewholder.message.BaseMessageListItemViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.DateSeparatorViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.MessageListItemViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.ThreadSeparatorViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.TypingIndicatorViewHolder
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

/**
 * Allows you to easily customize message rendering
 */
open class MessageViewHolderFactory {
    companion object {
        const val MESSAGEITEM_DATE_SEPARATOR = 1
        const val MESSAGEITEM_MESSAGE = 2
        const val MESSAGEITEM_TYPING = 3
        const val MESSAGEITEM_THREAD_SEPARATOR = 4
        const val MESSAGEITEM_NOT_FOUND = 5
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
        style: MessageListViewStyle,
        channel: Channel
    ): BaseMessageListItemViewHolder<*> {
        return when (viewType) {
            MESSAGEITEM_DATE_SEPARATOR ->
                DateSeparatorViewHolder(
                    parent,
                    style
                )
            MESSAGEITEM_MESSAGE ->
                MessageListItemViewHolder(
                    parent,
                    style,
                    channel,
                    attachmentViewHolderFactory,
                    bubbleHelper,
                    listenerContainer.messageClickListener,
                    listenerContainer.messageLongClickListener,
                    listenerContainer.messageRetryListener,
                    listenerContainer.reactionViewClickListener,
                    listenerContainer.userClickListener,
                    listenerContainer.readStateClickListener
                )
            MESSAGEITEM_TYPING ->
                TypingIndicatorViewHolder(parent, style)
            MESSAGEITEM_THREAD_SEPARATOR ->
                ThreadSeparatorViewHolder(parent)
            else ->
                throw IllegalArgumentException("Unhandled viewType ($viewType)")
        }
    }

    enum class Position {
        TOP, MIDDLE, BOTTOM
    }
}
