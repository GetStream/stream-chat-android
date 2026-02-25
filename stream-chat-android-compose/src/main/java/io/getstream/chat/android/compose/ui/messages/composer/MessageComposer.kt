/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.messages.composer

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.messages.composer.actions.AudioRecordingActions
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.LocalMessageComposerFloatingStyleEnabled
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.SnackbarPopup
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.LinkPreview
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.ValidationError
import io.getstream.chat.android.ui.common.utils.MediaStringUtil

/**
 * Default MessageComposer component that relies on [MessageComposerViewModel] to handle data and
 * communicate various events.
 *
 * @param viewModel The ViewModel that provides pieces of data to show in the composer, like the
 * currently selected integration data or the user input. It also handles sending messages.
 * @param modifier Modifier for styling.
 * @param isAttachmentPickerVisible If the attachment picker is visible or not.
 * @param onSendMessage Handler when the user sends a message. By default it delegates this to the
 * ViewModel, but the user can override if they want more custom behavior.
 * @param onAttachmentsClick Handler for the default Attachments integration.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onLinkPreviewClick Handler when the user taps on a link preview.
 * @param onCancelLinkPreviewClick Handler when the user taps on the cancel link preview.
 * @param onMentionSelected Handler when the user taps on a mention suggestion item.
 * @param onCommandSelected Handler when the user taps on a command suggestion item.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 * @param recordingActions The actions that can be performed on an audio recording.
 * @param headerContent The content shown at the top of the message composer.
 * @param footerContent The content shown at the bottom of the message composer.
 * @param mentionPopupContent Customizable composable that represents the mention suggestions popup.
 * @param commandPopupContent Customizable composable that represents the instant command suggestions popup.
 * @param leadingContent The content shown at the start of the message composer.
 * @param input Customizable composable that represents the input field for the composer, [MessageInput] by default.
 * @param trailingContent Customizable composable that represents the trailing content of the composer, send button
 * by default.
 */
@Composable
public fun MessageComposer(
    viewModel: MessageComposerViewModel,
    modifier: Modifier = Modifier,
    isAttachmentPickerVisible: Boolean = false,
    onSendMessage: (Message) -> Unit = { viewModel.sendMessage(it) },
    onAttachmentsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = { viewModel.setMessageInput(it) },
    onAttachmentRemoved: (Attachment) -> Unit = { viewModel.removeSelectedAttachment(it) },
    onCancelAction: () -> Unit = { viewModel.dismissMessageActions() },
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onCancelLinkPreviewClick: (() -> Unit)? = { viewModel.cancelLinkPreview() },
    onMentionSelected: (User) -> Unit = { viewModel.selectMention(it) },
    onCommandSelected: (Command) -> Unit = { viewModel.selectCommand(it) },
    onAlsoSendToChannelSelected: (Boolean) -> Unit = { viewModel.setAlsoSendToChannel(it) },
    recordingActions: AudioRecordingActions = AudioRecordingActions.defaultActions(
        viewModel = viewModel,
        sendOnComplete = ChatTheme.config.composer.audioRecordingSendOnComplete,
    ),
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerHeaderContent(
                state = it,
                onCancel = onCancelAction,
                onLinkPreviewClick = onLinkPreviewClick,
            )
        }
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerFooterContent(
                state = it,
                onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
            )
        }
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerMentionsPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerCommandsPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    leadingContent: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerLeadingContent(
                state = it,
                isAttachmentPickerVisible = isAttachmentPickerVisible,
                onAttachmentsClick = onAttachmentsClick,
            )
        }
    },
    input: @Composable RowScope.(MessageComposerState) -> Unit = { state ->
        val inputFocusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) {
            viewModel.inputFocusEvents.collect {
                inputFocusRequester.requestFocus()
            }
        }

        with(ChatTheme.componentFactory) {
            MessageComposerInput(
                state = state,
                onInputChanged = onValueChange,
                onAttachmentRemoved = onAttachmentRemoved,
                onLinkPreviewClick = onLinkPreviewClick,
                onCancelLinkPreviewClick = onCancelLinkPreviewClick,
                onCancel = onCancelAction,
                onSendClick = { input, attachments ->
                    val message = viewModel.buildNewMessage(input, attachments)
                    onSendMessage(message)
                },
                recordingActions = recordingActions,
                leadingContent = {
                    ChatTheme.componentFactory.MessageComposerInputLeadingContent(
                        state = state,
                    )
                },
                centerContent = { modifier ->
                    ChatTheme.componentFactory.MessageComposerInputCenterContent(
                        state = state,
                        onValueChange = onValueChange,
                        modifier = modifier.focusRequester(inputFocusRequester),
                    )
                },
                trailingContent = {
                    ChatTheme.componentFactory.MessageComposerInputTrailingContent(
                        state = state,
                        recordingActions = recordingActions,
                        onSendClick = { input, attachments ->
                            val message = viewModel.buildNewMessage(input, attachments)
                            onSendMessage(message)
                        },
                    )
                },
            )
        }
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerTrailingContent(
            state = it,
        )
    },
) {
    val messageComposerState by viewModel.messageComposerState.collectAsState()

    ChatTheme.componentFactory.MessageComposer(
        modifier = modifier,
        isAttachmentPickerVisible = isAttachmentPickerVisible,
        onSendMessage = { text, attachments ->
            val messageWithData = viewModel.buildNewMessage(text, attachments)

            onSendMessage(messageWithData)
        },
        onMentionSelected = onMentionSelected,
        onCommandSelected = onCommandSelected,
        onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        recordingActions = recordingActions,
        headerContent = headerContent,
        footerContent = footerContent,
        mentionPopupContent = mentionPopupContent,
        commandPopupContent = commandPopupContent,
        leadingContent = leadingContent,
        input = input,
        trailingContent = trailingContent,
        messageComposerState = messageComposerState,
        onCancelAction = onCancelAction,
        onAttachmentsClick = onAttachmentsClick,
        onValueChange = onValueChange,
        onAttachmentRemoved = onAttachmentRemoved,
        onLinkPreviewClick = onLinkPreviewClick,
    )
}

