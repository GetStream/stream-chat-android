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
import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.parts.AIMessagePart
import io.getstream.chat.android.ai.compose.parts.AIReasoningPart

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

/**
 * Parameters for [ChatAiComponentFactory.AIConversationUserMessage].
 *
 * @param message The message, which is one of the app's own: cast it to read what else it holds.
 * Null while the message is on its way.
 * @param text What the person wrote.
 * @param attachments What they sent with it, such as images and files.
 * @param modifier The modifier to apply.
 */
public data class AIConversationUserMessageParams(
    val message: AIMessage?,
    val text: String,
    val attachments: List<AIMessageAttachment>,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatAiComponentFactory.AIConversationAssistantMessage].
 *
 * @param message The reply, which is one of the app's own messages: cast it to read what else it
 * holds.
 * @param text The answer, in Markdown.
 * @param isGenerating Whether the agent is still writing it.
 * @param parts The steps the agent took, in order.
 * @param attachments What the agent sent with it, such as images and files.
 * @param reasoningText The whole of a reasoning step's thinking, when the app has it.
 * @param approver Who answers tool calls' questions on this device.
 * @param modifier The modifier to apply.
 */
public data class AIConversationAssistantMessageParams(
    val message: AIMessage,
    val text: String,
    val isGenerating: Boolean,
    val parts: List<AIMessagePart>,
    val attachments: List<AIMessageAttachment>,
    val reasoningText: (AIReasoningPart) -> String?,
    val approver: AIToolApprover?,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatAiComponentFactory.AIConversationReplyActions].
 *
 * @param message The reply, which is one of the app's own messages: cast it to act on it.
 * @param text The reply's text, in Markdown.
 * @param isLatestReply Whether it is the latest reply in the conversation, such as the only one an
 * app offers to regenerate.
 * @param modifier The modifier to apply.
 */
public data class AIConversationReplyActionsParams(
    val message: AIMessage,
    val text: String,
    val isLatestReply: Boolean,
    val modifier: Modifier = Modifier,
)

/**
 * Parameters for [ChatAiComponentFactory.AIConversationAttachment].
 *
 * @param attachment The attachment.
 * @param role Who sent the message that carries it.
 * @param modifier The modifier to apply.
 */
public data class AIConversationAttachmentParams(
    val attachment: AIMessageAttachment,
    val role: AIMessageRole,
    val modifier: Modifier = Modifier,
)
