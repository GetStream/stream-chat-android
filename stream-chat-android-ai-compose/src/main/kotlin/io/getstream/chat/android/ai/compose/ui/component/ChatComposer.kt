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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

/**
 * Chat composer with attach, voice, and send buttons.
 *
 * This composable provides full control over the message state and includes:
 * - Text input field with placeholder
 * - Add button for selecting images
 * - Voice input button with speech-to-text
 * - Send button (shown when text is not empty)
 * - Stop button (shown during AI generating)
 *
 * The rendered components are resolved through [LocalChatAiComponentFactory], so each part can
 * be overridden without replacing the whole composer. See [CompoundChatAiComponentFactory].
 *
 * The composer keeps the message being written itself. To own it instead, for example to fill
 * in text from elsewhere or restore it after a refused send, use the overload that takes
 * `onMessageDataChange`.
 *
 * @param onSendClick Callback invoked when the send button is clicked with the composed message data.
 * @param onStopClick Callback invoked when the stop button is clicked (during AI generation).
 * @param isGenerating Whether the AI is currently generating a response.
 * @param modifier The modifier to be applied to the composer.
 * @param messageData The initial message data to be displayed in the input field.
 * @param focusRequester Attached to the text field, so you can put the cursor in it with
 * [FocusRequester.requestFocus].
 */
@Suppress("LongParameterList") // Both actions, the state, the initial message and the focus requester.
@Composable
public fun ChatComposer(
    onSendClick: (data: MessageData) -> Unit,
    onStopClick: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    messageData: MessageData = MessageData(),
    focusRequester: FocusRequester? = null,
) {
    var state by rememberSaveable(stateSaver = MessageData.Saver) {
        mutableStateOf(messageData)
    }
    ChatComposer(
        messageData = state,
        onMessageDataChange = { state = it },
        onSendClick = onSendClick,
        onStopClick = onStopClick,
        isGenerating = isGenerating,
        modifier = modifier,
        focusRequester = focusRequester,
    )
}

/**
 * The [ChatComposer] signature without `focusRequester`, kept so apps compiled against it keep
 * working.
 *
 * @param onSendClick Callback invoked when the send button is clicked with the composed message data.
 * @param onStopClick Callback invoked when the stop button is clicked (during AI generation).
 * @param isGenerating Whether the AI is currently generating a response.
 * @param modifier The modifier to be applied to the composer.
 * @param messageData The initial message data to be displayed in the input field.
 */
@Deprecated("Kept for binary compatibility.", level = DeprecationLevel.HIDDEN)
@Composable
public fun ChatComposer(
    onSendClick: (data: MessageData) -> Unit,
    onStopClick: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    messageData: MessageData = MessageData(),
) {
    ChatComposer(
        onSendClick = onSendClick,
        onStopClick = onStopClick,
        isGenerating = isGenerating,
        modifier = modifier,
        messageData = messageData,
        focusRequester = null,
    )
}

/**
 * Chat composer with attach, voice, and send buttons, whose message the caller owns.
 *
 * It renders exactly like the composer that keeps its own state, through the same
 * [LocalChatAiComponentFactory] slots, but reads the message from [messageData] and reports every
 * change through [onMessageDataChange]. Use it to put text in the composer from elsewhere (a
 * suggestion, a restored draft) or to keep what was written when a send is refused.
 *
 * ```
 * var message by rememberSaveable(stateSaver = MessageData.Saver) { mutableStateOf(MessageData()) }
 * val focus = remember { FocusRequester() }
 *
 * ChatComposer(
 *     messageData = message,
 *     onMessageDataChange = { message = it },
 *     onSendClick = { sent -> if (!send(sent)) message = sent },
 *     onStopClick = { stop() },
 *     isGenerating = isGenerating,
 *     focusRequester = focus,
 * )
 * ```
 *
 * @param messageData The message being written.
 * @param onMessageDataChange Called with the new message whenever it changes: as the person types
 * or dictates, picks or removes attachments, and with an empty message once it is sent.
 * @param onSendClick Called with the message when the person sends it. The composer reports an empty
 * message through [onMessageDataChange] just before; set the message back, here or later, to keep it.
 * @param onStopClick Called when the stop button is clicked (during AI generation).
 * @param isGenerating Whether the AI is currently generating a response.
 * @param modifier The modifier to be applied to the composer.
 * @param focusRequester Attached to the text field, so you can put the cursor in it with
 * [FocusRequester.requestFocus].
 */
