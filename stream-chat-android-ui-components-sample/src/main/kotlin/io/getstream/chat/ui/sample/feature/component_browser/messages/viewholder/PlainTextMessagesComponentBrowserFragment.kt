package io.getstream.chat.ui.sample.feature.component_browser.messages.viewholder

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.ui.sample.R
import io.getstream.chat.ui.sample.feature.component_browser.utils.drawableResToUri
import java.util.Date

class PlainTextMessagesComponentBrowserFragment : BaseMessagesComponentBrowserFragment() {

    override fun getItems(): List<MessageListItem.MessageItem> {
        val date = Date()
        val attachmentLink = Attachment(
            ogUrl = drawableResToUri(requireContext(), R.drawable.stream_ui_sample_image_1),
            title = "Title",
            text = "Some description",
            authorName = "Stream",
        )
        return listOf(
            MessageListItem.MessageItem(
                message = Message(text = "Lorem ipsum dolor"),
                positions = listOf(MessageListItem.Position.TOP),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."),
                positions = listOf(MessageListItem.Position.MIDDLE),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Ut enim ad minim veniam", createdAt = date),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit", createdAt = date),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = false
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Whaaat?", createdAt = date),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Ephemeral", createdAt = date, type = "ephemeral"),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(text = "Ephemeral", createdAt = date, syncStatus = SyncStatus.FAILED_PERMANENTLY),
                positions = listOf(MessageListItem.Position.TOP, MessageListItem.Position.BOTTOM),
                isMine = true,
            ),
            MessageListItem.MessageItem(
                message = Message(
                    text = "https://www.google.com/",
                    createdAt = date,
                    attachments = mutableListOf(attachmentLink, attachmentLink)
                ),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = true
            ),
            MessageListItem.MessageItem(
                message = Message(
                    text = "https://www.google.com/",
                    createdAt = date,
                    attachments = mutableListOf(attachmentLink)
                ),
                positions = listOf(MessageListItem.Position.BOTTOM),
                isMine = false
            ),
        )
    }
}
