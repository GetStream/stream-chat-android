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
 * Emitted when a channel is successfully shown.
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ChannelVisibleEvent(
    @Json(name = "created_at")
    internal val createdAt: io.getstream.chat.android.network.infrastructure.ExactDate,

    // Patched: the spec marks `channel` required, but backends before v239.47.0 (CHA-3482, chat#17545) replay channel.visible on /sync
    // without it, and a parse failure fails the whole /sync response. On regen, make it required again only once every
    // region runs v239.47.0 or later.
    @Json(name = "channel")
    internal val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "custom")
    internal val custom: Map<String, Any?> = emptyMap(),

    @Json(name = "type")
    internal val type: String = "channel.visible",

    @Json(name = "channel_id")
    internal val channelId: String? = null,

    @Json(name = "channel_member_count")
    internal val channelMemberCount: Int? = null,

    @Json(name = "channel_message_count")
    internal val channelMessageCount: Int? = null,

    @Json(name = "channel_type")
    internal val channelType: String? = null,

    @Json(name = "cid")
    internal val cid: String? = null,

    @Json(name = "received_at")
    internal val receivedAt: java.util.Date? = null,

    @Json(name = "team")
    internal val team: String? = null,

    @Json(name = "channel_custom")
    internal val channelCustom: Map<String, Any?>? = null,

    @Json(name = "user")
    internal val user: io.getstream.chat.android.network.models.UserResponseCommonFields? = null,
) :
    io.getstream.chat.android.network.models.WSClientEvent, io.getstream.chat.android.network.models.WSEvent {

    override fun getWSClientEventType(): String {
        return type
    }

    override fun getWSEventType(): String {
        return type
    }
}
