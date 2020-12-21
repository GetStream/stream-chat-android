package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.client.models.Attachment

internal object MessageListItemViewTypeMapper {
    private val MEDIA_ATTACHMENT_TYPES: Collection<String> = listOf(ModelType.attach_image, ModelType.attach_giphy)

    fun getViewTypeValue(messageListItem: MessageListItem): Int = listItemToViewType(messageListItem).ordinal

    fun viewTypeValueToViewType(viewTypeValue: Int): MessageListItemViewType {
        return MessageListItemViewType.values().find { it.ordinal == viewTypeValue }
            ?: error("View type must be a value from MessageListItemViewType")
    }

    private fun listItemToViewType(messageListItem: MessageListItem): MessageListItemViewType {
        return when (messageListItem) {
            is MessageListItem.DateSeparatorItem -> MessageListItemViewType.DATE_DIVIDER
            is MessageListItem.LoadingMoreIndicatorItem -> MessageListItemViewType.LOADING_INDICATOR
            is MessageListItem.ThreadSeparatorItem -> MessageListItemViewType.THREAD_SEPARATOR
            is MessageListItem.ReadStateItem -> MessageListItemViewType.READ_STATE
            is MessageListItem.MessageItem -> messageItemToViewType(messageListItem)
            is MessageListItem.TypingItem -> MessageListItemViewType.TYPING_INDICATOR
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

    internal fun Collection<Attachment>.isMedia(): Boolean = isNotEmpty() && all { it.type in MEDIA_ATTACHMENT_TYPES }


}
