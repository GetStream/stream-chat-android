package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment

internal object MessageListItemViewTypeMapper {
    fun getViewTypeValue(messageListItem: MessageListItem): Int = listItemToViewType(messageListItem).ordinal

    fun viewTypeValueToViewType(viewTypeValue: Int): MessageListItemViewType {
        return when (viewTypeValue) {
            MessageListItemViewType.DATE_DIVIDER.ordinal -> MessageListItemViewType.DATE_DIVIDER
            MessageListItemViewType.MESSAGE_DELETED.ordinal -> MessageListItemViewType.MESSAGE_DELETED
            MessageListItemViewType.PLAIN_TEXT.ordinal -> MessageListItemViewType.PLAIN_TEXT
            MessageListItemViewType.REPLY_MESSAGE.ordinal -> MessageListItemViewType.REPLY_MESSAGE
            MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS.ordinal -> MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS
            MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS.ordinal -> MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS
            MessageListItemViewType.MEDIA_ATTACHMENTS.ordinal -> MessageListItemViewType.MEDIA_ATTACHMENTS
            MessageListItemViewType.ATTACHMENTS.ordinal -> MessageListItemViewType.ATTACHMENTS
            MessageListItemViewType.LOADING_INDICATOR.ordinal -> MessageListItemViewType.LOADING_INDICATOR
            MessageListItemViewType.THREAD_SEPARATOR.ordinal -> MessageListItemViewType.THREAD_SEPARATOR
            else -> error("View type must be a value from MessageListItemViewType")
        }
    }

    private fun listItemToViewType(messageListItem: MessageListItem): MessageListItemViewType {
        return when (messageListItem) {
            is MessageListItem.DateSeparatorItem -> MessageListItemViewType.DATE_DIVIDER
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
            messageItem.message.text.isNotEmpty() && messageItem.message.attachments.isMedia() -> MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS
            messageItem.message.text.isNotEmpty() && messageItem.message.attachments.isNotEmpty() -> MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS
            messageItem.message.attachments.isMedia() -> MessageListItemViewType.MEDIA_ATTACHMENTS
            messageItem.message.attachments.isNotEmpty() -> MessageListItemViewType.ATTACHMENTS
            /** Here will be additional clause for replay type */
            else -> MessageListItemViewType.PLAIN_TEXT
        }
    }

    internal fun Collection<Attachment>.isMedia(): Boolean = isNotEmpty() && all { it.type == ModelType.attach_image }
}
