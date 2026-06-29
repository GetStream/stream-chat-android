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
import kotlin.collections.List
import kotlin.collections.Map

/**
 *
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ChannelMemberResponse(
    @Json(name = "banned")
    val banned: kotlin.Boolean,

    @Json(name = "channel_role")
    val channelRole: kotlin.String,

    @Json(name = "created_at")
    val createdAt: java.util.Date,

    @Json(name = "notifications_muted")
    val notificationsMuted: kotlin.Boolean,

    @Json(name = "shadow_banned")
    val shadowBanned: kotlin.Boolean,

    @Json(name = "updated_at")
    val updatedAt: java.util.Date,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "archived_at")
    val archivedAt: java.util.Date? = null,

    @Json(name = "ban_expires")
    val banExpires: java.util.Date? = null,

    @Json(name = "deleted_at")
    val deletedAt: java.util.Date? = null,

    @Json(name = "invite_accepted_at")
    val inviteAcceptedAt: java.util.Date? = null,

    @Json(name = "invite_rejected_at")
    val inviteRejectedAt: java.util.Date? = null,

    @Json(name = "invited")
    val invited: kotlin.Boolean? = null,

    @Json(name = "is_moderator")
    val isModerator: kotlin.Boolean? = null,

    @Json(name = "pinned_at")
    val pinnedAt: java.util.Date? = null,

    @Json(name = "role")
    val role: kotlin.String? = null,

    @Json(name = "status")
    val status: kotlin.String? = null,

    @Json(name = "user_id")
    val userId: kotlin.String? = null,

    @Json(name = "deleted_messages")
    val deletedMessages: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponse? = null,
)
