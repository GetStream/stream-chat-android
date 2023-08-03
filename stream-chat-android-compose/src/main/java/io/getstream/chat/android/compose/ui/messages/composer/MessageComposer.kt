/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Bottom
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import io.getstream.chat.android.client.errors.extractCause
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.attachments.StatefulStreamMediaRecorder
import io.getstream.chat.android.compose.ui.attachments.audio.RunningWaveForm
import io.getstream.chat.android.compose.ui.components.composer.CoolDownIndicator
import io.getstream.chat.android.compose.ui.components.composer.MessageInput
import io.getstream.chat.android.compose.ui.components.composer.MessageInputOptions
import io.getstream.chat.android.compose.ui.components.suggestions.commands.CommandSuggestionList
import io.getstream.chat.android.compose.ui.components.suggestions.mentions.MentionSuggestionList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.AboveAnchorPopupPositionProvider
import io.getstream.chat.android.compose.ui.util.mirrorRtl
import io.getstream.chat.android.compose.viewmodel.messages.MessageComposerViewModel
import io.getstream.chat.android.models.Attachment
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.state.messages.Edit
import io.getstream.chat.android.ui.common.state.messages.MessageMode
import io.getstream.chat.android.ui.common.state.messages.composer.MessageComposerState
import io.getstream.chat.android.ui.common.state.messages.composer.ValidationError
import io.getstream.chat.android.ui.common.utils.MediaStringUtil
import io.getstream.log.Priority
import io.getstream.log.StreamLog
import io.getstream.log.streamLog
import io.getstream.sdk.chat.audio.recording.MediaRecorderState
import kotlinx.coroutines.launch
import java.util.Date

/**
 * Default MessageComposer component that relies on [MessageComposerViewModel] to handle data and
 * communicate various events.
 *
 * @param viewModel The ViewModel that provides pieces of data to show in the composer, like the
 * currently selected integration data or the user input. It also handles sending messages.
 * @param modifier Modifier for styling.
 * @param statefulStreamMediaRecorder Used for recording audio messages. Passing in null will disable audio recording.
 * @param onSendMessage Handler when the user sends a message. By default it delegates this to the
 * ViewModel, but the user can override if they want more custom behavior.
 * @param onAttachmentsClick Handler for the default Attachments integration.
 * @param onCommandsClick Handler for the default Commands integration.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onMentionSelected Handler when the user taps on a mention suggestion item.
 * @param onCommandSelected Handler when the user taps on a command suggestion item.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 * @param headerContent The content shown at the top of the message composer.
 * @param footerContent The content shown at the bottom of the message composer.
 * @param mentionPopupContent Customizable composable that represents the mention suggestions popup.
 * @param commandPopupContent Customizable composable that represents the instant command suggestions popup.
 * @param integrations A view that represents custom integrations. By default, we provide
 * [DefaultComposerIntegrations], which show Attachments & Giphy, but users can override this with
 * their own integrations, which they need to hook up to their own data providers and UI.
 * @param label Customizable composable that represents the input field label (hint).
 * @param input Customizable composable that represents the input field for the composer, [MessageInput] by default.
 * @param audioRecordingContent Customizable composable used for displaying audio recording information
 * while audio recording is in progress.
 * @param trailingContent Customizable composable that represents the trailing content of the composer, send button
 * by default.
 */
