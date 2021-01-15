package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.utils.extensions.hasLink
import io.getstream.chat.android.ui.utils.extensions.isGiphyEphemeral
import io.getstream.chat.android.ui.utils.extensions.isMedia

internal object MessageListItemViewTypeMapper {

    fun getViewTypeValue(messageListItem: MessageListItem): Int = listItemToViewType(messageListItem)

    private fun listItemToViewType(messageListItem: MessageListItem): Int {
        return when (messageListItem) {
            is MessageListItem.DateSeparatorItem -> MessageListItemViewType.DATE_DIVIDER
            is MessageListItem.LoadingMoreIndicatorItem -> MessageListItemViewType.LOADING_INDICATOR
            is MessageListItem.ThreadSeparatorItem -> MessageListItemViewType.THREAD_SEPARATOR
            is MessageListItem.MessageItem -> messageItemToViewType(messageListItem)
            is MessageListItem.TypingItem -> MessageListItemViewType.TYPING_INDICATOR
        }
    }

    private fun messageItemToViewType(messageItem: MessageListItem.MessageItem): Int {
        return when {
            messageItem.message.deletedAt != null -> MessageListItemViewType.MESSAGE_DELETED
            messageItem.message.isGiphyEphemeral() -> MessageListItemViewType.GIPHY
            messageItem.message.isMediaWithText() -> MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS
            messageItem.message.isFileWithText() -> MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS
            messageItem.message.attachments.isMedia() -> MessageListItemViewType.MEDIA_ATTACHMENTS
            messageItem.message.attachments.isAttachmentWithoutLinks() -> MessageListItemViewType.ATTACHMENTS
            /** Here will be additional clause for replay type */
            else -> MessageListItemViewType.PLAIN_TEXT
        }
    }

    internal fun Collection<Attachment>.isMedia(): Boolean = isNotEmpty() && all { it.isMedia() && it.hasLink().not() }

    private fun Collection<Attachment>.isMediaOrLink(): Boolean = isNotEmpty() && all { it.isMedia() || it.hasLink() }

    private fun Message.isMediaWithText(): Boolean {
        return text.isNotEmpty() && attachments.isMediaOrLink() && attachments.any { it.hasLink().not() }
    }

    private fun Message.isFileWithText(): Boolean = text.isNotEmpty() && attachments.any { it.hasLink().not() }

    private fun Collection<Attachment>.isAttachmentWithoutLinks(): Boolean = isNotEmpty() && all { it.hasLink().not() }
}
