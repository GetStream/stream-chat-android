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

import io.getstream.chat.android.randomReactionGroup
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.Date
import java.util.stream.Stream

internal class ReactionSortingTest {

    companion object {
        @JvmStatic
        fun provideTestCases(): Stream<ReactionSortingTestCase> {
            return Stream.of(
                listOf(
                    randomReactionGroup(sumScore = 5),
                    randomReactionGroup(sumScore = 7),
                    randomReactionGroup(sumScore = 10),
                ).let {
                    ReactionSortingTestCase(
                        it.shuffled(),
                        ReactionSortingBySumScore,
                        it,
                    )
                },
                listOf(
                    randomReactionGroup(count = 5),
                    randomReactionGroup(count = 7),
                    randomReactionGroup(count = 10),
                ).let {
                    ReactionSortingTestCase(
                        it.shuffled(),
                        ReactionSortingByCount,
                        it,
                    )
                },
                listOf(
                    randomReactionGroup(lastReactionAt = Date(0)),
                    randomReactionGroup(lastReactionAt = Date(5000)),
                    randomReactionGroup(lastReactionAt = Date(10000)),
                ).let {
                    ReactionSortingTestCase(
                        it.shuffled(),
                        ReactionSortingByLastReactionAt,
                        it,
                    )
                },
                listOf(
                    randomReactionGroup(firstReactionAt = Date(0)),
                    randomReactionGroup(firstReactionAt = Date(5000)),
                    randomReactionGroup(firstReactionAt = Date(10000)),
                ).let {
                    ReactionSortingTestCase(
                        it.shuffled(),
                        ReactionSortingByFirstReactionAt,
                        it,
                    )
                },
            )
        }
    }

    @ParameterizedTest
    @MethodSource("provideTestCases")
    fun `ReactionSorting sorts correctly`(testCase: ReactionSortingTestCase) {
        val result = testCase.input.sortedWith(testCase.sorting)
        result `should be equal to` testCase.expected
    }

    data class ReactionSortingTestCase(
        val input: List<ReactionGroup>,
        val sorting: ReactionSorting,
        val expected: List<ReactionGroup>,
    )
}
