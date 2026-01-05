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

package io.getstream.chat.android.models

import java.util.Date

/**
 * A group of reactions of the same type.
 *
 * @property type The type of the reaction this group represents.
 * @property count The number of users that have reacted with this reaction.
 * @property sumScore The sum of the scores of this reaction.
 * @property firstReactionAt The first time this reaction was added.
 * @property lastReactionAt The last time this reaction was added.
 */
public data class ReactionGroup(
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
) {

    @SinceKotlin("99999.9")
    @Suppress("NEWER_VERSION_IN_SINCE_KOTLIN")
    public fun newBuilder(): Builder = Builder(this)

    public class Builder() {
        private var type: String = ""
        private var count: Int = 0
        private var sumScore: Int = 0
        private var firstReactionAt: Date = Date()
        private var lastReactionAt: Date = Date()

        public constructor(reactionGroup: ReactionGroup) : this() {
            type = reactionGroup.type
            count = reactionGroup.count
            sumScore = reactionGroup.sumScore
            firstReactionAt = reactionGroup.firstReactionAt
            lastReactionAt = reactionGroup.lastReactionAt
        }

        public fun type(type: String): Builder = apply { this.type = type }
        public fun count(count: Int): Builder = apply { this.count = count }
        public fun sumScore(sumScore: Int): Builder = apply { this.sumScore = sumScore }
        public fun firstReactionAt(firstReactionAt: Date): Builder = apply { this.firstReactionAt = firstReactionAt }
        public fun lastReactionAt(lastReactionAt: Date): Builder = apply { this.lastReactionAt = lastReactionAt }

        public fun build(): ReactionGroup = ReactionGroup(
            type = type,
            count = count,
            sumScore = sumScore,
            firstReactionAt = firstReactionAt,
            lastReactionAt = lastReactionAt,
        )
    }
}
