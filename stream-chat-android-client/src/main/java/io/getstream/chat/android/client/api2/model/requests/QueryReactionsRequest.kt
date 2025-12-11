/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

/**
 * Request for querying reactions on a message.
 *
 * @property filter The filter criteria.
 * @property limit The maximum number of reactions to return.
 * @property next The pagination token for fetching the next set of results.
 * @property sort The sorting criteria to apply.
 */
@JsonClass(generateAdapter = true)
internal data class QueryReactionsRequest(
    val filter: Map<*, *>? = null,
    val limit: Int? = null,
    val next: String? = null,
    val sort: List<Map<String, Any>>? = null,
)
