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

package io.getstream.chat.android.client.api2.model.requests

import com.squareup.moshi.JsonClass

/**
 * Request object for querying reminders.
 *
 * @property filter The filter conditions for querying reminders.
 * @property limit The maximum number of reminders to return.
 * @property sort The sorting criteria for the reminders.
 * @property next The pagination token for fetching the next set of results.
 */
@JsonClass(generateAdapter = true)
internal data class QueryRemindersRequest(
    val filter: Map<*, *>,
    val limit: Int,
    val next: String?,
    val sort: List<Map<String, Any>>,
)
