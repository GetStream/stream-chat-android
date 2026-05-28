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

@JsonClass(generateAdapter = true)
internal data class QueryChannelsRequest(
    // Standard filter + sort query
    val filter_conditions: Map<*, *>? = null,
    val sort: List<Map<String, Any>>? = null,

    // Predefined filters query
    val predefined_filter: String? = null,
    val filter_values: Map<String, Any>? = null,
    val sort_values: Map<String, Any>? = null,

    // Query options
    val offset: Int,
    val limit: Int,
    val message_limit: Int?,
    val member_limit: Int?,
    val state: Boolean,
    val watch: Boolean,
    val presence: Boolean,
)
