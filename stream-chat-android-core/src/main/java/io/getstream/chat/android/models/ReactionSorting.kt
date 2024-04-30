/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

/**
 * A comparator used to sort [ReactionGroup]s.
 *
 */
public interface ReactionSorting : Comparator<ReactionGroup>

/**
 * Sorts [ReactionGroup]s by the sum of their scores.
 *
 */
public object ReactionSortingBySumScore : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        return o1.sumScore.compareTo(o2.sumScore)
    }
}

/**
 * Sorts [ReactionGroup]s by their count.
 *
 */
public object ReactionSortingByCount : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        return o1.count.compareTo(o2.count)
    }
}

/**
 * Sorts [ReactionGroup]s by the date of their last reaction.
 *
 */
public object ReactionSortingByLastReactionAt : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        return o1.lastReactionAt.compareTo(o2.lastReactionAt)
    }
}

/**
 * Sorts [ReactionGroup]s by the date of their first reaction.
 *
 */
public object ReactionSortingByFirstReactionAt : ReactionSorting {
    override fun compare(o1: ReactionGroup, o2: ReactionGroup): Int {
        return o1.firstReactionAt.compareTo(o2.firstReactionAt)
    }
}
