package io.getstream.chat.android.compose.ui.components.composer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.compose.state.messages.composer.MessageComposerState
import io.getstream.chat.android.compose.ui.components.messages.QuotedMessage
import io.getstream.chat.android.compose.ui.messages.composer.DefaultComposerLabel

/**
 * The default number of lines allowed in the input. The message input will become scrollable after
 * this threshold is exceeded.
 */
private const val DEFAULT_MESSAGE_INPUT_MAX_LINES = 6

/**
 * Input field for the Messages/Conversation screen. Allows label customization, as well as handlers
 * when the input changes.
 *
 * @param messageComposerState The state of the input.
 * @param onValueChange Handler when the value changes.
 * @param onAttachmentRemoved Handler when the user removes a selected attachment.
 * @param modifier Modifier for styling.
 * @param maxLines The number of lines that are allowed in the input.
 * @param label Composable function that represents the label UI, when there's no input/focus.
 */
@Composable
public fun MessageInput(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = DEFAULT_MESSAGE_INPUT_MAX_LINES,
    label: @Composable () -> Unit = { DefaultComposerLabel() },
) {
    val (value, attachments, activeAction) = messageComposerState

    InputField(
        modifier = modifier,
        value = value,
        maxLines = maxLines,
        onValueChange = onValueChange,
        decorationBox = { innerTextField ->
            Column {
                if (activeAction is Reply) {
                    QuotedMessage(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        message = activeAction.message
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                }

                if (attachments.isNotEmpty() && activeAction !is Edit) {
                    MessageInputAttachments(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                        attachments = attachments,
                        onAttachmentRemoved = onAttachmentRemoved
                    )

                    Spacer(modifier = Modifier.size(16.dp))
                }

                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    if (value.isEmpty()) {
                        label()
                    }

                    innerTextField()
                }
            }
        }
    )
}
