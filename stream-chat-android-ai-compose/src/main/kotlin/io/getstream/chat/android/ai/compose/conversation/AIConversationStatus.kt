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
 * Where an [AIConversationState]'s latest exchange with the agent is. It is an open set: compare
 * against the statuses you know.
 *
 * @param rawValue The status's name, such as `ready`.
 */
@JvmInline
public value class AIConversationStatus(public val rawValue: String) {

    /** Whether the agent is answering: the request is [Submitted], or its reply [Streaming]. */
    public val isAnswering: Boolean
        get() = this == Submitted || this == Streaming

    override fun toString(): String = rawValue

    public companion object {
        /** Nothing is on its way: the agent answered, or the person stopped it. */
        public val Ready: AIConversationStatus = AIConversationStatus("ready")

        /**
         * The person's request is on its way, or the agent is working on it and its reply shows
         * nothing yet.
         */
        public val Submitted: AIConversationStatus = AIConversationStatus("submitted")

        /** The agent is writing its reply. */
        public val Streaming: AIConversationStatus = AIConversationStatus("streaming")

        /**
         * The latest request failed, the agent couldn't answer it, or older messages couldn't load.
         * [AIConversationState.error] says why, and [AIConversationState.retry] tries again.
         */
        public val Error: AIConversationStatus = AIConversationStatus("error")
    }
}
