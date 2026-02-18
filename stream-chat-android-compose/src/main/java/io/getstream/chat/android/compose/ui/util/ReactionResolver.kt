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

    /** A small set of commonly used reaction types, typically shown for quick access in the UI. */
    public val defaultReactions: List<String>

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
    override val defaultReactions: List<String> = defaultQuickReactions,
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
        private val defaultQuickReactions = listOf("like", "love", "haha", "wow", "sad")

        // Explicitly use linkedMapOf to make it clear that map iteration follows insertion order
        private val defaultEmojiMapping: Map<String, String> = linkedMapOf(
            "grinning" to "😀",
            "smiley" to "😃",
            "smile" to "😄",
            "grin" to "😁",
            "laughing" to "😆",
            "sweat_smile" to "😅",
            "rofl" to "🤣",
            "haha" to "😂",
            "slightly_smiling_face" to "🙂",
            "upside_down_face" to "🙃",
            "wink" to "😉",
            "blush" to "😊",
            "innocent" to "😇",
            "smiling_face_with_three_hearts" to "🥰",
            "heart_eyes" to "😍",
            "star_struck" to "🤩",
            "kissing_heart" to "😘",
            "kissing" to "😗",
            "kissing_closed_eyes" to "😚",
            "kissing_smiling_eyes" to "😙",
            "yum" to "😋",
            "stuck_out_tongue" to "😛",
            "stuck_out_tongue_winking_eye" to "😜",
            "zany_face" to "🤪",
            "stuck_out_tongue_closed_eyes" to "😝",
            "money_mouth_face" to "🤑",
            "hugs" to "🤗",
            "hand_over_mouth" to "🤭",
            "shushing_face" to "🤫",
            "thinking" to "🤔",
            "zipper_mouth_face" to "🤐",
            "raised_eyebrow" to "🤨",
            "neutral_face" to "😐",
            "expressionless" to "😑",
            "no_mouth" to "😶",
            "face_in_clouds" to "😶‍🌫️",
            "smirk" to "😏",
            "unamused" to "😒",
            "roll_eyes" to "🙄",
            "grimacing" to "😬",
            "lying_face" to "🤥",
            "relieved" to "😌",
            "pensive" to "😔",
            "sleepy" to "😪",
            "drooling_face" to "🤤",
            "sleeping" to "😴",
            "mask" to "😷",
            "face_with_thermometer" to "🤒",
            "face_with_head_bandage" to "🤕",
            "nauseated_face" to "🤢",
            "vomiting_face" to "🤮",
            "sneezing_face" to "🤧",
            "hot_face" to "🥵",
            "cold_face" to "🥶",
            "woozy_face" to "🥴",
            "face_with_spiral_eyes" to "😵‍💫",
            "exploding_head" to "🤯",
            "cowboy_hat_face" to "🤠",
            "partying_face" to "🥳",
            "sunglasses" to "😎",
            "nerd_face" to "🤓",
            "monocle_face" to "🧐",
            "confused" to "😕",
            "worried" to "😟",
            "slightly_frowning_face" to "🙁",
            "frowning_face" to "☹️",
            "wow" to "😮",
            "hushed" to "😯",
            "astonished" to "😲",
            "flushed" to "😳",
            "pleading_face" to "🥺",
            "frowning" to "😦",
            "anguished" to "😧",
            "fearful" to "😨",
            "cold_sweat" to "😰",
            "disappointed_relieved" to "😥",
            "cry" to "😢",
            "sob" to "😭",
            "scream" to "😱",
            "confounded" to "😖",
            "persevere" to "😣",
            "disappointed" to "😞",
            "sweat" to "😓",
            "weary" to "😩",
            "tired_face" to "😫",
            "yawning_face" to "🥱",
            "triumph" to "😤",
            "rage" to "😡",
            "angry" to "😠",
            "cursing_face" to "🤬",
            "smiling_imp" to "😈",
            "imp" to "👿",
            "skull" to "💀",
            "skull_and_crossbones" to "☠️",
            "poop" to "💩",
            "clown_face" to "🤡",
            "japanese_ogre" to "👹",
            "japanese_goblin" to "👺",
            "ghost" to "👻",
            "alien" to "👽",
            "space_invader" to "👾",
            "robot" to "🤖",
            "jack_o_lantern" to "🎃",
            "smiley_cat" to "😺",
            "smile_cat" to "😸",
            "joy_cat" to "😹",
            "heart_eyes_cat" to "😻",
            "smirk_cat" to "😼",
            "kissing_cat" to "😽",
            "scream_cat" to "🙀",
            "crying_cat_face" to "😿",
            "pouting_cat" to "😾",
            "like" to "👍",
            "sad" to "👎",
            "ok_hand" to "👌",
            "pinched_fingers" to "🤌",
            "pinching_hand" to "🤏",
            "v" to "✌️",
            "crossed_fingers" to "🤞",
            "love_you_gesture" to "🤟",
            "metal" to "🤘",
            "call_me_hand" to "🤙",
            "point_left" to "👈",
            "point_right" to "👉",
            "point_up_2" to "👆",
            "point_down" to "👇",
            "point_up" to "☝️",
            "raised_hand" to "✋",
            "raised_back_of_hand" to "🤚",
            "raised_hand_with_fingers_splayed" to "🖐️",
            "vulcan_salute" to "🖖",
            "wave" to "👋",
            "handshake" to "🤝",
            "pray" to "🙏",
            "muscle" to "💪",
            "footprints" to "👣",
            "eyes" to "👀",
            "brain" to "🧠",
            "heart_hands" to "🫶",
            "kiss" to "💋",
            "love" to "❤️",
            "orange_heart" to "🧡",
            "yellow_heart" to "💛",
            "green_heart" to "💚",
            "blue_heart" to "💙",
            "purple_heart" to "💜",
            "black_heart" to "🖤",
            "white_heart" to "🤍",
            "brown_heart" to "🤎",
            "broken_heart" to "💔",
            "heart_exclamation" to "❣️",
            "two_hearts" to "💕",
            "revolving_hearts" to "💞",
            "heartbeat" to "💓",
            "growing_heart" to "💗",
            "sparkling_heart" to "💖",
            "cupid" to "💘",
            "gift_heart" to "💝",
        )
    }
}
