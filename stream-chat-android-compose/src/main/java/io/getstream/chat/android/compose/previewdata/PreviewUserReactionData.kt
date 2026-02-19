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

import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.previewdata.PreviewUserData

/**
 * Provides sample user reactions that will be used to render component previews.
 */
internal object PreviewUserReactionData {

    fun user1Reaction() = UserReactionItemState(
        user = PreviewUserData.user1,
        type = "like",
        emojiCode = "👍",
    )

    fun user2Reaction() = UserReactionItemState(
        user = PreviewUserData.user2,
        type = "love",
        emojiCode = "❤️",
    )

    fun user3Reaction() = UserReactionItemState(
        user = PreviewUserData.user3,
        type = "wow",
        emojiCode = "😮",
    )

    fun user4Reaction() = UserReactionItemState(
        user = PreviewUserData.user4,
        type = "sad",
        emojiCode = "👎",
    )

    fun oneUserReaction() = listOf(
        user1Reaction(),
    )

    fun manyUserReactions() = listOf(
        user1Reaction(),
        user2Reaction(),
        user3Reaction(),
        user4Reaction(),
    )
}
