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

package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.ui.common.helper.ReactionDefaults

/**
 * Resolves reaction types to emoji codes and provides the list of available reactions.
 */
public interface ReactionResolver {

    /**
     * All the supported reaction types.
     *
     * Iteration order of this set will be used as reaction display order in default components.
     * If order is important, use an implementation with predictable ordering, like LinkedHashMap
     * (as in [DefaultReactionResolver.defaultEmojiMapping]) or LinkedHashSet.
     */
    public val supportedReactions: Set<String>

    /**
     * Returns the emoji code for the given reaction [type], or null if the type is not supported.
     *
     * @param type The reaction type (e.g. "like", "love", "haha").
     * @return The emoji code for the reaction, or null if unsupported.
     */
    public fun emojiCode(type: String): String?

    public companion object {
        /**
         * Builds the default reaction resolver that provides emoji codes for the default reaction types.
         *
         * @return The default implementation of [ReactionResolver].
         */
        public fun defaultResolver(): ReactionResolver = DefaultReactionResolver()
    }
}

/**
 * Default implementation of [ReactionResolver] that provides emoji codes for based on the emoji
 * mapping passed to the constructor.
 *
 * @param emojiMapping Mapping from reaction type to emoji code
 */
public class DefaultReactionResolver(
    private val emojiMapping: Map<String, String?> = defaultEmojiMapping,
) : ReactionResolver {
    /**
     * Returns the supported reaction types based on the keys in [emojiMapping].
     *
     * Note: this means iteration order depends on the map's iteration order. Implementations like
     * LinkedHashMap (Kotlin's default for [mapOf]) preserve insertion order.
     */
    override val supportedReactions: Set<String> = emojiMapping.keys

    /**
     * @param type The reaction type
     * @return The emoji code corresponding to [type] based on [emojiMapping].
     */
    override fun emojiCode(type: String): String? = emojiMapping[type]

    private companion object {
        // Explicitly use linkedMapOf to make it clear that map iteration follows insertion order
        private val defaultEmojiMapping: Map<String, String> = linkedMapOf(
            ReactionDefaults.THUMBS_UP to "👍",
            ReactionDefaults.LOVE to "❤️",
            ReactionDefaults.LOL to "😂",
            ReactionDefaults.WUT to "😮",
            ReactionDefaults.THUMBS_DOWN to "👎",
        )
    }
}
