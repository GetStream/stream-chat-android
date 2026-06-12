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
 * 
 */

data class ChannelStateResponseFields (
    @Json(name = "members")
    val members: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMemberResponse> = emptyList(),

    @Json(name = "messages")
    val messages: kotlin.collections.List<io.getstream.chat.android.network.models.MessageResponse> = emptyList(),

    @Json(name = "pinned_messages")
    val pinnedMessages: kotlin.collections.List<io.getstream.chat.android.network.models.MessageResponse> = emptyList(),

    @Json(name = "threads")
    val threads: kotlin.collections.List<io.getstream.chat.android.network.models.ThreadStateResponse> = emptyList(),

    @Json(name = "hidden")
    val hidden: kotlin.Boolean? = null,

    @Json(name = "hide_messages_before")
    val hideMessagesBefore: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "watcher_count")
    val watcherCount: kotlin.Int? = null,

    @Json(name = "active_live_locations")
    val activeLiveLocations: kotlin.collections.List<io.getstream.chat.android.network.models.SharedLocationResponseData>? = emptyList(),

    @Json(name = "pending_messages")
    val pendingMessages: kotlin.collections.List<io.getstream.chat.android.network.models.PendingMessageResponse>? = emptyList(),

    @Json(name = "read")
    val read: kotlin.collections.List<io.getstream.chat.android.network.models.ReadStateResponse>? = emptyList(),

    @Json(name = "watchers")
    val watchers: kotlin.collections.List<io.getstream.chat.android.network.models.UserResponse>? = emptyList(),

    @Json(name = "channel")
    val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "draft")
    val draft: io.getstream.chat.android.network.models.DraftResponse? = null,

    @Json(name = "membership")
    val membership: io.getstream.chat.android.network.models.ChannelMemberResponse? = null,

    @Json(name = "push_preferences")
    val pushPreferences: io.getstream.chat.android.network.models.ChannelPushPreferencesResponse? = null
)
