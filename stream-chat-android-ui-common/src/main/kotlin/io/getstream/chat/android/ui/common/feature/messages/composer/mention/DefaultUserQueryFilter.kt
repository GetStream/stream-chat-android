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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.query.filter.DefaultQueryFilter
import io.getstream.chat.android.ui.common.feature.messages.composer.query.filter.QueryFilter
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.DefaultStreamTransliterator
import io.getstream.chat.android.ui.common.feature.messages.composer.transliteration.StreamTransliterator

/**
 * Default implementation of [QueryFilter] for [User] objects.
 *
 * This implementation of [QueryFilter] ignores upper case, diacritics
 * It uses levenshtein approximation so typos are included in the search.
 *
 * It is possible to choose a transliteration by providing a [transliterator].
 *
 * @param transliterator The transliterator to use for transliterating the query string.
 */
public class DefaultUserQueryFilter(
    transliterator: StreamTransliterator = DefaultStreamTransliterator(),
) : QueryFilter<User> {

    private val delegate = DefaultQueryFilter<User>(transliterator) { it.name.ifBlank { it.id } }

    override fun filter(items: List<User>, query: String): List<User> = delegate.filter(items, query)
}
