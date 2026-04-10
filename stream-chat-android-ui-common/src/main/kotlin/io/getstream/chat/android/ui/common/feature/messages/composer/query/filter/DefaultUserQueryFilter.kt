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

package io.getstream.chat.android.ui.common.feature.messages.composer.query.filter

import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.Combine
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.IgnoreDiacritics
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.Lowercase
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.Transliterate
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.DefaultStreamTransliterator
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.StreamTransliterator
import io.getstream.log.taggedLogger
import kotlin.math.min

/**
 * Default [QueryFilter] for [User] objects used in mention suggestions.
 *
 * Keeps only users whose normalized name (or id) contains the normalized query as a substring,
 * then sorts results by Levenshtein distance so the closest matches appear first. Normalization
 * applies lowercasing, diacritics removal, and optional transliteration.
 *
 * @param transliterator The transliterator to use for normalizing strings.
 */
public class DefaultUserQueryFilter(
    transliterator: StreamTransliterator = DefaultStreamTransliterator(),
) : QueryFilter<User> {

    private val logger by taggedLogger("Chat:QueryFilter")

    private val queryFormatter = Combine(
        Lowercase(),
        IgnoreDiacritics(),
        Transliterate(transliterator),
    )

    override fun filter(items: List<User>, query: String): List<User> {
        logger.d { "[filter] query: \"$query\", items.size: ${items.size}" }
        val formattedQuery = queryFormatter.format(query)
        if (formattedQuery.isEmpty()) return items
        return items
            .mapNotNull { user ->
                val formattedName = queryFormatter.format(query = user.name.ifBlank(user::id))
                if (formattedName.contains(formattedQuery)) {
                    user to levenshteinDistance(formattedQuery, formattedName)
                } else {
                    null
                }
            }
            .sortedBy { (_, distance) -> distance }
            .map { (user, _) -> user }
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
}
