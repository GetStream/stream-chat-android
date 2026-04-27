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

/**
 * Default [QueryFilter] for [User] objects used in mention suggestions.
 *
 * Keeps only users whose normalized name (or id) contains the normalized query as a substring,
 * then sorts results by match position so prefix matches appear first. Normalization applies
 * lowercasing, diacritics removal, and optional transliteration.
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
                val index = formattedName.indexOf(formattedQuery)
                if (index >= 0) user to index else null
            }
            .sortedBy { (_, index) -> index }
            .map { (user, _) -> user }
    }
}
