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
internal data class ChannelMemberResponse(
    @Json(name = "banned")
    internal val banned: Boolean,

    @Json(name = "channel_role")
    internal val channelRole: String,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "notifications_muted")
    internal val notificationsMuted: Boolean,

    @Json(name = "shadow_banned")
    internal val shadowBanned: Boolean,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "archived_at")
    internal val archivedAt: java.util.Date? = null,

    @Json(name = "ban_expires")
    internal val banExpires: java.util.Date? = null,

    @Json(name = "deleted_at")
    internal val deletedAt: java.util.Date? = null,

    @Json(name = "invite_accepted_at")
    internal val inviteAcceptedAt: java.util.Date? = null,

    @Json(name = "invite_rejected_at")
    internal val inviteRejectedAt: java.util.Date? = null,

    @Json(name = "invited")
    internal val invited: Boolean? = null,

    @Json(name = "is_moderator")
    internal val isModerator: Boolean? = null,

    @Json(name = "pinned_at")
    internal val pinnedAt: java.util.Date? = null,

    @Json(name = "role")
    internal val role: String? = null,

    @Json(name = "status")
    internal val status: String? = null,

    @Json(name = "user_id")
    internal val userId: String? = null,

    @Json(name = "deleted_messages")
    internal val deletedMessages: List<String>? = emptyList(),

    @Json(name = "user")
    internal val user: io.getstream.chat.android.network.models.UserResponse? = null,
)
