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

package io.getstream.chat.android.ui.common.feature.messages.composer.query.filter

import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.Combine
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.IgnoreDiacritics
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.Lowercase
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.QueryFormatter
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.Transliterate
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.DefaultStreamTransliterator
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.StreamTransliterator
import kotlin.math.min

/**
 * Default implementation of [QueryFilter].
 *
 * This implementation of [QueryFilter] ignores upper case, diacritics
 * It uses levenshtein approximation so typos are included in the search.
 *
 * It is possible to choose a transliteration by providing a [transliterator].
 *
 * @param transliterator The transliterator to use for transliterating the query string.
 * @param target The function to extract the target string from the item.
 */
public class DefaultQueryFilter<T>(
    private val transliterator: StreamTransliterator = DefaultStreamTransliterator(),
    private val target: (T) -> String,
) : QueryFilter<T> {

    private val queryFormatter: QueryFormatter = Combine(
        Lowercase(),
        IgnoreDiacritics(),
        Transliterate(transliterator),
    )

    override fun filter(items: List<T>, query: String): List<T> {
        val formattedQuery = queryFormatter.format(query)
        return items.asSequence()
            .map { it.measureDistance(formattedQuery) }
            .filter { it.distance < MAX_DISTANCE }
            .sorted()
            .map { it.item }
            .toList()
    }

    private fun T.measureDistance(formattedQuery: String): MeasuredItem<T> {
        val formattedTarget = queryFormatter.format(target(this))
        val distance = when (formattedTarget.contains(formattedQuery, ignoreCase = true)) {
            true -> 0
            else -> levenshteinDistance(formattedQuery, formattedTarget)
        }
        return MeasuredItem(this, distance)
    }

    private data class MeasuredItem<T>(val item: T, val distance: Int) : Comparable<MeasuredItem<T>> {
        override fun compareTo(other: MeasuredItem<T>): Int {
            return distance.compareTo(other.distance)
        }
    }

    private fun levenshteinDistance(search: String, target: String): Int {
        when {
            search == target -> return 0
            search.isEmpty() -> return target.length
            target.isEmpty() -> return search.length
        }

        val searchLength = search.length + 1
        val targetLength = target.length + 1

        var cost = Array(searchLength) { it }
        var newCost = Array(searchLength) { 0 }

        for (i in 1 until targetLength) {
            newCost[0] = i

            for (j in 1 until searchLength) {
                val match = if (search[j - 1] == target[i - 1]) 0 else 1

                val costReplace = cost[j - 1] + match
                val costInsert = cost[j] + 1
                val costDelete = newCost[j - 1] + 1

                newCost[j] = min(min(costInsert, costDelete), costReplace)
            }

            val swap = cost
            cost = newCost
            newCost = swap
        }

        return cost[searchLength - 1]
    }

    private companion object {
        private const val MAX_DISTANCE = 3
    }
}
