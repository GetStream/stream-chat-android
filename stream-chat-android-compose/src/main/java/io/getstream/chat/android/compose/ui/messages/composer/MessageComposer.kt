package io.getstream.chat.android.compose.ui.messages.composer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.getstream.sdk.chat.utils.MediaStringUtil
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.client.models.Command
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.common.state.Edit
import io.getstream.chat.android.common.state.ValidationError
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.composer.MessageInputState
import io.getstream.chat.android.compose.ui.messages.composer.components.DefaultComposerIntegrations
import io.getstream.chat.android.compose.ui.messages.composer.components.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.components.MessageInputOptions
import io.getstream.chat.android.compose.ui.messages.suggestions.commands.CommandSuggestionList
import io.getstream.chat.android.compose.ui.messages.suggestions.mentions.MentionSuggestionList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel

/**
 * Default MessageComposer component that relies on [MessageComposerViewModel] to handle data and
 * communicate various events.
 *
 * @param viewModel The ViewModel that provides pieces of data to show in the composer, like the
 * currently selected integration data or the user input. It also handles sending messages.
 * @param modifier Modifier for styling.
 * @param onSendMessage Handler when the user sends a message. By default it delegates this to the
 * ViewModel, but the user can override if they want more custom behavior.
 * @param onAttachmentsClick Handler for the default Attachments integration.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onMentionClick @param onMentionClick Handler when the user taps on a mention suggestion item.
 * @param mentionPopupContent Customizable composable function that represents the mention suggestions popup.
 * @param integrations A view that represents custom integrations. By default, we provide
 * [DefaultComposerIntegrations], which show Attachments & Giphy, but users can override this with
 * their own integrations, which they need to hook up to their own data providers and UI.
 * @param label The input field label (hint).
 * @param input The input field for the composer, [MessageInput] by default.
 */
@Composable
public fun MessageComposer(
    viewModel: MessageComposerViewModel,
    modifier: Modifier = Modifier,
    onSendMessage: (Message) -> Unit = { viewModel.sendMessage(it) },
    onAttachmentsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = { viewModel.setMessageInput(it) },
    onAttachmentRemoved: (Attachment) -> Unit = { viewModel.removeSelectedAttachment(it) },
    onCancelAction: () -> Unit = { viewModel.dismissMessageActions() },
    onMentionClick: (User) -> Unit = { viewModel.selectMention(it) },
    onCommandClick: (Command) -> Unit = { viewModel.selectCommand(it) },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        DefaultMentionPopupContent(
            mentionSuggestions = it,
            onMentionClick = onMentionClick
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        DefaultCommandPopupContent(
            commandSuggestions = it,
            onCommandClick = onCommandClick
        )
    },
    integrations: @Composable RowScope.() -> Unit = {
        DefaultComposerIntegrations(onAttachmentsClick)
    },
    label: @Composable () -> Unit = { DefaultComposerLabel() },
    input: @Composable RowScope.(MessageInputState) -> Unit = { inputState ->
        MessageInput(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .weight(1f),
            label = label,
            messageInputState = inputState,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved
        )
    },
) {
    val value by viewModel.input.collectAsState()
    val selectedAttachments by viewModel.selectedAttachments.collectAsState()
    val activeAction by viewModel.lastActiveAction.collectAsState(null)
    val validationErrors by viewModel.validationErrors.collectAsState()
    val mentionSuggestions by viewModel.mentionSuggestions.collectAsState()
    val commandSuggestions by viewModel.commandSuggestions.collectAsState()
    val cooldownTimer by viewModel.cooldownTimer.collectAsState()

    MessageComposer(
        modifier = modifier,
        onSendMessage = { text, attachments ->
            val messageWithData = viewModel.buildNewMessage(text, attachments)

            onSendMessage(messageWithData)
        },
        onMentionClick = onMentionClick,
        onCommandClick = onCommandClick,
        mentionPopupContent = mentionPopupContent,
        commandPopupContent = commandPopupContent,
        integrations = integrations,
        input = input,
        messageInputState = MessageInputState(
            inputValue = value,
            attachments = selectedAttachments,
            action = activeAction,
            validationErrors = validationErrors,
            mentionSuggestions = mentionSuggestions,
            commandSuggestions = commandSuggestions,
            cooldownTimer = cooldownTimer,
        ),
        shouldShowIntegrations = true,
        onCancelAction = onCancelAction
    )
}

/**
 * Clean version of the [MessageComposer] that doesn't rely on ViewModels, so the user can provide a
 * manual way to handle and represent data and various operations.
 *
 * @param messageInputState The state of the message input.
 * @param onSendMessage Handler when the user taps on the send message button.
 * @param onCancelAction Handler when the user cancels the active action (Reply or Edit).
 * @param onMentionClick Handler when the user taps on a mention suggestion item.
 * @param mentionPopupContent Customizable composable function that represents the mention suggestions popup.
 * @param modifier Modifier for styling.
 * @param shouldShowIntegrations If we should show or hide integrations.
 * @param integrations Composable that represents integrations for the composer, such as Attachments.
 * @param input Composable that represents the input field in the composer.
 */
