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
 * What the agent says it is doing while it answers, besides what its reply shows, such as
 * thinking or checking external sources. Stream Chat's AI indicator events carry it:
 *
 * ```
 * feed.update(activity = AIAgentActivity(AIAgentActivity.State(event.aiState), text = event.aiMessage))
 * ```
 *
 * @param state What the agent is doing.
 * @param text What it says about it, such as "Searching the web", or why it couldn't answer.
 */
public data class AIAgentActivity(
    val state: State,
    val text: String? = null,
) {

    /**
     * What the agent is doing. It is an open set: compare against the states you know. Their raw
     * values are those of Stream Chat's AI indicator.
     *
     * @param rawValue The state's name, such as `AI_STATE_THINKING`.
     */
    @JvmInline
    public value class State(public val rawValue: String) {
        override fun toString(): String = rawValue

        public companion object {
            /** Thinking about its answer. */
            public val Thinking: State = State("AI_STATE_THINKING")

            /** Looking something up, such as on the web or with a tool. */
            public val CheckingExternalSources: State = State("AI_STATE_EXTERNAL_SOURCES")

            /** Writing its answer. */
            public val Generating: State = State("AI_STATE_GENERATING")

            /** It couldn't answer. */
            public val Error: State = State("AI_STATE_ERROR")
        }
    }
}
