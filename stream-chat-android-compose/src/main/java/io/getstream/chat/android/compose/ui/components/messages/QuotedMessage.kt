package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.compose.ui.attachments.content.MessageAttachmentsContent
import io.getstream.chat.android.compose.ui.components.avatar.Avatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Wraps the quoted message into a special component, that doesn't show some information, like
 * the timestamp, thread participants and similar.
 *
 * @param message Message to show.
 * @param modifier Modifier for styling.
 * @param onLongItemClick Handler when the item is long clicked.
 */
@Composable
public fun QuotedMessage(
    message: Message,
    modifier: Modifier = Modifier,
    onLongItemClick: (Message) -> Unit,
) {
    val user = message.user

    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Avatar(
            modifier = Modifier.size(24.dp),
            imageUrl = user.image,
            initials = user.initials,
            textStyle = ChatTheme.typography.captionBold,
        )

        Spacer(modifier = Modifier.size(8.dp))

        MessageBubble(
            shape = ChatTheme.shapes.otherMessageBubble, color = ChatTheme.colors.barsBackground,
            content = {
                Column {
                    MessageAttachmentsContent(
                        message = message,
                        onLongItemClick = {}
                    )

                    if (message.text.isNotEmpty()) {
                        MessageText(
                            message = message,
                            onLongItemClick = onLongItemClick
                        )
                    }
                }
            }
        )
    }
}
