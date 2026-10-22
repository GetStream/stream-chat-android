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

package io.getstream.chat.android.ai.compose.conversation

import java.util.UUID

/**
 * A message the person sent that has not arrived in the conversation yet. The conversation shows
 * it at once, and its copy takes over its row when it arrives.
 *
 * @param id The message's id once it arrives, when you know it. Its copy is then found by it rather
 * than by where it lands. Pass it when you can: by where it lands, a copy that arrived before the
 * conversation first showed this message is not found.
 * @param text What the person wrote.
 * @param attachments What they sent with it, such as the photos and files they picked.
 */
public data class AIPendingMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val attachments: List<AIMessageAttachment> = emptyList(),
)