@Composable
public fun MessageComposer(
    viewModel: MessageComposerViewModel,
    modifier: Modifier = Modifier,
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder? = null,
    onSendMessage: (Message) -> Unit = { viewModel.sendMessage(it) },
    onAttachmentsClick: () -> Unit = {},
    onCommandsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = { viewModel.setMessageInput(it) },
    onAttachmentRemoved: (Attachment) -> Unit = { viewModel.removeSelectedAttachment(it) },
    onCancelAction: () -> Unit = { viewModel.dismissMessageActions() },
    onMentionSelected: (User) -> Unit = { viewModel.selectMention(it) },
    onCommandSelected: (Command) -> Unit = { viewModel.selectCommand(it) },
    onAlsoSendToChannelSelected: (Boolean) -> Unit = { viewModel.setAlsoSendToChannel(it) },
    onRecordingSaved: (Attachment) -> Unit = { viewModel.addSelectedAttachments(listOf(it)) },
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerHeaderContent(
            messageComposerState = it,
            onCancelAction = onCancelAction,
        )
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerFooterContent(
            messageComposerState = it,
            onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        )
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        DefaultMentionPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        DefaultCommandPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    integrations: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerIntegrations(
            messageInputState = it,
            onAttachmentsClick = onAttachmentsClick,
            onCommandsClick = onCommandsClick,
            ownCapabilities = it.ownCapabilities,
        )
    },
    label: @Composable (MessageComposerState) -> Unit = { DefaultComposerLabel(it.ownCapabilities) },
    input: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerInputContent(
            messageComposerState = it,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved,
            label = label,
        )
    },
    audioRecordingContent: @Composable RowScope.(StatefulStreamMediaRecorder) -> Unit = {
        DefaultMessageComposerAudioRecordingContent(it)
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        DefaultMessageComposerTrailingContent(
            value = it.inputValue,
            coolDownTime = it.coolDownTime,
            validationErrors = it.validationErrors,
            attachments = it.attachments,
            ownCapabilities = it.ownCapabilities,
            isInEditMode = it.action is Edit,
            onSendMessage = { input, attachments ->
                val message = viewModel.buildNewMessage(input, attachments)

                onSendMessage(message)
            },
            onRecordingSaved = onRecordingSaved,
            statefulStreamMediaRecorder = statefulStreamMediaRecorder,
        )
    },
) {
    val messageComposerState by viewModel.messageComposerState.collectAsState()

    MessageComposer(
        modifier = modifier,
        onSendMessage = { text, attachments ->
            val messageWithData = viewModel.buildNewMessage(text, attachments)

            onSendMessage(messageWithData)
        },
        onMentionSelected = onMentionSelected,
        onCommandSelected = onCommandSelected,
        onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        headerContent = headerContent,
        footerContent = footerContent,
        mentionPopupContent = mentionPopupContent,
        commandPopupContent = commandPopupContent,
        integrations = integrations,
        input = input,
        audioRecordingContent = audioRecordingContent,
        trailingContent = trailingContent,
        messageComposerState = messageComposerState,
        onCancelAction = onCancelAction,
        statefulStreamMediaRecorder = statefulStreamMediaRecorder,
    )
}

/**
 * Clean version of the [MessageComposer] that doesn't rely on ViewModels, so the user can provide a
 * manual way to handle and represent data and various operations.
 *
 * @param messageComposerState The state of the message input.
 * @param onSendMessage Handler when the user wants to send a message.
 * @param modifier Modifier for styling.
 * @param statefulStreamMediaRecorder Used for recording audio messages. Passing in null will disable audio recording.
 * @param onAttachmentsClick Handler for the default Attachments integration.
 * @param onCommandsClick Handler for the default Commands integration.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 * @param onMentionSelected Handler when the user taps on a mention suggestion item.
 * @param onCommandSelected Handler when the user taps on a command suggestion item.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 * @param headerContent The content shown at the top of the message composer.
 * @param footerContent The content shown at the bottom of the message composer.
 * @param mentionPopupContent Customizable composable that represents the mention suggestions popup.
 * @param commandPopupContent Customizable composable that represents the instant command suggestions popup.
 * @param integrations A view that represents custom integrations. By default, we provide
 * [DefaultComposerIntegrations], which show Attachments & Giphy, but users can override this with
 * their own integrations, which they need to hook up to their own data providers and UI.
 * @param label Customizable composable that represents the input field label (hint).
 * @param input Customizable composable that represents the input field for the composer, [MessageInput] by default.
 * @param audioRecordingContent Customizable composable used for displaying audio recording information
 * while audio recording is in progress.
 * @param trailingContent Customizable composable that represents the trailing content of the composer, send button
 * by default.
 */
