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

import android.content.ClipData
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import kotlinx.coroutines.launch

/**
 * A message the person sent: its attachments, then its text in a bubble, at the end of the row.
 * Pressing and holding the bubble offers to copy the text, and to edit it given [onEdit].
 *
 * ```
 * AIUserMessage(text = message.text, onEdit = { composerState.edit(message) })
 * ```
 *
 * Each attachment shows through [ChatAiComponentFactory.AIConversationAttachment], which renders
 * [AIMessageAttachmentItem] by default.
 *
 * @param text What the person wrote. Without it, only the attachments show.
 * @param modifier The modifier to apply to the message.
 * @param attachments What they sent with it, such as images and files.
 * @param onEdit Edits the message, such as by putting its text back in the composer. Without it,
 * the menu offers only to copy.
 */
@Composable
public fun AIUserMessage(
    text: String,
    modifier: Modifier = Modifier,
    attachments: List<AIMessageAttachment> = emptyList(),
    onEdit: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = LeadingInset),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        val factory = LocalChatAiComponentFactory.current
        attachments.forEach { attachment ->
            with(factory) {
                AIConversationAttachment(AIConversationAttachmentParams(attachment, AIMessageRole.User))
            }
        }
        if (text.isNotEmpty()) {
            UserMessageBubble(text = text, onEdit = onEdit)
        }
    }
}

@Composable
private fun UserMessageBubble(text: String, onEdit: (() -> Unit)?) {
    var menuShown by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val actionsLabel = stringResource(R.string.stream_ai_compose_conversation_message_actions)
    Box {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerHigh, BubbleShape)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuShown = true
                        },
                    )
                }
                .semantics {
                    onLongClick(label = actionsLabel) {
                        menuShown = true
                        true
                    }
                }
                .padding(horizontal = 16.dp, vertical = 10.dp),
        )
        DropdownMenu(expanded = menuShown, onDismissRequest = { menuShown = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.stream_ai_compose_conversation_copy)) },
                onClick = {
                    menuShown = false
                    scope.launch { clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(null, text))) }
                },
            )
            if (onEdit != null) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.stream_ai_compose_conversation_message_edit)) },
                    onClick = {
                        menuShown = false
                        onEdit()
                    },
                )
            }
        }
    }
}

/** Keeps the person's messages off the start of the row, so they read as theirs. */
private val LeadingInset = 48.dp

private val BubbleShape = RoundedCornerShape(20.dp)