/**
 * Clean version of the [MessageComposer] that doesn't rely on ViewModels, so the user can provide a
 * manual way to handle and represent data and various operations.
 *
 * @param messageComposerState The state of the message input.
 * @param onSendMessage Handler when the user wants to send a message.
 * @param modifier Modifier for styling.
 * @param isAttachmentPickerVisible If the attachment picker is visible or not.
 * @param onAttachmentsClick Handler for the default Attachments integration.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onLinkPreviewClick Handler when the user taps on a link preview.
 * @param onCancelLinkPreviewClick Handler when the user taps on the cancel link preview.
 * @param onMentionSelected Handler when the user taps on a mention suggestion item.
 * @param onCommandSelected Handler when the user taps on a command suggestion item.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 * @param recordingActions The actions that can be performed on an audio recording.
 * @param headerContent The content shown at the top of the message composer.
 * @param footerContent The content shown at the bottom of the message composer.
 * @param mentionPopupContent Customizable composable that represents the mention suggestions popup.
 * @param commandPopupContent Customizable composable that represents the instant command suggestions popup.
 * @param leadingContent The content shown at the start of the message composer.
 * @param input Customizable composable that represents the input field for the composer, [MessageInput] by default.
 * @param trailingContent Customizable composable that represents the trailing content of the composer, send button
 * by default.
 */
@Composable
public fun MessageComposer(
    messageComposerState: MessageComposerState,
    onSendMessage: (String, List<Attachment>) -> Unit,
    modifier: Modifier = Modifier,
    isAttachmentPickerVisible: Boolean = false,
    onAttachmentsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = {},
    onAttachmentRemoved: (Attachment) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onLinkPreviewClick: ((LinkPreview) -> Unit)? = null,
    onCancelLinkPreviewClick: (() -> Unit)? = null,
    onMentionSelected: (User) -> Unit = {},
    onCommandSelected: (Command) -> Unit = {},
    onAlsoSendToChannelSelected: (Boolean) -> Unit = {},
    recordingActions: AudioRecordingActions = AudioRecordingActions.None,
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerHeaderContent(
                state = it,
                onCancel = onCancelAction,
                onLinkPreviewClick = onLinkPreviewClick,
            )
        }
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerFooterContent(
                state = it,
                onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
            )
        }
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerMentionsPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        ChatTheme.componentFactory.MessageComposerCommandsPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    leadingContent: @Composable RowScope.(MessageComposerState) -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageComposerLeadingContent(
                state = it,
                isAttachmentPickerVisible = isAttachmentPickerVisible,
                onAttachmentsClick = onAttachmentsClick,
            )
        }
    },
    input: @Composable RowScope.(MessageComposerState) -> Unit = { state ->
        with(ChatTheme.componentFactory) {
            MessageComposerInput(
                state = state,
                onInputChanged = onValueChange,
                onAttachmentRemoved = onAttachmentRemoved,
                onCancel = onCancelAction,
                onLinkPreviewClick = onLinkPreviewClick,
                onCancelLinkPreviewClick = onCancelLinkPreviewClick,
                onSendClick = onSendMessage,
                recordingActions = recordingActions,
                leadingContent = {
                    ChatTheme.componentFactory.MessageComposerInputLeadingContent(
                        state = state,
                    )
                },
                centerContent = { modifier ->
                    ChatTheme.componentFactory.MessageComposerInputCenterContent(
                        state = state,
                        onValueChange = onValueChange,
                        modifier = modifier,
                    )
                },
                trailingContent = {
                    ChatTheme.componentFactory.MessageComposerInputTrailingContent(
                        state = state,
                        recordingActions = recordingActions,
                        onSendClick = onSendMessage,
                    )
                },
            )
        }
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        ChatTheme.componentFactory.MessageComposerTrailingContent(
            state = it,
        )
    },
) {
    val activeAction = messageComposerState.action
    val validationErrors = messageComposerState.validationErrors
    val mentionSuggestions = messageComposerState.mentionSuggestions
    val commandSuggestions = messageComposerState.commandSuggestions
    val snackbarHostState = remember { SnackbarHostState() }

    MessageInputValidationError(
        validationErrors = validationErrors,
        snackbarHostState = snackbarHostState,
    )

    MessageComposerSurface(
        modifier = modifier,
        floatingStyleEnabled = ChatTheme.messageComposerFloatingStyleEnabled,
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            headerContent(messageComposerState)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = StreamTokens.spacingMd,
                        end = StreamTokens.spacingMd,
                        top = if (ChatTheme.messageComposerFloatingStyleEnabled) {
                            0.dp
                        } else {
                            StreamTokens.spacingMd
                        },
                        bottom = StreamTokens.spacingMd,
                    ),
                verticalAlignment = Bottom,
            ) {
                if (activeAction !is Edit) {
                    leadingContent(messageComposerState)
                }

                input(messageComposerState)

                trailingContent(messageComposerState)
            }

            footerContent(messageComposerState)
        }

        if (snackbarHostState.currentSnackbarData != null) {
            SnackbarPopup(hostState = snackbarHostState)
        }

        if (mentionSuggestions.isNotEmpty()) {
            mentionPopupContent(mentionSuggestions)
        }

        if (commandSuggestions.isNotEmpty()) {
            commandPopupContent(commandSuggestions)
        }
    }
}