@Composable
public fun MessageComposer(
    messageComposerState: MessageComposerState,
    onSendMessage: (String, List<Attachment>) -> Unit,
    modifier: Modifier = Modifier,
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder? = null,
    onAttachmentsClick: () -> Unit = {},
    onCommandsClick: () -> Unit = {},
    onValueChange: (String) -> Unit = {},
    onAttachmentRemoved: (Attachment) -> Unit = {},
    onCancelAction: () -> Unit = {},
    onMentionSelected: (User) -> Unit = {},
    onCommandSelected: (Command) -> Unit = {},
    onAlsoSendToChannelSelected: (Boolean) -> Unit = {},
    onRecordingSaved: (Attachment) -> Unit = {},
    headerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerHeaderContent(
            messageComposerState = it,
            onCancelAction = onCancelAction,
        )
    },
    footerContent: @Composable ColumnScope.(MessageComposerState) -> Unit = {
        DefaultMessageComposerFooterContent(
            messageComposerState = it,
            onAlsoSendToChannelSelected = onAlsoSendToChannelSelected,
        )
    },
    mentionPopupContent: @Composable (List<User>) -> Unit = {
        DefaultMentionPopupContent(
            mentionSuggestions = it,
            onMentionSelected = onMentionSelected,
        )
    },
    commandPopupContent: @Composable (List<Command>) -> Unit = {
        DefaultCommandPopupContent(
            commandSuggestions = it,
            onCommandSelected = onCommandSelected,
        )
    },
    integrations: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerIntegrations(
            messageInputState = it,
            onAttachmentsClick = onAttachmentsClick,
            onCommandsClick = onCommandsClick,
            ownCapabilities = messageComposerState.ownCapabilities,
        )
    },
    label: @Composable (MessageComposerState) -> Unit = { DefaultComposerLabel(messageComposerState.ownCapabilities) },
    input: @Composable RowScope.(MessageComposerState) -> Unit = {
        DefaultComposerInputContent(
            messageComposerState = messageComposerState,
            onValueChange = onValueChange,
            onAttachmentRemoved = onAttachmentRemoved,
            label = label,
        )
    },
    audioRecordingContent: @Composable RowScope.(StatefulStreamMediaRecorder) -> Unit = {
        DefaultMessageComposerAudioRecordingContent(it)
    },
    trailingContent: @Composable (MessageComposerState) -> Unit = {
        DefaultMessageComposerTrailingContent(
            value = it.inputValue,
            coolDownTime = it.coolDownTime,
            validationErrors = it.validationErrors,
            attachments = it.attachments,
            onSendMessage = onSendMessage,
            ownCapabilities = messageComposerState.ownCapabilities,
            isInEditMode = it.action is Edit,
            onRecordingSaved = onRecordingSaved,
            statefulStreamMediaRecorder = statefulStreamMediaRecorder,
        )
    },
) {
    val (_, _, activeAction, validationErrors, mentionSuggestions, commandSuggestions) = messageComposerState
    val snackbarHostState = remember { SnackbarHostState() }

    val isRecording = statefulStreamMediaRecorder?.mediaRecorderState?.value

    MessageInputValidationError(
        validationErrors = validationErrors,
        snackbarHostState = snackbarHostState,
    )

    Surface(
        modifier = modifier,
        elevation = 4.dp,
        color = ChatTheme.colors.barsBackground,
    ) {
        Column(Modifier.padding(vertical = 4.dp)) {
            headerContent(messageComposerState)

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Bottom,
            ) {
                if (activeAction !is Edit) {
                    integrations(messageComposerState)
                } else {
                    Spacer(
                        modifier = Modifier.size(16.dp),
                    )
                }

                if (isRecording == MediaRecorderState.RECORDING) {
                    audioRecordingContent(statefulStreamMediaRecorder)
                } else {
                    input(messageComposerState)
                }

                trailingContent(messageComposerState)
            }

            footerContent(messageComposerState)
        }

        if (snackbarHostState.currentSnackbarData != null) {
            SnackbarPopup(snackbarHostState = snackbarHostState)
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
 * Represents the default content shown at the top of the message composer component.
 *
 * @param messageComposerState The state of the message composer.
 * @param onCancelAction Handler for the cancel button on Message actions, such as Edit and Reply.
 */
@Composable
public fun DefaultMessageComposerHeaderContent(
    messageComposerState: MessageComposerState,
    onCancelAction: () -> Unit,
) {
    val activeAction = messageComposerState.action

    if (activeAction != null) {
        MessageInputOptions(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp, start = 8.dp, end = 8.dp),
            activeAction = activeAction,
            onCancelAction = onCancelAction,
        )
    }
}

/**
 * Represents the default content shown at the bottom of the message composer component.
 *
 * @param messageComposerState The state of the message composer.
 * @param onAlsoSendToChannelSelected Handler when the user checks the also send to channel checkbox.
 */
@Composable
public fun DefaultMessageComposerFooterContent(
    messageComposerState: MessageComposerState,
    onAlsoSendToChannelSelected: (Boolean) -> Unit,
) {
    if (messageComposerState.messageMode is MessageMode.MessageThread) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = messageComposerState.alsoSendToChannel,
                onCheckedChange = { onAlsoSendToChannelSelected(it) },
                colors = CheckboxDefaults.colors(ChatTheme.colors.primaryAccent),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = stringResource(R.string.stream_compose_message_composer_show_in_channel),
                color = ChatTheme.colors.textLowEmphasis,
                textAlign = TextAlign.Center,
                style = ChatTheme.typography.body,
            )
        }
    }
}

