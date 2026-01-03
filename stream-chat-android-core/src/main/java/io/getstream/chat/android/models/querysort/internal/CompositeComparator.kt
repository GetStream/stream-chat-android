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

package io.getstream.chat.android.models.querysort.internal

import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * Composite comparator that combines multiple comparators into one.
 *
 * @param T The type of elements to be compared.
 * @param comparators The list of comparators to be combined.
 */
internal class CompositeComparator<T>(private val comparators: List<Comparator<T>>) : Comparator<T> {
    override fun compare(o1: T, o2: T): Int =
        comparators.fold(QuerySorter.EQUAL_ON_COMPARISON) { currentComparisonValue, comparator ->
            when (currentComparisonValue) {
                QuerySorter.EQUAL_ON_COMPARISON -> comparator.compare(o1, o2)
                else -> currentComparisonValue
            }
        }
}
