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

package io.getstream.chat.android.compose.previewdata

import io.getstream.chat.android.compose.state.messages.MessageReactionItemState
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState
import io.getstream.chat.android.compose.state.userreactions.ReactionItem

/**
 * Provides sample reaction option items that will be used to render component previews.
 */
internal object PreviewReactionData {

    fun reactionOption1() = ReactionOptionItemState(
        item = ReactionItem(type = "like", emoji = "👍"),
        isSelected = false,
    )

    fun reactionOption2() = ReactionOptionItemState(
        item = ReactionItem(type = "love", emoji = "❤️"),
        isSelected = true,
    )

    fun messageReaction1() = MessageReactionItemState(
        item = ReactionItem(type = "like", emoji = "👍"),
        count = 1,
    )

    fun messageReaction2() = MessageReactionItemState(
        item = ReactionItem(type = "love", emoji = "❤️"),
        count = 10,
    )

    fun messageReaction3() = MessageReactionItemState(
        item = ReactionItem(type = "wow", emoji = "😮"),
        count = 2,
    )

    fun messageReaction4() = MessageReactionItemState(
        item = ReactionItem(type = "sad", emoji = "👎"),
        count = 5,
    )

    fun oneReaction(): List<MessageReactionItemState> = listOf(
        messageReaction1(),
    )

    fun manyReactions(): List<MessageReactionItemState> = listOf(
        messageReaction1(),
        messageReaction2(),
        messageReaction3(),
        messageReaction4(),
    )

    fun reactionPickerEmojis(): Map<String, String> = mapOf(
        "like" to "👍",
        "love" to "❤️",
        "haha" to "😂",
        "wow" to "😮",
        "sad" to "👎",
    )
}
