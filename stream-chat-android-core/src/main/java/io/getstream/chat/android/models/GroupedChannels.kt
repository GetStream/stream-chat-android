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
 * A grouped channels response returned by [ChatClient.groupedQueryChannels].
 *
 * @param groups The channel groups returned by the backend in response order.
 */
public data class GroupedChannels(public val groups: Map<String, GroupedChannelsGroup>)

/**
 * A channel group returned by [ChatClient.groupedQueryChannels].
 *
 * @param channels The channels that belong to this group.
 * @param unreadChannels The total unread channel count in the group.
 */
public data class GroupedChannelsGroup(
    public val channels: List<Channel>,
    public val unreadChannels: Int?,
)
