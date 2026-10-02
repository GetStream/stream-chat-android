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
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ChannelStateResponse(
    @Json(name = "duration")
    internal val duration: String,

    @Json(name = "members")
    internal val members: List<io.getstream.chat.android.network.models.ChannelMemberResponse> = emptyList(),

    @Json(name = "messages")
    internal val messages: List<io.getstream.chat.android.network.models.MessageResponse> = emptyList(),

    @Json(name = "pinned_messages")
    internal val pinnedMessages: List<io.getstream.chat.android.network.models.MessageResponse> = emptyList(),

    @Json(name = "threads")
    internal val threads: List<io.getstream.chat.android.network.models.ThreadStateResponse> = emptyList(),

    @Json(name = "hidden")
    internal val hidden: Boolean? = null,

    @Json(name = "hide_messages_before")
    internal val hideMessagesBefore: java.util.Date? = null,

    @Json(name = "watcher_count")
    internal val watcherCount: Int? = null,

    @Json(name = "active_live_locations")
    internal val activeLiveLocations: List<io.getstream.chat.android.network.models.SharedLocationResponseData>? = null,

    @Json(name = "pending_messages")
    internal val pendingMessages: List<io.getstream.chat.android.network.models.PendingMessageResponse>? = null,

    @Json(name = "read")
    internal val read: List<io.getstream.chat.android.network.models.ReadStateResponse>? = null,

    @Json(name = "watchers")
    internal val watchers: List<io.getstream.chat.android.network.models.UserResponse>? = null,

    @Json(name = "channel")
    internal val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "draft")
    internal val draft: io.getstream.chat.android.network.models.DraftResponse? = null,

    @Json(name = "membership")
    internal val membership: io.getstream.chat.android.network.models.ChannelMemberResponse? = null,

    @Json(name = "push_preferences")
    internal val pushPreferences: io.getstream.chat.android.network.models.ChannelPushPreferencesResponse? = null,
)
