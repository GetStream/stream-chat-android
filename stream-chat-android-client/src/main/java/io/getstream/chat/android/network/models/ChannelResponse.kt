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
 * Represents channel in chat
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ChannelResponse(
    @Json(name = "cid")
    internal val cid: String,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "disabled")
    internal val disabled: Boolean,

    @Json(name = "frozen")
    internal val frozen: Boolean,

    @Json(name = "id")
    internal val id: String,

    @Json(name = "type")
    internal val type: String,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "auto_translation_enabled")
    internal val autoTranslationEnabled: Boolean? = null,

    @Json(name = "auto_translation_language")
    internal val autoTranslationLanguage: String? = null,

    @Json(name = "blocked")
    internal val blocked: Boolean? = null,

    @Json(name = "cooldown")
    internal val cooldown: Int? = null,

    @Json(name = "deleted_at")
    internal val deletedAt: java.util.Date? = null,

    @Json(name = "hidden")
    internal val hidden: Boolean? = null,

    @Json(name = "hide_messages_before")
    internal val hideMessagesBefore: java.util.Date? = null,

    @Json(name = "last_message_at")
    internal val lastMessageAt: java.util.Date? = null,

    @Json(name = "member_count")
    internal val memberCount: Int? = null,

    @Json(name = "message_count")
    internal val messageCount: Int? = null,

    @Json(name = "mute_expires_at")
    internal val muteExpiresAt: java.util.Date? = null,

    @Json(name = "muted")
    internal val muted: Boolean? = null,

    @Json(name = "team")
    internal val team: String? = null,

    @Json(name = "truncated_at")
    internal val truncatedAt: java.util.Date? = null,

    @Json(name = "filter_tags")
    internal val filterTags: List<String>? = emptyList(),

    @Json(name = "members")
    internal val members: List<ChannelMemberResponse>? = emptyList(),

    @Json(name = "own_capabilities")
    internal val ownCapabilities: List<ChannelOwnCapability>? = emptyList(),

    @Json(name = "config")
    internal val config: ChannelConfigWithInfo? = null,

    @Json(name = "created_by")
    internal val createdBy: UserResponse? = null,

    @Json(name = "truncated_by")
    internal val truncatedBy: UserResponse? = null,
)
