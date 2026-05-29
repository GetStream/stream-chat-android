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
 * Matches by whitespace-tokenizing the query and the user's name (or id when blank): every word
 * except the last must equal at least one name word, and the last word must be a prefix of at
 * least one name word. The same name word may satisfy multiple query words. Results are sorted
 * alphabetically by normalized name. Normalization applies lowercasing, diacritics removal, and
 * optional transliteration.
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
        val queryTokens = queryFormatter.format(query).tokenize()
        return items
            .map { user -> user to queryFormatter.format(user.name.ifBlank(user::id)) }
            .filter { (_, formattedName) -> matches(queryTokens, formattedName) }
            .sortedBy { (_, formattedName) -> formattedName }
            .map { (user, _) -> user }
    }

    private fun matches(queryTokens: List<String>, formattedName: String): Boolean {
        if (queryTokens.isEmpty()) return true
        val nameTokens = formattedName.tokenize()
        val lastIndex = queryTokens.lastIndex
        return queryTokens.withIndex().all { (i, token) ->
            if (i == lastIndex) {
                nameTokens.any { it.startsWith(token) }
            } else {
                nameTokens.any { it == token }
            }
        }
    }

    private fun String.tokenize(): List<String> = split(WHITESPACE).filter(String::isNotEmpty)

    private companion object {
        private val WHITESPACE = "\\s+".toRegex()
    }
}
