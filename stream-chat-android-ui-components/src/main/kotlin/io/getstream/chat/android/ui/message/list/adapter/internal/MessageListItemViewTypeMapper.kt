package io.getstream.chat.android.ui.message.list.adapter.internal

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.common.extensions.isError
import io.getstream.chat.android.ui.common.extensions.isGiphyEphemeral
import io.getstream.chat.android.ui.common.extensions.isSystem
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.ERROR_MESSAGE
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.MESSAGE_DELETED
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.PLAIN_TEXT
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.SYSTEM_MESSAGE
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.TEXT_AND_ATTACHMENTS
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.TYPING_INDICATOR

internal object MessageListItemViewTypeMapper {

    fun getViewTypeValue(messageListItem: MessageListItem): Int = listItemToViewType(messageListItem)

    private fun listItemToViewType(messageListItem: MessageListItem): Int {
        return when (messageListItem) {
            is MessageListItem.DateSeparatorItem -> DATE_DIVIDER
            is MessageListItem.LoadingMoreIndicatorItem -> LOADING_INDICATOR
            is MessageListItem.ThreadSeparatorItem -> THREAD_SEPARATOR
            is MessageListItem.MessageItem -> messageItemToViewType(messageListItem)
            is MessageListItem.TypingItem -> TYPING_INDICATOR
        }
    }

    private fun messageItemToViewType(messageItem: MessageListItem.MessageItem): Int {
        return when {
            messageItem.message.isError() -> ERROR_MESSAGE
            messageItem.message.isSystem() -> SYSTEM_MESSAGE
            messageItem.message.deletedAt != null -> MESSAGE_DELETED
            messageItem.message.isGiphyEphemeral() -> GIPHY
            messageItem.message.attachments.isNotEmpty() -> TEXT_AND_ATTACHMENTS
            else -> PLAIN_TEXT
        }
    }
}
