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
 * Request body for the grouped query channels endpoint (`POST /channels/grouped`).
 *
 * @param limit Default max channels per group when a group does not specify its own limit.
 * `null` uses the server default.
 * @param groups Optional per-group configuration keyed by group name. Omitting `groups` returns
 * the server-defined default set. Pagination (`next` or `prev` on any group) requires that
 * exactly one group is requested.
 * @param watch Whether to start watching the returned channels for real-time events.
 * @param presence Whether to receive presence events for the members of the returned channels.
 */
@JsonClass(generateAdapter = true)
internal data class QueryGroupedChannelsRequest(
    val limit: Int?,
    val groups: Map<String, QueryGroupedChannelsGroupRequest>?,
    val watch: Boolean,
    val presence: Boolean,
)

/**
 * Per-group request options inside a [QueryGroupedChannelsRequest].
 *
 * @param limit Max channels for this group. `null` (or `0`) falls back to the request-level limit.
 * @param next Cursor for the next page of this group. Mutually exclusive with [prev].
 * @param prev Cursor for the previous page of this group. Mutually exclusive with [next].
 */
@JsonClass(generateAdapter = true)
internal data class QueryGroupedChannelsGroupRequest(
    val limit: Int?,
    val next: String?,
    val prev: String?,
)
