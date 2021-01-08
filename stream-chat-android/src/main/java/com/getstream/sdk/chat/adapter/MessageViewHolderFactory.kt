package com.getstream.sdk.chat.adapter

import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem.DateSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.MessageItem
import com.getstream.sdk.chat.adapter.MessageListItem.ThreadSeparatorItem
import com.getstream.sdk.chat.adapter.MessageListItem.TypingItem
import com.getstream.sdk.chat.adapter.viewholder.message.BaseMessageListItemViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.DateSeparatorViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.LoadingMoreViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.MessageListItemViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.ThreadSeparatorViewHolder
import com.getstream.sdk.chat.adapter.viewholder.message.TypingIndicatorViewHolder
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.view.MessageListView
import com.getstream.sdk.chat.view.MessageListViewStyle
import io.getstream.chat.android.client.models.Channel

/**
 * Allows you to easily customize message rendering
 */
public open class MessageViewHolderFactory {
    public companion object {
        public const val MESSAGEITEM_DATE_SEPARATOR: Int = 1
        public const val MESSAGEITEM_MESSAGE: Int = 2
        public const val MESSAGEITEM_TYPING: Int = 3
        public const val MESSAGEITEM_THREAD_SEPARATOR: Int = 4
        public const val MESSAGEITEM_LOADING_MORE: Int = 5
    }

    public lateinit var listenerContainer: ListenerContainer
        internal set

    public lateinit var attachmentViewHolderFactory: AttachmentViewHolderFactory
        internal set

    public lateinit var bubbleHelper: MessageListView.BubbleHelper
        internal set

    public lateinit var messageDateFormatter: DateFormatter
        internal set

    public open fun getMessageViewType(messageListItem: MessageListItem): Int {
        return when (messageListItem) {
            is DateSeparatorItem -> MESSAGEITEM_DATE_SEPARATOR
            is TypingItem -> MESSAGEITEM_TYPING
            is MessageItem -> MESSAGEITEM_MESSAGE
            is ThreadSeparatorItem -> MESSAGEITEM_THREAD_SEPARATOR
            is MessageListItem.LoadingMoreIndicatorItem -> MESSAGEITEM_LOADING_MORE
        }
    }

    public open fun createMessageViewHolder(
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
                    messageDateFormatter,
                    listenerContainer.messageClickListener,
                    listenerContainer.messageLongClickListener,
                    listenerContainer.messageRetryListener,
                    listenerContainer.reactionViewClickListener,
                    listenerContainer.userClickListener,
                    listenerContainer.readStateClickListener
                )
            MESSAGEITEM_TYPING -> TypingIndicatorViewHolder(parent, style)
            MESSAGEITEM_THREAD_SEPARATOR -> ThreadSeparatorViewHolder(parent)
            MESSAGEITEM_LOADING_MORE -> LoadingMoreViewHolder(parent)
            else -> throw IllegalArgumentException("Unhandled message view type ($viewType)")
        }
    }
}