@Composable
public fun MessageComposer(
    messageInputState: MessageInputState,
    onSendMessage: (String, List<Attachment>) -> Unit,
    onCancelAction: () -> Unit,
    onMentionClick: (User) -> Unit,
    onCommandClick: (Command) -> Unit,
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        DefaultMentionPopupContent(
            mentionSuggestions = it,
            onMentionClick = onMentionClick
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        DefaultCommandPopupContent(
            commandSuggestions = it,
            onCommandClick = onCommandClick
        )
    },
    modifier: Modifier = Modifier,
    shouldShowIntegrations: Boolean = true,
    integrations: @Composable RowScope.() -> Unit,
    input: @Composable RowScope.(MessageInputState) -> Unit,
) {
    val (value, attachments, activeAction, validationErrors, mentionSuggestions, commandSuggestions, cooldownTimer) = messageInputState

    showValidationErrorIfNecessary(validationErrors)

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
                Modifier.fillMaxWidth(),
                verticalAlignment = Bottom
            ) {

                if (shouldShowIntegrations && activeAction !is Edit) {
                    integrations()
                } else {
                    Spacer(
                        modifier = Modifier.size(16.dp)
                    )
                }

                input(messageInputState)

                if (cooldownTimer > 0) {
                    CooldownIndicator(cooldownTimer = cooldownTimer)
                } else {
                    val isInputValid = (value.isNotEmpty() || attachments.isNotEmpty()) && validationErrors.isEmpty()

                    IconButton(
                        enabled = isInputValid,
                        content = {
                            Icon(
                                painter = painterResource(id = R.drawable.stream_compose_ic_send),
                                contentDescription = stringResource(id = R.string.stream_compose_send_message),
                                tint = if (isInputValid) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis
                            )
                        },
                        onClick = {
                            if (isInputValid) {
                                onSendMessage(value, attachments)
                            }
                        }
                    )
                }
            }
        }

        if (mentionSuggestions.isNotEmpty()) {
            mentionPopupContent(mentionSuggestions)
        }

        if (commandSuggestions.isNotEmpty()) {
            commandPopupContent(commandSuggestions)
        }
    }
}

/**
 * Represent a timer that show the remaining time until the user is allowed to send the next message.
 *
 * @param cooldownTimer The amount of time left until the user is allowed to sent the next message.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun CooldownIndicator(
    cooldownTimer: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .padding(12.dp)
            .background(shape = RoundedCornerShape(24.dp), color = ChatTheme.colors.disabled),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = cooldownTimer.toString(),
            color = Color.White,
            textAlign = TextAlign.Center,
            style = ChatTheme.typography.bodyBold
        )
    }
}

/**
 * Represents the default mention suggestion list popup shown above the message composer.
 *
 * @param mentionSuggestions The list of users that can be used to autocomplete the current mention input.
 * @param onMentionClick Handler when the user taps on a mention suggestion item.
 */
@Composable
internal fun DefaultMentionPopupContent(
    mentionSuggestions: List<User>,
    onMentionClick: (User) -> Unit,
) {
    MentionSuggestionList(
        users = mentionSuggestions,
        onMentionClick = { onMentionClick(it) }
    )
}

/**
 * Represents the default command suggestion list popup shown above the message composer.
 *
 * @param commandSuggestions The list of available commands in the channel.
 * @param onCommandClick Handler when the user taps on a command suggestion item.
 */
@Composable
internal fun DefaultCommandPopupContent(
    commandSuggestions: List<Command>,
    onCommandClick: (Command) -> Unit,
) {
    CommandSuggestionList(
        commands = commandSuggestions,
        onCommandClick = { onCommandClick(it) }
    )
}

/**
 * Default input field label that the user can override in [MessageComposer].
 */
@Composable
internal fun DefaultComposerLabel() {
    Text(
        text = stringResource(id = R.string.stream_compose_message_label),
        color = ChatTheme.colors.textLowEmphasis
    )
}

/**
 * Shows a [Toast] with an error if one of the following constraints are violated:
 *
 * - The message length exceeds the maximum allowed message length.
 * - The number of selected attachments is too big.
 * - At least one of the attachments is too big.
 *
 * @param validationErrors The list of validation errors for the current user input.
 */
@Composable
private fun showValidationErrorIfNecessary(validationErrors: List<ValidationError>) {
    if (validationErrors.isNotEmpty()) {
        val errorMessage = when (val validationError = validationErrors.first()) {
            is ValidationError.MessageLengthExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_message_length,
                    validationError.maxMessageLength
                )
            }
            is ValidationError.AttachmentCountExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_attachment_count,
                    validationError.maxAttachmentCount
                )
            }
            is ValidationError.AttachmentSizeExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_file_size,
                    MediaStringUtil.convertFileSizeByteCount(validationError.maxAttachmentSize)
                )
            }
        }

        val context = LocalContext.current
        LaunchedEffect(validationErrors.size) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
