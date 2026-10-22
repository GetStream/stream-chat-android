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

/**
 * A message the person sent and the replies after it. Messages before the person's first one,
 * such as a greeting, make a turn of their own.
 *
 * @param id The key of the turn's first message.
 * @param messages The turn's messages, oldest first.
 */
internal data class AIMessageTurn<T>(val id: Any, val messages: List<T>)

/**
 * Groups [messages], oldest first, into turns: each message the person sent starts one, and every
 * other message joins the turn before it. The first message starts a turn whoever sent it, and so
 * does each message whose key is in [turnStarts], such as one that was first before older messages
 * arrived above it.
 */
internal fun <T> messageTurns(
    messages: List<T>,
    key: (T) -> Any,
    isFromUser: (T) -> Boolean,
    turnStarts: Set<Any> = emptySet(),
): List<AIMessageTurn<T>> {
    val turns = mutableListOf<AIMessageTurn<T>>()
    var current: MutableList<T>? = null
    for (message in messages) {
        val messageKey = key(message)
        if (current == null || isFromUser(message) || messageKey in turnStarts) {
            current = mutableListOf(message)
            turns += AIMessageTurn(id = messageKey, messages = current)
        } else {
            current += message
        }
    }
    return turns
}
