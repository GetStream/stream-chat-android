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

package io.getstream.chat.android.models

/**
 * A grouped channels response returned by [ChatClient.queryGroupedChannels].
 *
 * @param groups The channel groups returned by the backend, keyed by group name.
 */
public data class GroupedChannels(public val groups: Map<String, GroupedChannelsGroup>)

/**
 * A channel group returned by [ChatClient.queryGroupedChannels].
 *
 * @param groupKey The name of the group.
 * @param channels The channels that belong to this group.
 * @param unreadChannels The total unread channel count in the group.
 * @param next Cursor for the next page of this group, or `null` if there is no further page.
 * @param prev Cursor for the previous page of this group, or `null` if there is none.
 */
public data class GroupedChannelsGroup(
    public val groupKey: String,
    public val channels: List<Channel>,
    public val unreadChannels: Int = 0,
    public val next: String? = null,
    public val prev: String? = null,
)

/**
 * Per-group request options for [ChatClient.queryGroupedChannels].
 *
 * @param limit Max channels for this group. `null` falls back to the request-level limit
 * (which, in turn, falls back to the server default when also `null`).
 * @param next Cursor for the next page of this group. Mutually exclusive with [prev].
 * @param prev Cursor for the previous page of this group. Mutually exclusive with [next].
 * @param filter Conditions the group's channels must match, or `null` to return the group as stored.
 * For a named group, only a `query_timestamp` `greaterThanEquals` cutoff is accepted, given as a date, a full
 * timestamp or a relative `now-7d` / `7d` value. For the `all` group, `or` branches each combine a `group`
 * condition (`eq` or `in`) with an optional cutoff using `and`, or a single standalone `ne` excludes one group.
 * [Filters.neutral] matches every channel of the stored group. A non-null filter requires the request
 * to sort by `query_timestamp` descending.
 */
public data class GroupedChannelsGroupQuery(
    public val limit: Int? = null,
    public val next: String? = null,
    public val prev: String? = null,
    public val filter: FilterObject? = null,
)
