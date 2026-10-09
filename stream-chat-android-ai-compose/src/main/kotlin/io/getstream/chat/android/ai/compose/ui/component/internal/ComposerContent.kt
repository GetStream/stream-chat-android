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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.ComposerInputContentParams
import io.getstream.chat.android.ai.compose.ui.component.ComposerInputTrailingContentParams
import io.getstream.chat.android.ai.compose.ui.component.ComposerLeadingContentParams
import io.getstream.chat.android.ai.compose.ui.component.LocalChatAiComponentFactory
import io.getstream.chat.android.ai.compose.ui.component.SpeechToTextButtonState
import io.getstream.chat.android.ai.compose.ui.component.rememberSpeechToTextButtonState

/**
 * Default implementation of the leading content of the chat composer.
 *
 * Renders the button that opens the system photo picker.
 */
@Composable
internal fun DefaultComposerLeadingContent(params: ComposerLeadingContentParams) {
    OutlinedIconButton(
        enabled = !params.isGenerating,
        onClick = params.onAttachmentsClick,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_ai_compose_ic_add),
            contentDescription = stringResource(R.string.stream_ai_compose_composer_add_attachments_button),
        )
    }
}

/**
 * Default implementation of the input content of the chat composer.
 *
 * Renders the text field together with the speech-to-text and send/stop controls.
 */
@Suppress("LongMethod")
@Composable
internal fun DefaultComposerInputContent(
    modifier: Modifier,
    params: ComposerInputContentParams,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val speechToTextState = rememberDictationState(params.messageData.text, params.onTextChange)

    val trailingButton = when {
        params.isGenerating -> ComposerTrailingButton.Stop
        params.messageData.text.isNotBlank() && !speechToTextState.isRecording() -> ComposerTrailingButton.Send
        else -> null
    }

    // Keeps the person's selection while they type; text set from outside puts the cursor at the end.
    var fieldValue by remember { mutableStateOf(TextFieldValue()) }
    val text = params.messageData.text
    val value = if (fieldValue.text == text) fieldValue else TextFieldValue(text, TextRange(text.length))

    val interactionSource = remember { MutableInteractionSource() }
    val componentFactory = LocalChatAiComponentFactory.current
    val trailingParams = ComposerInputTrailingContentParams(
        text = params.messageData.text,
        isGenerating = params.isGenerating,
        speechToTextState = speechToTextState,
        onPermissionDenied = rememberPermissionDeniedHandler(snackbarHostState),
    )

    Column(modifier = modifier) {
        SnackbarHost(hostState = snackbarHostState)

        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = LocalMinimumInteractiveComponentSize.current)
                .then(params.focusRequester?.let { Modifier.focusRequester(it) } ?: Modifier),
            value = value,
            onValueChange = {
                fieldValue = it
                if (it.text != text) params.onTextChange(it.text)
            },
            enabled = !params.isGenerating && !speechToTextState.isRecording(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { params.onSendClick() }),
            textStyle = resolveTextFieldStyle(interactionSource, disabled = params.isGenerating),
            cursorBrush = SolidColor(OutlinedTextFieldDefaults.colors().cursorColor),
            interactionSource = interactionSource,
            maxLines = 6,
            minLines = 1,
            decorationBox = { innerTextField ->
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Column {
                        AttachmentList(
                            attachments = params.messageData.attachments,
                            onRemoveAttachment = params.onRemoveAttachment,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            TextInput(
                                modifier = Modifier.weight(1f),
                                text = params.messageData.text,
                                innerTextField = innerTextField,
                            )
                            with(componentFactory) {
                                ComposerInputTrailingContent(trailingParams)
                            }
                            TrailingButton(
                                button = trailingButton,
                                onSendClick = params.onSendClick,
                                onStopClick = params.onStopClick,
                            )
                        }
                    }
                }
            },
        )
    }
}

/**
 * The speech-to-text state of the input field: the transcript is written into the field, after
 * the text that was there when recording started.
 */
@Composable
private fun rememberDictationState(
    text: String,
    onTextChange: (String) -> Unit,
): SpeechToTextButtonState {
    // Remember the text that existed before starting speech recognition
    var textBeforeSpeech by remember { mutableStateOf("") }

    val onTextRecognized = { recognizedText: String ->
        onTextChange(
            if (textBeforeSpeech.isBlank()) {
                recognizedText
            } else {
                "${textBeforeSpeech.trim()} $recognizedText"
            },
        )
    }

    val speechToTextState = rememberSpeechToTextButtonState(
        onPartialResult = onTextRecognized,
        onFinalResult = onTextRecognized,
    )

    // Update textBeforeSpeech when recording starts/stops
    LaunchedEffect(speechToTextState.isRecording()) {
        if (speechToTextState.isRecording()) {
            textBeforeSpeech = text
        }
    }
    return speechToTextState
}

@Composable
private fun resolveTextFieldStyle(
    interactionSource: MutableInteractionSource,
    disabled: Boolean,
): TextStyle {
    val colors = OutlinedTextFieldDefaults.colors()
    val textStyle = LocalTextStyle.current
    val textColor = textStyle.color.takeOrElse {
        val focused = interactionSource.collectIsFocusedAsState().value
        when {
            disabled -> colors.disabledTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    return textStyle.merge(TextStyle(color = textColor))
}

@Composable
private fun AttachmentList(
    attachments: Set<Uri>,
    onRemoveAttachment: (Uri) -> Unit,
) {
    AnimatedContent(targetState = attachments.isNotEmpty()) { visible ->
        if (visible) {
            AttachmentList(
                modifier = Modifier.fillMaxWidth(),
                uris = attachments,
                onRemoveAttachment = onRemoveAttachment,
            )
        }
    }
}

@Composable
private fun TextInput(
    modifier: Modifier,
    text: String,
    innerTextField: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
        ) {
            if (text.isBlank()) {
                Text(
                    text = stringResource(R.string.stream_ai_compose_composer_input_placeholder),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            innerTextField()
        }
    }
}

private enum class ComposerTrailingButton { Send, Stop }

@Composable
private fun TrailingButton(
    button: ComposerTrailingButton?,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    AnimatedContent(targetState = button) { target ->
        when (target) {
            ComposerTrailingButton.Stop -> TrailingIconButton(
                icon = R.drawable.stream_ai_compose_ic_stop,
                contentDescription = stringResource(R.string.stream_ai_compose_composer_stop_button),
                onClick = onStopClick,
            )

            ComposerTrailingButton.Send -> TrailingIconButton(
                icon = R.drawable.stream_ai_compose_ic_send,
                contentDescription = stringResource(R.string.stream_ai_compose_composer_send_button),
                onClick = onSendClick,
            )

            null -> Unit
        }
    }
}

@Composable
private fun TrailingIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    FilledIconButton(onClick = onClick) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
        )
    }
}
