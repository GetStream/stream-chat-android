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

package io.getstream.chat.android.client.api.state.querychannels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.models.GroupedChannelsGroupQuery

/**
 * Configuration captured from the most recent [ChatClient.queryGroupedChannels] request that
 * targeted a specific group. Subsequent paginated and recovery calls read this back so they
 * reuse the original parameters the caller chose.
 *
 * @property limit Request-level fallback limit applied to every group that doesn't specify its
 * own override.
 * @property pageSize This group's per-group override ([GroupedChannelsGroupQuery.limit]). `null`
 * when only the request-level [limit] was specified.
 * @property watch Whether the request asked to subscribe to channel events.
 * @property presence Whether the request asked to subscribe to presence events.
 */
public data class GroupedQueryConfig(
    public val limit: Int?,
    public val pageSize: Int?,
    public val watch: Boolean,
    public val presence: Boolean,
)
