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
 * The message a scheduled message will send
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ScheduledMessageContentResponse(
    @Json(name = "text")
    internal val text: String,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "html")
    internal val html: String? = null,

    @Json(name = "mentioned_channel")
    internal val mentionedChannel: Boolean? = null,

    @Json(name = "mentioned_here")
    internal val mentionedHere: Boolean? = null,

    @Json(name = "mml")
    internal val mml: String? = null,

    @Json(name = "parent_id")
    internal val parentId: String? = null,

    @Json(name = "pin_expires")
    internal val pinExpires: java.util.Date? = null,

    @Json(name = "pinned")
    internal val pinned: Boolean? = null,

    @Json(name = "poll_id")
    internal val pollId: String? = null,

    @Json(name = "quoted_message_id")
    internal val quotedMessageId: String? = null,

    @Json(name = "show_in_channel")
    internal val showInChannel: Boolean? = null,

    @Json(name = "silent")
    internal val silent: Boolean? = null,

    @Json(name = "type")
    internal val type: String? = null,

    @Json(name = "attachments")
    internal val attachments: List<io.getstream.chat.android.network.models.Attachment>? = null,

    @Json(name = "mentioned_group_ids")
    internal val mentionedGroupIds: List<String>? = null,

    @Json(name = "mentioned_roles")
    internal val mentionedRoles: List<String>? = null,

    @Json(name = "mentioned_users")
    internal val mentionedUsers: List<String>? = null,

    @Json(name = "restricted_visibility")
    internal val restrictedVisibility: List<String>? = null,

    @Json(name = "shared_location")
    internal val sharedLocation: io.getstream.chat.android.network.models.ScheduledMessageLocation? = null,
)