@Composable
private fun MessageComposerSurface(
    modifier: Modifier,
    floatingStyleEnabled: Boolean,
    content: @Composable () -> Unit,
) {
    if (floatingStyleEnabled) {
        Box(
            modifier = modifier,
        ) {
            content()
        }
    } else {
        Surface(
            modifier = modifier,
            shadowElevation = 24.dp,
            color = ChatTheme.colors.backgroundElevationElevation1,
            content = content,
        )
    }
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
private fun MessageInputValidationError(validationErrors: List<ValidationError>, snackbarHostState: SnackbarHostState) {
    if (validationErrors.isNotEmpty()) {
        val firstValidationError = validationErrors.first()

        val errorMessage = when (firstValidationError) {
            is ValidationError.MessageLengthExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_message_length,
                    firstValidationError.maxMessageLength,
                )
            }

            is ValidationError.AttachmentCountExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_attachment_count,
                    firstValidationError.maxAttachmentCount,
                )
            }

            is ValidationError.AttachmentSizeExceeded -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_file_size,
                    MediaStringUtil.convertFileSizeByteCount(firstValidationError.maxAttachmentSize),
                )
            }

            is ValidationError.ContainsLinksWhenNotAllowed -> {
                stringResource(
                    R.string.stream_compose_message_composer_error_sending_links_not_allowed,
                )
            }
        }

        val context = LocalContext.current
        LaunchedEffect(validationErrors.size) {
            if (firstValidationError is ValidationError.ContainsLinksWhenNotAllowed ||
                firstValidationError is ValidationError.AttachmentSizeExceeded
            ) {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = context.getString(R.string.stream_compose_ok),
                    duration = SnackbarDuration.Indefinite,
                )
            } else {
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Preview
@Composable
private fun MessageComposerDefaultStylePreview() {
    ChatTheme {
        MessageComposerDefaultStyle()
    }
}

@Composable
internal fun MessageComposerDefaultStyle() {
    MessageComposer(
        messageComposerState = PreviewMessageComposerState,
        onSendMessage = { _, _ -> },
    )
}

@Preview
@Composable
private fun MessageComposerDefaultStyleWithVisibleAttachmentPickerPreview() {
    ChatTheme {
        MessageComposerDefaultStyleWithVisibleAttachmentPicker()
    }
}

@Composable
internal fun MessageComposerDefaultStyleWithVisibleAttachmentPicker() {
    MessageComposer(
        messageComposerState = PreviewMessageComposerState,
        isAttachmentPickerVisible = true,
        onSendMessage = { _, _ -> },
    )
}

@Preview(showBackground = true)
@Composable
private fun MessageComposerFloatingStylePreview() {
    ChatTheme {
        MessageComposerFloatingStyle()
    }
}

@Composable
internal fun MessageComposerFloatingStyle() {
    CompositionLocalProvider(LocalMessageComposerFloatingStyleEnabled provides true) {
        MessageComposer(
            messageComposerState = PreviewMessageComposerState,
            onSendMessage = { _, _ -> },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageComposerFloatingStyleWithVisibleAttachmentPickerPreview() {
    ChatTheme {
        MessageComposerFloatingStyleWithVisibleAttachmentPicker()
    }
}

@Composable
internal fun MessageComposerFloatingStyleWithVisibleAttachmentPicker() {
    CompositionLocalProvider(LocalMessageComposerFloatingStyleEnabled provides true) {
        MessageComposer(
            messageComposerState = PreviewMessageComposerState,
            isAttachmentPickerVisible = true,
            onSendMessage = { _, _ -> },
        )
    }
}

private val PreviewMessageComposerState = MessageComposerState(
    ownCapabilities = ChannelCapabilities.toSet(),
    hasCommands = true,
)
