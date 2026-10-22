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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.ai.compose.ui.component.internal.LocalAIThinkingTurn

/**
 * A reply's thinking before any of it arrives: "Thinking…", counting the seconds.
 *
 * [AIConversation] shows it under the latest question while the agent is thinking, counting from
 * when the question was asked, and the reply's first reasoning step takes its place as "Thought for
 * 8s". Anywhere else it counts from when it appears. It looks like the header of
 * [StreamingReasoning] while the model thinks.
 *
 * @param modifier The modifier to apply to the view.
 */
@Composable
public fun AIThinking(modifier: Modifier = Modifier) {
    val turn = LocalAIThinkingTurn.current
    ReasoningView(
        text = "",
        isThinking = true,
        modifier = modifier,
        thinkingSince = turn?.let { it.clock.startedAt(it.turn) },
    )
}
