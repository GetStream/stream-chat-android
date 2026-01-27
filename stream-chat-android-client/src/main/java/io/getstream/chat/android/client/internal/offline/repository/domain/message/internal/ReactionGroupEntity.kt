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

package io.getstream.chat.android.client.internal.offline.repository.domain.message.internal

import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class ReactionGroupEntity(
    /**
     * The type of the reaction this group represents.
     */
    val type: String,

    /**
     *The number of users that have reacted with this reaction.
     */
    val count: Int,

    /**
     * The sum of the scores of this reaction.
     */
    val sumScore: Int,

    /**
     * The first time this reaction was added.
     */
    val firstReactionAt: Date,

    /**
     * The last time this reaction was added.
     */
    val lastReactionAt: Date,
)
