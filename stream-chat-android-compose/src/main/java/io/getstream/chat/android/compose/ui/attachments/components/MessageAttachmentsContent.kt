package io.getstream.chat.android.compose.ui.attachments.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.state.messages.items.MessageItem
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.hasLink

/**
 * Represents the content that's shown in message attachments. We decide if we need to show link previews or other
 * attachments.
 *
 * @param messageItem - The message that contains the attachments.
 * @param onLongItemClick - Handler for long item taps on this content.
 * */
@Composable
public fun MessageAttachmentsContent(
    messageItem: MessageItem,
    onLongItemClick: (Message) -> Unit,
) {
    val (message, _) = messageItem

    if (message.attachments.isNotEmpty()) {

        val (links, attachments) = message.attachments.partition { it.hasLink() && it.type != "giphy" }

        val linkFactory = if (links.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(links) }
        } else {
            null
        }

        val attachmentFactory = if (attachments.isNotEmpty()) {
            ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }
        } else {
            null
        }

        val attachmentState = AttachmentState(
            modifier = Modifier.padding(4.dp),
            message = messageItem,
            onLongItemClick = onLongItemClick
        )

        if (attachmentFactory != null) {
            attachmentFactory.content(attachmentState)
        } else if (linkFactory != null) {
            linkFactory.content(attachmentState)
        }
    }
}
