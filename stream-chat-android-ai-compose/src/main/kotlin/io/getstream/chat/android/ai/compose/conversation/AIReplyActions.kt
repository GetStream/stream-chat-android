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

/** How the person rated a reply. */
public enum class AIMessageRating {
    /** A good reply. */
    Positive,

    /** A bad reply. */
    Negative,
}

/**
 * What the person can do with a finished reply besides copying and sharing it.
 *
 * @param rating How the person rated the reply, if they did.
 * @param onRegenerate Asks the agent for a new reply in its place, or null to not offer it.
 * @param onRate Rates the reply, or takes the rating back with null. Null to not offer rating.
 */
public data class AIReplyActions(
    val rating: AIMessageRating? = null,
    val onRegenerate: (() -> Unit)? = null,
    val onRate: ((AIMessageRating?) -> Unit)? = null,
)
