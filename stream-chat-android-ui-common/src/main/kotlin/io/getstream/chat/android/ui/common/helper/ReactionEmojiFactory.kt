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

package io.getstream.chat.android.ui.common.helper

import io.getstream.chat.android.ui.common.utils.EmojiUtil

/**
 * Factory that creates emojis for message reactions, used both in-app and in push notifications.
 * Example:
 * ```kotlin
 * class CustomReactionEmojiFactory : ReactionEmojiFactory {
 *
 *   override fun emojiCode(type: String): String? {
 *       return when (type) {
 *         "like" -> "👍"
 *         "love" -> null
 *         else -> null
 *       }
 *   }
 * }
 * ```
 *
 * With the above implementation, a "like" reaction will show a thumbs up emoji (👍) both in the message
 * reaction list and in push notifications. A "love" reaction will fall back to displaying the reaction type
 * as text (e.g. `:love:` in push notifications).
 */
public fun interface ReactionEmojiFactory {

    /**
     * Creates an emoji code for the given reaction [type].
     *
     * @param type The type of the reaction (e.g. "like", "love", "haha").
     * @return The emoji code for the reaction, or null if no emoji is available for the given type.
     */
    public fun emojiCode(type: String): String?

    public companion object Companion {
        /**
         * Builds the default reaction emoji factory that provides emoji codes for the default reaction types.
         *
         * @return The default implementation of [ReactionEmojiFactory].
         */
        public fun defaultFactory(): ReactionEmojiFactory = DefaultReactionEmojiFactory()
    }
}

/**
 * Default implementation of [ReactionEmojiFactory] that resolves emoji codes for the built-in reaction types. Types
 * that are already a single emoji are returned as-is. Returns null for
 * unrecognized types.
 */
private class DefaultReactionEmojiFactory : ReactionEmojiFactory {

    override fun emojiCode(type: String): String? = when (type) {
        ReactionDefaults.THUMBS_UP -> "👍"
        ReactionDefaults.LOVE -> "❤️"
        ReactionDefaults.LOL -> "😂"
        ReactionDefaults.WUT -> "😮"
        ReactionDefaults.THUMBS_DOWN -> "👎"
        else -> type.takeIf(EmojiUtil::isSingleEmoji)
    }
}
