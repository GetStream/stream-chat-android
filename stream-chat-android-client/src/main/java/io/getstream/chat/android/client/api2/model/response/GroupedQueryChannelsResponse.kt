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
 * @param family The family identifier that groups the buckets (e.g. "support").
 * @param buckets The list of channel buckets belonging to this family.
 * @param duration The server-reported request duration (e.g. "12ms").
 */
@JsonClass(generateAdapter = true)
internal data class GroupedQueryChannelsResponse(
    val family: String,
    val buckets: List<GroupedQueryChannelsBucket>,
    val duration: String,
)

/**
 * A single bucket within a [GroupedQueryChannelsResponse].
 *
 * @param key The backend-defined key for this bucket within the family (e.g. "all-open").
 * @param channels The channel responses that belong to this bucket.
 * @param unread_count The total number of unread messages across all channels in this bucket.
 * @param unread_channels The number of channels with unread messages in this bucket.
 */
@JsonClass(generateAdapter = true)
internal data class GroupedQueryChannelsBucket(
    val key: String,
    val channels: List<ChannelResponse>,
    val unread_count: Int,
    val unread_channels: Int,
)
