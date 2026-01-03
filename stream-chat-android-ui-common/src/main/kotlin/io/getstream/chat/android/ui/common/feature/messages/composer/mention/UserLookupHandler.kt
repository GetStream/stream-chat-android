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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.query.formatter.QueryFormatter

/**
 * Users lookup functional interface. Used to create custom users lookup algorithm.
 */
public fun interface UserLookupHandler {
    /**
     * Performs users lookup by given [query] in suspend way.
     * It's executed on background, so it can perform heavy operations.
     *
     * @param query String as user input for lookup algorithm.
     * @return List of users as result of lookup.
     */
    public suspend fun handleUserLookup(query: String): List<User>
}

/**
 * Wraps the current [UserLookupHandler] with the provided [queryFormatter].
 *
 * @param queryFormatter The query formatter to be used.
 */
public fun UserLookupHandler.withQueryFormatter(
    queryFormatter: QueryFormatter,
): UserLookupHandler {
    val delegate = this
    return UserLookupHandler { query ->
        val updatedQuery = queryFormatter.format(query)
        delegate.handleUserLookup(updatedQuery)
    }
}
