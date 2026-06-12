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

data class OwnUserResponse (
    @Json(name = "banned")
    val banned: kotlin.Boolean,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "invisible")
    val invisible: kotlin.Boolean,

    @Json(name = "language")
    val language: kotlin.String,

    @Json(name = "online")
    val online: kotlin.Boolean,

    @Json(name = "role")
    val role: kotlin.String,

    @Json(name = "total_unread_count")
    val totalUnreadCount: kotlin.Int,

    @Json(name = "unread_channels")
    val unreadChannels: kotlin.Int,

    @Json(name = "unread_count")
    val unreadCount: kotlin.Int,

    @Json(name = "unread_threads")
    val unreadThreads: kotlin.Int,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "channel_mutes")
    val channelMutes: kotlin.collections.List<io.getstream.chat.android.network.models.ChannelMute> = emptyList(),

    @Json(name = "devices")
    val devices: kotlin.collections.List<io.getstream.chat.android.network.models.DeviceResponse> = emptyList(),

    @Json(name = "mutes")
    val mutes: kotlin.collections.List<io.getstream.chat.android.network.models.UserMuteResponse> = emptyList(),

    @Json(name = "teams")
    val teams: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "avg_response_time")
    val avgResponseTime: kotlin.Int? = null,

    @Json(name = "deactivated_at")
    val deactivatedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "deleted_at")
    val deletedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "image")
    val image: kotlin.String? = null,

    @Json(name = "last_active")
    val lastActive: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "name")
    val name: kotlin.String? = null,

    @Json(name = "revoke_tokens_issued_before")
    val revokeTokensIssuedBefore: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "blocked_user_ids")
    val blockedUserIds: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "latest_hidden_channels")
    val latestHiddenChannels: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "push_preferences")
    val pushPreferences: io.getstream.chat.android.network.models.PushPreferencesResponse? = null,

    @Json(name = "teams_role")
    val teamsRole: kotlin.collections.Map<kotlin.String, kotlin.String>? = emptyMap(),

    @Json(name = "total_unread_count_by_team")
    val totalUnreadCountByTeam: kotlin.collections.Map<kotlin.String, kotlin.Int>? = emptyMap()
)
