/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-video-android/blob/main/LICENSE
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
    "UnusedImport"
)

package io.getstream.chat.android.network.models

import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.*
import kotlin.io.*
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Basic response information
 */

data class WrappedUnreadCountsResponse (
    @Json(name = "duration")
    val duration: kotlin.String,

    @Json(name = "total_unread_count")
    val totalUnreadCount: kotlin.Int,

    @Json(name = "total_unread_threads_count")
    val totalUnreadThreadsCount: kotlin.Int,

    @Json(name = "channel_type")
    val channelType: kotlin.collections.List<io.getstream.chat.android.network.models.UnreadCountsChannelType> = emptyList(),

    @Json(name = "channels")
    val channels: kotlin.collections.List<io.getstream.chat.android.network.models.UnreadCountsChannel> = emptyList(),

    @Json(name = "threads")
    val threads: kotlin.collections.List<io.getstream.chat.android.network.models.UnreadCountsThread> = emptyList(),

    @Json(name = "total_unread_count_by_team")
    val totalUnreadCountByTeam: kotlin.collections.Map<kotlin.String, kotlin.Int>? = emptyMap()
)
