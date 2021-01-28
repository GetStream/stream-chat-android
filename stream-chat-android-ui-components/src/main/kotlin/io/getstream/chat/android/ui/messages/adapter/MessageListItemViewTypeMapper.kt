package io.getstream.chat.android.ui.messages.adapter

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.MEDIA_ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.MESSAGE_DELETED
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.PLAIN_TEXT
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.TYPING_INDICATOR
import io.getstream.chat.android.ui.utils.extensions.hasLink
import io.getstream.chat.android.ui.utils.extensions.isGiphyEphemeral
import io.getstream.chat.android.ui.utils.extensions.isMedia

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
            messageItem.message.deletedAt != null -> MESSAGE_DELETED
            messageItem.message.isGiphyEphemeral() -> GIPHY
            messageItem.message.isMediaWithText() -> PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS
            messageItem.message.isFileWithText() -> PLAIN_TEXT_WITH_FILE_ATTACHMENTS
            messageItem.message.attachments.isMedia() -> MEDIA_ATTACHMENTS
            messageItem.message.attachments.isAttachmentWithoutLinks() -> ATTACHMENTS
            else -> PLAIN_TEXT
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
