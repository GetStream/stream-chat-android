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

package io.getstream.chat.android.previewdata

import io.getstream.chat.android.models.Reaction

/**
 * Provides sample reactions that will be used to render component previews.
 */
public object PreviewReactionData {

    private val reaction1: Reaction = Reaction(
        type = "like",
        user = PreviewUserData.user1,
    )

    private val reaction2: Reaction = Reaction(
        type = "love",
        user = PreviewUserData.user2,
    )

    private val reaction3: Reaction = Reaction(
        type = "wow",
        user = PreviewUserData.user3,
    )

    private val reaction4: Reaction = Reaction(
        type = "sad",
        user = PreviewUserData.user4,
    )

    public val oneReaction: List<Reaction> = listOf(reaction1)

    public val manyReaction: List<Reaction> = listOf(reaction1, reaction2, reaction3, reaction4)
}
