package io.getstream.chat.android.compose.ui.components.messages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Message

/**
 * The default text message content. It holds the quoted message in case there is one.
 *
 * @param message The message to show.
 * @param modifier Modifier for styling.
 */
@Composable
public fun DefaultMessageContent(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val quotedMessage = message.replyTo

    Column(
        modifier = modifier
    ) {
        if (quotedMessage != null) {
            QuotedMessage(
                modifier = Modifier.padding(8.dp),
                message = quotedMessage
            )
        }
        MessageText(message = message)
    }
}
