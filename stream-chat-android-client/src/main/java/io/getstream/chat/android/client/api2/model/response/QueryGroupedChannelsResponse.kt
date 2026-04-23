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

package io.getstream.chat.android.client.api2.model.response

import com.squareup.moshi.JsonClass

/**
 * Raw API response for the grouped query channels endpoint (`POST /channels/grouped`).
 *
 * @param groups The list of channel groups.
 * @param duration The server-reported request duration.
 */
@JsonClass(generateAdapter = true)
internal data class QueryGroupedChannelsResponse(
    val groups: Map<String, QueryGroupedChannelsGroup>,
    val duration: String,
)

/**
 * A single group within a [QueryGroupedChannelsResponse].
 *
 * @param channels The channel responses that belong to this group.
 * @param unread_channels The number of channels with unread messages in this group.
 */
@JsonClass(generateAdapter = true)
internal data class QueryGroupedChannelsGroup(
    val channels: List<ChannelResponse>,
    val unread_channels: Int?,
)
