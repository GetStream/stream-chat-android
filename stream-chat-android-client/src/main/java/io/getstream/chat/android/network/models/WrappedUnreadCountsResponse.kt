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

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.getstream.chat.android.network.models

import com.squareup.moshi.Json

/**
 * Basic response information
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class WrappedUnreadCountsResponse(
    @Json(name = "duration")
    internal val duration: String,

    @Json(name = "total_unread_count")
    internal val totalUnreadCount: Int,

    @Json(name = "total_unread_threads_count")
    internal val totalUnreadThreadsCount: Int,

    @Json(name = "channel_type")
    internal val channelType: List<io.getstream.chat.android.network.models.UnreadCountsChannelType> = emptyList(),

    @Json(name = "channels")
    internal val channels: List<io.getstream.chat.android.network.models.UnreadCountsChannel> = emptyList(),

    @Json(name = "threads")
    internal val threads: List<io.getstream.chat.android.network.models.UnreadCountsThread> = emptyList(),

    @Json(name = "total_unread_count_by_team")
    internal val totalUnreadCountByTeam: Map<String, Int>? = emptyMap(),
)
