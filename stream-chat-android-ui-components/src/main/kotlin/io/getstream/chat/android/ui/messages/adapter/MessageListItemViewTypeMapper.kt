package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.adapter.MessageListItem

internal object MessageListItemViewTypeMapper {
    fun getViewTypeValue(messageListItem: MessageListItem): Int = listItemToViewType(messageListItem).typeValue

    fun viewTypeValueToViewType(viewTypeValue: Int): MessageListItemViewType {
        return when (viewTypeValue) {
            MessageListItemViewType.DATE_DIVIDER.typeValue -> MessageListItemViewType.DATE_DIVIDER
            MessageListItemViewType.TYPING_INDICATOR.typeValue -> MessageListItemViewType.TYPING_INDICATOR
            MessageListItemViewType.MESSAGE_DELETED.typeValue -> MessageListItemViewType.MESSAGE_DELETED
            MessageListItemViewType.PLAIN_TEXT.typeValue -> MessageListItemViewType.PLAIN_TEXT
            MessageListItemViewType.REPLY_MESSAGE.typeValue -> MessageListItemViewType.REPLY_MESSAGE
            MessageListItemViewType.PLAIN_TEXT_WITH_ATTACHMENTS.typeValue -> MessageListItemViewType.PLAIN_TEXT_WITH_ATTACHMENTS
            MessageListItemViewType.ATTACHMENTS.typeValue -> MessageListItemViewType.ATTACHMENTS
            MessageListItemViewType.LOADING_INDICATOR.typeValue -> MessageListItemViewType.LOADING_INDICATOR
            MessageListItemViewType.THREAD_SEPARATOR.typeValue -> MessageListItemViewType.THREAD_SEPARATOR
            else -> error("View type must be a value from MessageListItemViewType")
        }
    }

    private fun listItemToViewType(messageListItem: MessageListItem): MessageListItemViewType {
        return when (messageListItem) {
            is MessageListItem.DateSeparatorItem -> MessageListItemViewType.DATE_DIVIDER
            is MessageListItem.TypingItem -> MessageListItemViewType.TYPING_INDICATOR
            is MessageListItem.LoadingMoreIndicatorItem -> MessageListItemViewType.LOADING_INDICATOR
            is MessageListItem.ThreadSeparatorItem -> MessageListItemViewType.THREAD_SEPARATOR
            is MessageListItem.ReadStateItem -> TODO("In current design there isn't any example of it")
            is MessageListItem.MessageItem -> messageItemToViewType(messageListItem)
            else -> MessageListItemViewType.PLAIN_TEXT
        }
    }

    private fun messageItemToViewType(messageItem: MessageListItem.MessageItem): MessageListItemViewType {
        return when {
            messageItem.message.deletedAt != null -> MessageListItemViewType.MESSAGE_DELETED
            messageItem.message.text.isNotEmpty() && messageItem.message.attachments.isNotEmpty() -> MessageListItemViewType.PLAIN_TEXT_WITH_ATTACHMENTS
            messageItem.message.attachments.isNotEmpty() -> MessageListItemViewType.ATTACHMENTS
            /** Here will be additional clause for replay type */
            else -> MessageListItemViewType.PLAIN_TEXT
        }
    }
}
