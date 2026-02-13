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

import androidx.compose.runtime.Composable
import io.getstream.chat.android.compose.state.reactionoptions.ReactionOptionItemState

/**
 * Provides sample reaction option items that will be used to render component previews.
 */
internal object PreviewReactionOptionData {

    @Composable
    fun reactionOption1() = ReactionOptionItemState(
        type = "like",
        emojiCode = "👍",
    )

    @Composable
    fun reactionOption2() = ReactionOptionItemState(
        type = "love",
        emojiCode = "❤️",
    )

    @Composable
    fun reactionOption3() = ReactionOptionItemState(
        type = "wow",
        emojiCode = "😮",
    )

    @Composable
    fun reactionOption4() = ReactionOptionItemState(
        type = "sad",
        emojiCode = "👎",
    )

    @Composable
    fun oneReaction(): List<ReactionOptionItemState> = listOf(
        reactionOption1(),
    )

    @Composable
    fun manyReactions(): List<ReactionOptionItemState> = listOf(
        reactionOption1(),
        reactionOption2(),
        reactionOption3(),
        reactionOption4(),
    )
}
