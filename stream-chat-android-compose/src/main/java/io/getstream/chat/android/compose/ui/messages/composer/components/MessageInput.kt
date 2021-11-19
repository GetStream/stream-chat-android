package io.getstream.chat.android.compose.ui.messages.composer.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.MessageAction
import io.getstream.chat.android.common.state.Reply
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.composer.MessageInputState
import io.getstream.chat.android.compose.ui.common.InputField
import io.getstream.chat.android.compose.ui.messages.composer.DefaultComposerLabel
import io.getstream.chat.android.compose.ui.messages.list.QuotedMessage
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the options "header" for the message input component. This is based on the currently active
 * message action - [Reply] or [Edit].
 *
 * @param modifier Modifier for styling.
 * @param activeAction Currently active [MessageAction].
 * @param onCancelAction Handler when the user cancels the current action.
 */
@Composable
public fun MessageInputOptions(
    activeAction: MessageAction,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val optionImage = if (activeAction is Reply) Icons.Default.Reply else Icons.Default.Edit
    val title = stringResource(
        id = if (activeAction is Reply) R.string.stream_compose_reply_to_message else R.string.stream_compose_edit_message
    )

    Row(
        modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            modifier = Modifier.padding(4.dp),
            imageVector = optionImage,
            contentDescription = null,
            tint = ChatTheme.colors.textLowEmphasis,
        )

        Text(
            text = title,
            style = ChatTheme.typography.bodyBold,
            color = ChatTheme.colors.textHighEmphasis,
        )

        Icon(
            modifier = Modifier
                .padding(4.dp)
                .clickable(
                    onClick = onCancelAction,
                    indication = rememberRipple(bounded = false),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            imageVector = Icons.Default.Cancel,
            contentDescription = stringResource(id = R.string.stream_compose_cancel),
            tint = ChatTheme.colors.textLowEmphasis,
        )
    }
}

/**
 * Input field for the Messages/Conversation screen. Allows label customization, as well as handlers
 * when the input changes.
 *
 * @param messageInputState The state of the input.
 * @param onValueChange Handler when the value changes.
 * @param onAttachmentRemoved Handler when the user removes a selected attachment.
 * @param modifier Modifier for styling.
 * @param label Composable function that represents the label UI, when there's no input/focus.
 */
@Composable
public fun MessageInput(
    messageInputState: MessageInputState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = { DefaultComposerLabel() },
) {
    val (value, attachments, activeAction) = messageInputState

    InputField(
        modifier = modifier,
        value = value,
        maxLines = 6,
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

/**
 * Shows the selected attachments within the composer, based on if they're images or files.
 *
 * @param attachments List of selected attachments.
 * @param onAttachmentRemoved Handler when the user removes a selected attachment.
 * @param modifier Modifier for styling.
 */
@Composable
private fun MessageInputAttachments(
    attachments: List<Attachment>,
    onAttachmentRemoved: (Attachment) -> Unit,
    modifier: Modifier = Modifier,
) {
    val previewFactory = ChatTheme.attachmentFactories.firstOrNull { it.canHandle(attachments) }

    previewFactory?.previewContent?.invoke(modifier, attachments, onAttachmentRemoved)
}