/**
 * Represents the default mention suggestion list popup shown above the message composer.
 *
 * @param mentionSuggestions The list of users that can be used to autocomplete the current mention input.
 * @param onMentionSelected Handler when the user taps on a mention suggestion item.
 */
@Composable
internal fun DefaultMentionPopupContent(
    mentionSuggestions: List<User>,
    onMentionSelected: (User) -> Unit,
) {
    MentionSuggestionList(
        users = mentionSuggestions,
        onMentionSelected = { onMentionSelected(it) },
    )
}

/**
 * Represents the default command suggestion list popup shown above the message composer.
 *
 * @param commandSuggestions The list of available commands in the channel.
 * @param onCommandSelected Handler when the user taps on a command suggestion item.
 */
@Composable
internal fun DefaultCommandPopupContent(
    commandSuggestions: List<Command>,
    onCommandSelected: (Command) -> Unit,
) {
    CommandSuggestionList(
        commands = commandSuggestions,
        onCommandSelected = { onCommandSelected(it) },
    )
}

/**
 * Composable that represents the message composer integrations (special actions).
 *
 * Currently just shows the Attachment picker action.
 *
 * @param messageInputState The state of the input.
 * @param onAttachmentsClick Handler when the user selects attachments.
 * @param onCommandsClick Handler when the user selects commands.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 */
@Composable
internal fun DefaultComposerIntegrations(
    messageInputState: MessageComposerState,
    onAttachmentsClick: () -> Unit,
    onCommandsClick: () -> Unit,
    ownCapabilities: Set<String>,
) {
    val hasTextInput = messageInputState.inputValue.isNotEmpty()
    val hasAttachments = messageInputState.attachments.isNotEmpty()
    val hasCommandInput = messageInputState.inputValue.startsWith("/")
    val hasCommandSuggestions = messageInputState.commandSuggestions.isNotEmpty()
    val hasMentionSuggestions = messageInputState.mentionSuggestions.isNotEmpty()

    val isAttachmentsButtonEnabled = !hasCommandInput && !hasCommandSuggestions && !hasMentionSuggestions
    val isCommandsButtonEnabled = !hasTextInput && !hasAttachments

    val canSendMessage = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
    val canSendAttachments = ownCapabilities.contains(ChannelCapabilities.UPLOAD_FILE)

    if (canSendMessage) {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (canSendAttachments) {
                IconButton(
                    enabled = isAttachmentsButtonEnabled,
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.stream_compose_ic_attachments),
                            contentDescription = stringResource(id = R.string.stream_compose_attachments),
                            tint = if (isAttachmentsButtonEnabled) {
                                ChatTheme.colors.textLowEmphasis
                            } else {
                                ChatTheme.colors.disabled
                            },
                        )
                    },
                    onClick = onAttachmentsClick,
                )
            }

            val commandsButtonTint = if (hasCommandSuggestions && isCommandsButtonEnabled) {
                ChatTheme.colors.primaryAccent
            } else if (isCommandsButtonEnabled) {
                ChatTheme.colors.textLowEmphasis
            } else {
                ChatTheme.colors.disabled
            }

            AnimatedVisibility(visible = messageInputState.hasCommands) {
                IconButton(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(4.dp),
                    enabled = isCommandsButtonEnabled,
                    content = {
                        Icon(
                            painter = painterResource(id = R.drawable.stream_compose_ic_command),
                            contentDescription = null,
                            tint = commandsButtonTint,
                        )
                    },
                    onClick = onCommandsClick,
                )
            }
        }
    } else {
        Spacer(modifier = Modifier.width(12.dp))
    }
}

