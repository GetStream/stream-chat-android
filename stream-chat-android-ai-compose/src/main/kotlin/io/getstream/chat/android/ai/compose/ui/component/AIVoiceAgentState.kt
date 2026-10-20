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

/**
 * What a voice agent is doing, which a voice visualization shows as motion.
 *
 * The states differ only in how a visualization moves, never in its colors, and changing the state
 * eases from one motion into the next.
 */
public enum class AIVoiceAgentState {
    /** Waiting to be spoken to: a calm, barely-there shimmer. */
    Idle,

    /** Not ready yet: a loader sweep with a pulsing breath. */
    Connecting,

    /** Hearing somebody: a gentle expand and contract. */
    Listening,

    /** Working on an answer: a slow turn or a travelling wave. */
    Thinking,

    /** Talking: strong motion that ebbs and pauses like speech. */
    Speaking,
}
