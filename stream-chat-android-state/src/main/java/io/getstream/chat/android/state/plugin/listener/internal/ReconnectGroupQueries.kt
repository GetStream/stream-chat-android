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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.models.GroupedChannelsGroupQuery

/**
 * The groups of the grouped query that refreshes the grouped lists after a reconnect. A first page applied for a group
 * in [keysKeepingPages], whose channels the sync covered, can keep the pages the list has loaded instead of resetting
 * the list.
 *
 * Equal to any map with the same entries, so the request is sent and deduplicated like any other.
 *
 * @param groups The per-group request options.
 * @param keysKeepingPages The keys of the groups that can keep their loaded pages.
 */
internal class ReconnectGroupQueries(
    groups: Map<String, GroupedChannelsGroupQuery>,
    val keysKeepingPages: Set<String>,
) : LinkedHashMap<String, GroupedChannelsGroupQuery>(groups)