/**
 * Default input field label that the user can override in [MessageComposer].
 *
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 */
@Composable
internal fun DefaultComposerLabel(ownCapabilities: Set<String>) {
    val text =
        if (ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)) {
            stringResource(id = R.string.stream_compose_message_label)
        } else {
            stringResource(id = R.string.stream_compose_cannot_send_messages_label)
        }

    Text(
        text = text,
        color = ChatTheme.colors.textLowEmphasis,
    )
}

/**
 * Represents the default input content of the Composer.
 *
 * @param label Customizable composable that represents the input field label (hint).
 * @param messageComposerState The state of the message input.
 * @param onValueChange Handler when the input field value changes.
 * @param onAttachmentRemoved Handler when the user taps on the cancel/delete attachment action.
 */
@Composable
private fun RowScope.DefaultComposerInputContent(
    messageComposerState: MessageComposerState,
    onValueChange: (String) -> Unit,
    onAttachmentRemoved: (Attachment) -> Unit,
    label: @Composable (MessageComposerState) -> Unit,
) {
    MessageInput(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .weight(1f),
        label = label,
        messageComposerState = messageComposerState,
        onValueChange = onValueChange,
        onAttachmentRemoved = onAttachmentRemoved,
    )
}

/**
 * Used to display audio recording information while audio recording is in progress.
 *
 * @param statefulStreamMediaRecorder Used for recording audio messages.
 */
