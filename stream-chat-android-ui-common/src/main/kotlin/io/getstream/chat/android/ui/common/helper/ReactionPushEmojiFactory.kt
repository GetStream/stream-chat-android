/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

/**
 * Factory that creates emojis to be displayed in the push notifications delivered for message reactions.
 * Example:
 * ```kotlin
 * class CustomPushEmojiFactory : ReactionPushEmojiFactory {
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
 * With the above implementation, a "like" reaction will show a thumbs up emoji in the push notification:
 * `Reacted 👍 to "Your message"`.
 *
 * A "love" reaction will show the reaction type as text instead of an emoji:
 * `Reacted :love: to "Your message"`.
 *
 * Important: To use this feature, you must migrate you project to use Push V3 in the Stream Dashboard.
 *
 * See [Overview](https://getstream.io/chat/docs/android/push_introduction/) and
 * [Legacy Push System](https://getstream.io/chat/docs/android/legacy_push_system/).
 */
public fun interface ReactionPushEmojiFactory {

    /**
     * Creates an emoji code for the given reaction [type].
     *
     * @param type The type of the reaction (e.g. "like", "love", "haha").
     * @return The emoji code to be shown in the delivered push notification for the reaction. If null, the notification
     * will instead show the reaction type as text.
     */
    public fun emojiCode(type: String): String?

    public companion object {
        /**
         * Builds the default reaction push emoji factory that provides emoji codes for the default reaction types.
         *
         * @return The default implementation of [ReactionPushEmojiFactory].
         */
        public fun defaultFactory(): ReactionPushEmojiFactory = DefaultReactionPushEmojiFactory()
    }
}

/**
 *  Default implementation of [ReactionPushEmojiFactory] that provides emoji codes for the default reaction types.
 */
private class DefaultReactionPushEmojiFactory : ReactionPushEmojiFactory {

    override fun emojiCode(type: String): String? = when (type) {
        ReactionDefaults.THUMBS_UP -> "👍"
        ReactionDefaults.LOVE -> "❤️"
        ReactionDefaults.LOL -> "😂"
        ReactionDefaults.WUT -> "😮"
        ReactionDefaults.THUMBS_DOWN -> "👎"
        else -> null
    }
}
