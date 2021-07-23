package io.getstream.chat.android.compose.ui.messages.composer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.list.MessageAction
import io.getstream.chat.android.compose.ui.messages.composer.components.DefaultComposerIntegrations
import io.getstream.chat.android.compose.ui.messages.composer.components.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.components.MessageInputOptions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel

/**
 * Default MessageComposer component that relies on [MessageComposerViewModel] to handle data and
 * communicate various events.
 *
 * @param viewModel - The ViewModel that provides pieces of data to show in the composer, like the
 * currently selected integration data or the user input. It also handles sending messages.
 * @param modifier - Modifier for styling.
 * @param onSendMessage - Handler when the user sends a message. By default it delegates this to the
 * ViewModel, but the user can override if they want more custom behavior.
 * @param onAttachmentsClick - Handler for the default Attachments integration.
 * @param onCancelAction - Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onValueChange - Handler when the input field value changes.
 * @param onAttachmentRemoved - Handler when the user taps on the cancel/delete attachment action.
 * @param integrations - A view that represents custom integrations. By default, we provide
 * [DefaultComposerIntegrations], which show Attachments & Giphy, but users can override this with
 * their own integrations, which they need to hook up to their own data providers and UI.
 * @param label - The input field label (hint).
 * @param input - The input field for the composer, [MessageInput] by default.
 * */
@Composable
fun MessageComposer(
    viewModel: MessageComposerViewModel,
    modifier: Modifier = Modifier,
    onSendMessage: (String) -> Unit = { viewModel.onSendMessage(it) },
    onAttachmentsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = { viewModel.onInputChange(it) },
    onAttachmentRemoved: (Attachment) -> Unit = { viewModel.removeSelectedAttachment(it) },
    onCancelAction: () -> Unit = { viewModel.onDismissMessageActions() },
    integrations: @Composable RowScope.() -> Unit = {
        DefaultComposerIntegrations(onAttachmentsClick)
    },
    label: @Composable () -> Unit = { DefaultComposerLabel() },
    input: @Composable RowScope.() -> Unit = {
        MessageInput(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = label,
            value = viewModel.input,
            attachments = viewModel.selectedAttachments,
            activeAction = viewModel.activeAction,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved
        )
    },
) {
    MessageComposer(
        modifier = modifier,
        onSendMessage = onSendMessage,
        integrations = integrations,
        input = input,
        value = viewModel.input,
        attachments = viewModel.selectedAttachments,
        activeAction = viewModel.activeAction,
        shouldShowIntegrations = true,
        onCancelAction = onCancelAction
    )
}

/**
 * Clean version of the [MessageComposer] that doesn't rely on ViewModels, so the user can provide a
 * manual way to handle and represent data and various operations.
 *
 * @param value - Current input field value.
 * @param attachments - Currently selected attachments, shown in the composer.
 * @param integrations - Composable that represents integrations for the composer, such as Attachments.
 * @param input - Composable that represents the input field in the composer.
 * @param modifier - Modifier for styling.
 * @param shouldShowIntegrations - If we should show or hide integrations.
 * @param activeAction - Currently active action either Reply or Edit, whichever is last.
 * @param onSendMessage - Handler when the user taps on the send message button.
 * @param onCancelAction - Handler when the user cancels the active action (Reply or Edit).
 * */
@Composable
fun MessageComposer(
    value: String,
    attachments: List<Attachment>,
    integrations: @Composable RowScope.() -> Unit,
    input: @Composable RowScope.() -> Unit,
    activeAction: MessageAction?,
    onSendMessage: (String) -> Unit,
    onCancelAction: () -> Unit,
    modifier: Modifier = Modifier,
    shouldShowIntegrations: Boolean = true,
) {
    Surface(
        modifier = modifier,
        elevation = 4.dp,
        color = ChatTheme.colors.barsBackground,
    ) {
        Column(
            Modifier
                .padding(vertical = 6.dp)
        ) {
            if (activeAction != null) {
                MessageInputOptions(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
                    activeAction = activeAction,
                    onCancelAction = onCancelAction
                )
            }

            Row(
                Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (shouldShowIntegrations) {
                    integrations()
                }

                input()

                IconButton(
                    content = {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = stringResource(id = R.string.send_message),
                            tint = ChatTheme.colors.textLowEmphasis,
                        )
                    },
                    onClick = {
                        if (value.isNotEmpty() || attachments.isNotEmpty()) {
                            onSendMessage(value)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Default input field label that the user can override in [MessageComposer].
 * */
@Composable
internal fun DefaultComposerLabel() {
    Text(
        text = stringResource(id = R.string.message_label),
        color = ChatTheme.colors.textLowEmphasis
    )
}