@Composable
internal fun RowScope.DefaultMessageComposerAudioRecordingContent(
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .align(Alignment.CenterVertically)
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .weight(1f),
    ) {
        val amplitudeSample = statefulStreamMediaRecorder.latestMaxAmplitude.value
        val recordingDuration = statefulStreamMediaRecorder.activeRecordingDuration.value

        val recordingDurationFormatted by remember(recordingDuration) {
            derivedStateOf {
                // TODO consider moving to common
                val remainder = recordingDuration % 60_000
                val seconds = String.format("%02d", remainder / 1000)
                val minutes = String.format("%02d", (recordingDuration - remainder) / 60_000)

                "$minutes:$seconds"
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .align(Alignment.CenterVertically),
        ) {
            Icon(
                modifier = Modifier
                    .size(12.dp)
                    .align(Alignment.CenterVertically),
                painter = painterResource(id = R.drawable.stream_compose_ic_circle),
                tint = Color.Red,
                // TODO add later
                contentDescription = null,
            )

            Text(
                modifier = Modifier.align(Alignment.CenterVertically),
                text = recordingDurationFormatted,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        RunningWaveForm(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .fillMaxWidth()
                .height(20.dp),
            maxInputValue = 20_000,
            barWidth = 8.dp,
            barGap = 2.dp,
            restartKey = true,
            newValueKey = amplitudeSample.key,
            latestValue = amplitudeSample.value,
        )
    }
}

/**
 * Represents the default trailing content for the Composer, which represent a send button or a cooldown timer.
 *
 * @param value The input value.
 * @param coolDownTime The amount of time left in cool-down mode.
 * @param attachments The selected attachments.
 * @param validationErrors List of errors for message validation.
 * @param onSendMessage Handler when the user wants to send a message.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * For a full list @see [ChannelCapabilities].
 * @param streamMediaRecorder Used for recording audio messages.
 * For a full list @see [io.getstream.chat.android.client.models.ChannelCapabilities].
 * @param statefulStreamMediaRecorder Used for recording audio messages. Passing in null will disable audio recording.
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
internal fun DefaultMessageComposerTrailingContent(
    value: String,
    coolDownTime: Int,
    attachments: List<Attachment>,
    validationErrors: List<ValidationError>,
    ownCapabilities: Set<String>,
    isInEditMode: Boolean,
    onSendMessage: (String, List<Attachment>) -> Unit,
    onRecordingSaved: (Attachment) -> Unit,
    statefulStreamMediaRecorder: StatefulStreamMediaRecorder?,
) {
    val isSendButtonEnabled = ownCapabilities.contains(ChannelCapabilities.SEND_MESSAGE)
    val isInputValid by lazy { (value.isNotBlank() || attachments.isNotEmpty()) && validationErrors.isEmpty() }
    val sendButtonDescription = stringResource(id = R.string.stream_compose_cd_send_button)
    val recordAudioButtonDescription = stringResource(id = R.string.stream_compose_cd_record_audio_message)
    var permissionsRequested by rememberSaveable { mutableStateOf(false) }

    val isRecording = statefulStreamMediaRecorder?.mediaRecorderState?.value

    // TODO test permissions on lower APIs etc
    val storageAndRecordingPermissionState = rememberMultiplePermissionsState(
        permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_AUDIO,
                Manifest.permission.RECORD_AUDIO,
            )
        } else {
            listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )
        },
    ) {
        // TODO should we track this or always ask?
        permissionsRequested = true
    }
    val coroutineScope = rememberCoroutineScope()

    val context = LocalContext.current

    if (coolDownTime > 0 && !isInEditMode) {
        CoolDownIndicator(coolDownTime = coolDownTime)
    } else {
        Row(
            modifier = Modifier
                .height(44.dp)
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // TODO don't show if the own ability to send attachments isn't given
            if (statefulStreamMediaRecorder != null) {
                Box(
                    modifier = Modifier
                        .semantics { contentDescription = recordAudioButtonDescription }
                        .size(32.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = {
                                    /**
                                     * An internal function used to handle audio recording. It initiates the recording
                                     * and stops and saves the file once the appropriate gesture has been completed.
                                     */
                                    fun handleAudioRecording() = coroutineScope.launch {
                                        awaitPointerEventScope {
                                            statefulStreamMediaRecorder.startAudioRecording(
                                                context = context,
                                                recordingName = "audio_recording_${Date()}",
                                            )

                                            while (true) {
                                                val event = awaitPointerEvent(PointerEventPass.Main)

                                                if (event.changes.all { it.changedToUp() }) {
                                                    statefulStreamMediaRecorder.stopRecording()
                                                        .onSuccess {
                                                            StreamLog.i("MessageComposer") {
                                                                "[onRecordingSaved] attachment: $it"
                                                            }
                                                            onRecordingSaved(it.attachment)
                                                        }
                                                        .onError {
                                                            streamLog(throwable = it.extractCause()) {
                                                                "Could not save audio recording: ${it.message}"
                                                            }
                                                        }
                                                    break
                                                }
                                            }
                                        }
                                    }

                                    when {
                                        !storageAndRecordingPermissionState.allPermissionsGranted -> {
                                            storageAndRecordingPermissionState.launchMultiplePermissionRequest()
                                        }
                                        isRecording == MediaRecorderState.UNINITIALIZED -> {
                                            handleAudioRecording()
                                        }
                                        else -> streamLog(Priority.ERROR) { "Could not start audio recording" }
                                    }
                                },
                            )
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    val layoutDirection = LocalLayoutDirection.current

                    Icon(
                        modifier = Modifier.mirrorRtl(layoutDirection = layoutDirection),
                        painter = painterResource(id = R.drawable.stream_compose_ic_mic_active),
                        contentDescription = stringResource(id = R.string.stream_compose_record_audio_message),
                        // TODO disable if max attachments are reached
                        tint = if (isRecording == MediaRecorderState.RECORDING) {
                            ChatTheme.colors.primaryAccent
                        } else {
                            ChatTheme.colors.textLowEmphasis
                        },
                    )
                }
            }
        }

        IconButton(
            modifier = Modifier
                .semantics { contentDescription = sendButtonDescription },
            enabled = isSendButtonEnabled && isInputValid,
            content = {
                val layoutDirection = LocalLayoutDirection.current

                Icon(
                    modifier = Modifier.mirrorRtl(layoutDirection = layoutDirection),
                    painter = painterResource(id = R.drawable.stream_compose_ic_send),
                    contentDescription = stringResource(id = R.string.stream_compose_send_message),
                    tint = if (isInputValid) ChatTheme.colors.primaryAccent else ChatTheme.colors.textLowEmphasis,
                )
            },
            onClick = {
                if (isInputValid) {
                    onSendMessage(value, attachments)
                }
            },
        )
    }

    // TODO release recorder after the composable moves of screen
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

/**
 * A snackbar wrapped inside of a popup allowing it be
 * displayed above the Composable it's anchored to.
 *
 * @param snackbarHostState The state of the snackbar host. Contains
 * the snackbar data necessary to display the snackbar.
 */
@Composable
private fun SnackbarPopup(snackbarHostState: SnackbarHostState) {
    Popup(popupPositionProvider = AboveAnchorPopupPositionProvider()) {
        SnackbarHost(hostState = snackbarHostState)
    }
}
