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

package io.getstream.chat.android.compose.state

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.querysort.QuerySorter

/**
 * The configuration for querying various bits of data. It's generic, so it can be used to query
 * Channels, Messages or something else.
 *
 * @param filters The [FilterObject] to apply to the query.
 * @param querySort The sorting option for the query results.
 */
public data class QueryConfig<T : Any>(
    val filters: FilterObject,
    val querySort: QuerySorter<T>,
)
