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

/**
 * The agent couldn't answer: it said so, such as with an [AIAgentActivity] in the
 * [AIAgentActivity.State.Error] state, or its reply didn't start in time.
 *
 * @param message What the agent said about it, if anything. Show a message of your own when it is
 * null or empty.
 */
public class AIAgentError(message: String? = null) : Exception(message) {
    override fun equals(other: Any?): Boolean = other is AIAgentError && other.message == message

    override fun hashCode(): Int = message.hashCode()

    override fun toString(): String = "AIAgentError(message=$message)"
}