@Suppress("LongParameterList") // The message, its change, both actions and the focus requester.
@Composable
public fun ChatComposer(
    messageData: MessageData,
    onMessageDataChange: (MessageData) -> Unit,
    onSendClick: (data: MessageData) -> Unit,
    onStopClick: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    val handleSendClick = {
        keyboardController?.hide()
        // Clear before reporting, so a caller that restores the message in onSendClick keeps it.
        onMessageDataChange(MessageData())
        onSendClick(messageData)
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickMultipleVisualMedia(),
    ) { uris ->
        onMessageDataChange(messageData.copy(attachments = messageData.attachments + uris))
    }

    val componentFactory = LocalChatAiComponentFactory.current

    Row(
        modifier = modifier
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        with(componentFactory) {
            ComposerLeadingContent(
                ComposerLeadingContentParams(
                    isGenerating = isGenerating,
                    onAttachmentsClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                                maxItems = 3,
                            ),
                        )
                    },
                ),
            )

            ComposerInputContent(
                ComposerInputContentParams(
                    messageData = messageData,
                    isGenerating = isGenerating,
                    onTextChange = { onMessageDataChange(messageData.copy(text = it)) },
                    onRemoveAttachment = {
                        onMessageDataChange(messageData.copy(attachments = messageData.attachments - it))
                    },
                    onSendClick = handleSendClick,
                    onStopClick = onStopClick,
                    focusRequester = focusRequester,
                ),
            )

            ComposerTrailingContent(ComposerTrailingContentParams(isGenerating = isGenerating))
        }
    }
}

/**
 * Data class representing a message composed by the user.
 *
 * @param text The text content of the message.
 * @param attachments The set of attachment URIs to include with the message.
 */
public data class MessageData(
    val text: String = "",
    val attachments: Set<Uri> = emptySet(),
) {
    public companion object {
        /**
         * [Saver] implementation for [MessageData] that converts it to a saveable format. Use it to
         * keep a message you own across configuration changes and process death:
         * `rememberSaveable(stateSaver = MessageData.Saver) { mutableStateOf(MessageData()) }`.
         */
        public val Saver: Saver<MessageData, Any> = Saver(
            save = { messageData ->
                listOf(
                    messageData.text,
                ) + messageData.attachments.map(Uri::toString)
            },
            restore = { restored ->
                val saved = restored as List<*>
                val text = saved.firstOrNull() as? String ?: ""
                val attachmentStrings = saved.drop(1).mapNotNull { it as? String }
                val attachments = attachmentStrings.map(String::toUri).toSet()
                MessageData(text = text, attachments = attachments)
            },
        )
    }
}

@Composable
internal fun ChatComposerEmpty() {
    ChatComposer(
        onSendClick = {},
        onStopClick = {},
        isGenerating = false,
    )
}

@Composable
internal fun ChatComposerFilled() {
    ChatComposer(
        messageData = MessageData(text = "What is Stream Chat?"),
        onSendClick = {},
        onStopClick = {},
        isGenerating = false,
    )
}

@Composable
internal fun ChatComposerLongFilled() {
    ChatComposer(
        messageData = MessageData(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
        onSendClick = {},
        onStopClick = {},
        isGenerating = false,
    )
}

@Composable
internal fun ChatComposerWithAttachments() {
    ChatComposer(
        messageData = MessageData(
            text = "What is Stream Chat?",
            attachments = setOf("1".toUri(), "2".toUri(), "3".toUri()),
        ),
        onSendClick = {},
        onStopClick = {},
        isGenerating = false,
    )
}

@Composable
internal fun ChatComposerWithoutDictation() {
    CompoundChatAiComponentFactory(
        factory = { current ->
            object : ChatAiComponentFactory by current {
                @Composable
                override fun RowScope.ComposerInputTrailingContent(params: ComposerInputTrailingContentParams) {
                    // Render nothing to leave dictation out.
                }
            }
        },
    ) {
        ChatComposer(
            messageData = MessageData(text = "Summarize this conversation"),
            onSendClick = {},
            onStopClick = {},
            isGenerating = false,
        )
    }
}

@Composable
internal fun ChatComposerHoisted() {
    var messageData by remember { mutableStateOf(MessageData(text = "Explain this chart")) }
    ChatComposer(
        messageData = messageData,
        onMessageDataChange = { messageData = it },
        onSendClick = {},
        onStopClick = {},
        isGenerating = false,
    )
}

@Composable
internal fun ChatComposerGenerating() {
    ChatComposer(
        onSendClick = {},
        onStopClick = {},
        isGenerating = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerEmptyPreview() {
    MaterialTheme {
        ChatComposerEmpty()
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerFilledPreview() {
    MaterialTheme {
        ChatComposerFilled()
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerLongFilledPreview() {
    MaterialTheme {
        ChatComposerLongFilled()
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerWithAttachmentsPreview() {
    MaterialTheme {
        ChatComposerWithAttachments()
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerGeneratingPreview() {
    MaterialTheme {
        ChatComposerGenerating()
    }
}
