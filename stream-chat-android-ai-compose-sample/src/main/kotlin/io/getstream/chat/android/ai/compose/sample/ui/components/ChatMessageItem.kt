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

package io.getstream.chat.android.ai.compose.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.sample.presentation.chat.ChatUiState
import io.getstream.chat.android.ai.compose.ui.component.AIMessageParts
import io.getstream.chat.android.ai.compose.ui.component.StreamingText
import io.getstream.chat.android.compose.state.messages.attachments.AttachmentState
import io.getstream.chat.android.compose.ui.attachments.content.MediaAttachmentContent
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Message

/**
 * Displays a single chat message with proper styling based on role.
 * User messages appear on the right with a colored bubble.
 * Assistant messages fill the width without a bubble.
 *
 * @param message The message to display
 * @param modifier Modifier to be applied to the message item
 */
@Composable
public fun ChatMessageItem(
    message: ChatUiState.Message,
    modifier: Modifier = Modifier,
) {
    val isUser = message.role is ChatUiState.Message.Role.User

    SelectionContainer(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        ) {
            when (message.role) {
                ChatUiState.Message.Role.Assistant -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (message.parts.isNotEmpty()) {
                            AIMessageParts(parts = message.parts)
                        }
                        if (message.content.isNotBlank()) {
                            StreamingText(
                                text = message.content,
                                animate = message.isGenerating,
                            )
                        }
                    }
                }

                ChatUiState.Message.Role.User -> {
                    Spacer(modifier = Modifier.weight(SpacerWeight))
                    MessageBubble(modifier = Modifier.weight(BubbleWeight, fill = false)) {
                        Column {
                            if (message.attachments.isNotEmpty()) {
                                ChatTheme {
                                    MediaAttachmentContent(
                                        state = AttachmentState(
                                            message = Message(
                                                text = message.content,
                                                attachments = message.attachments,
                                            ),
                                            isMine = true,
                                        ),
                                    )
                                }
                            }
                            StreamingText(
                                text = message.content,
                                animate = false,
                            )
                        }
                    }
                }

                ChatUiState.Message.Role.Other -> {
                    MessageBubble(modifier = Modifier.weight(BubbleWeight, fill = false)) {
                        StreamingText(
                            text = message.content,
                            animate = false,
                        )
                    }
                    Spacer(modifier = Modifier.weight(SpacerWeight))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        content()
    }
}

private const val BubbleWeight = 0.8f
private const val SpacerWeight = 0.2f
