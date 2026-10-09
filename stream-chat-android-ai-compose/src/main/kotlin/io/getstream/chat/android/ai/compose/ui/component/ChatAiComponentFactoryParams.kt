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

package io.getstream.chat.android.ai.compose.ui.component

import android.net.Uri
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester

/**
 * Parameters for [ChatAiComponentFactory.ComposerLeadingContent].
 *
 * @param isGenerating Whether the AI is currently generating a response.
 * @param onAttachmentsClick Called when the user requests to add attachments.
 */
public data class ComposerLeadingContentParams(
    val isGenerating: Boolean,
    val onAttachmentsClick: () -> Unit,
)

/**
 * Parameters for [ChatAiComponentFactory.ComposerInputContent].
 *
 * @param messageData The message currently being composed.
 * @param isGenerating Whether the AI is currently generating a response.
 * @param onTextChange Called when the input text changes.
 * @param onRemoveAttachment Called when the user removes an attachment.
 * @param onSendClick Called when the user sends the message.
 * @param onStopClick Called when the user stops AI generation.
 * @param focusRequester The composer's focus requester, when its caller passed one. Attach it to
 * the text field so the caller can put the cursor in it.
 */
@Suppress("LongParameterList") // Everything the input field needs, so a replacement can do the same.
public data class ComposerInputContentParams(
    val messageData: MessageData,
    val isGenerating: Boolean,
    val onTextChange: (String) -> Unit,
    val onRemoveAttachment: (Uri) -> Unit,
    val onSendClick: () -> Unit,
    val onStopClick: () -> Unit,
    val focusRequester: FocusRequester? = null,
)

/**
 * Parameters for [ChatAiComponentFactory.ComposerInputTrailingContent].
 *
 * @param text The text currently in the input field.
 * @param isGenerating Whether the AI is currently generating a response.
 * @param speechToTextState The composer's speech-to-text state. The default content passes it to
 * [SpeechToTextButton], whose transcript the composer writes into the field.
 * @param onPermissionDenied Called when the microphone permission is denied. The composer shows a
 * message that links to the app's settings.
 */
public data class ComposerInputTrailingContentParams(
    val text: String,
    val isGenerating: Boolean,
    val speechToTextState: SpeechToTextButtonState,
    val onPermissionDenied: () -> Unit,
)

/**
 * Parameters for [ChatAiComponentFactory.ComposerTrailingContent].
 *
 * @param isGenerating Whether the AI is currently generating a response.
 */
public data class ComposerTrailingContentParams(
    val isGenerating: Boolean,
)

/**
 * Parameters for [ChatAiComponentFactory.AITypingIndicatorLabel].
 *
 * @param modifier The modifier to apply to the label content.
 */
public data class AITypingIndicatorLabelParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatAiComponentFactory.AITypingIndicatorIndicator].
 *
 * @param modifier The modifier to apply to the indicator content.
 */
public data class AITypingIndicatorIndicatorParams(
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatAiComponentFactory.SpeechToTextButtonIdleContent].
 *
 * @param onClick Called when the user taps the idle content to start recording.
 */
public data class SpeechToTextButtonIdleContentParams(
    val onClick: () -> Unit,
)

/**
 * Parameters for [ChatAiComponentFactory.SpeechToTextButtonRecordingContent].
 *
 * @param onClick Called when the user taps the recording content to stop recording.
 * @param rmsdB The current audio level in decibels, for visualization.
 */
public data class SpeechToTextButtonRecordingContentParams(
    val onClick: () -> Unit,
    val rmsdB: Float,
)

/**
 * Parameters for [ChatAiComponentFactory.AISuggestionsChip].
 *
 * @param text The suggestion text.
 * @param onClick Called when the user taps the chip.
 */
public data class AISuggestionsChipParams(
    val text: String,
    val onClick: () -> Unit,
)
