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
internal data class FullUserResponse(
    @Json(name = "banned")
    internal val banned: Boolean,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "id")
    internal val id: String,

    @Json(name = "invisible")
    internal val invisible: Boolean,

    @Json(name = "language")
    internal val language: String,

    @Json(name = "online")
    internal val online: Boolean,

    @Json(name = "role")
    internal val role: String,

    @Json(name = "shadow_banned")
    internal val shadowBanned: Boolean,

    @Json(name = "total_unread_count")
    internal val totalUnreadCount: Int,

    @Json(name = "unread_channels")
    internal val unreadChannels: Int,

    @Json(name = "unread_count")
    internal val unreadCount: Int,

    @Json(name = "unread_threads")
    internal val unreadThreads: Int,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "blocked_user_ids")
    internal val blockedUserIds: List<String> = emptyList(),

    @Json(name = "channel_mutes")
    internal val channelMutes: List<io.getstream.chat.android.network.models.ChannelMute> = emptyList(),

    @Json(name = "devices")
    internal val devices: List<io.getstream.chat.android.network.models.DeviceResponse> = emptyList(),

    @Json(name = "mutes")
    internal val mutes: List<io.getstream.chat.android.network.models.UserMuteResponse> = emptyList(),

    @Json(name = "teams")
    internal val teams: List<String> = emptyList(),

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "avg_response_time")
    internal val avgResponseTime: Int? = null,

    @Json(name = "ban_expires")
    internal val banExpires: java.util.Date? = null,

    @Json(name = "deactivated_at")
    internal val deactivatedAt: java.util.Date? = null,

    @Json(name = "deleted_at")
    internal val deletedAt: java.util.Date? = null,

    @Json(name = "image")
    internal val image: String? = null,

    @Json(name = "last_active")
    internal val lastActive: java.util.Date? = null,

    @Json(name = "name")
    internal val name: String? = null,

    @Json(name = "revoke_tokens_issued_before")
    internal val revokeTokensIssuedBefore: java.util.Date? = null,

    @Json(name = "latest_hidden_channels")
    internal val latestHiddenChannels: List<String>? = emptyList(),

    @Json(name = "privacy_settings")
    internal val privacySettings: io.getstream.chat.android.network.models.PrivacySettingsResponse? = null,

    @Json(name = "teams_role")
    internal val teamsRole: Map<String, String>? = emptyMap(),
)
