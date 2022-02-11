package io.getstream.chat.android.ui.message.list.adapter.internal

import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.model.ModelType
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.common.extensions.isError
import io.getstream.chat.android.ui.common.extensions.isGiphyEphemeral
import io.getstream.chat.android.ui.common.extensions.isSystem
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.ERROR_MESSAGE
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.FILE_ATTACHMENTS
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.GIPHY_ATTACHMENT
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.MESSAGE_DELETED
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.PLAIN_TEXT
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.SYSTEM_MESSAGE
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.TEXT_AND_ATTACHMENTS
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.THREAD_PLACEHOLDER
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
            is MessageListItem.ThreadPlaceholderItem -> THREAD_PLACEHOLDER
        }
    }

    /**
     * Transforms the given [messageItem] to the type of the message we should show in the list.
     *
     * @param messageItem The message item that holds all the information required to generate a message type.
     * @return The [Int] message type.
     */
    private fun messageItemToViewType(messageItem: MessageListItem.MessageItem): Int {
        val message = messageItem.message

        val (linksAndGiphy, _) = message.attachments.partition { attachment -> attachment.hasLink() }
        val containsGiphy = linksAndGiphy.any { attachment -> attachment.type == ModelType.attach_giphy }
        val hasAttachments = message.attachments.isNotEmpty()

        return when {
            message.isError() -> ERROR_MESSAGE
            message.isSystem() -> SYSTEM_MESSAGE
            hasAttachments -> FILE_ATTACHMENTS
            message.deletedAt != null -> MESSAGE_DELETED
            message.isGiphyEphemeral() -> GIPHY
            containsGiphy -> GIPHY_ATTACHMENT
            message.attachments.isNotEmpty() -> TEXT_AND_ATTACHMENTS
            else -> PLAIN_TEXT
        }
    }
}
