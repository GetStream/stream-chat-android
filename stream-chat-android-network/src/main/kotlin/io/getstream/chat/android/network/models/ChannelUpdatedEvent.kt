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
 * Emitted when a channel is successfully updated.
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ChannelUpdatedEvent (
    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "channel")
    val channel: io.getstream.chat.android.network.models.ChannelResponse,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "channel_id")
    val channelId: kotlin.String? = null,

    @Json(name = "channel_member_count")
    val channelMemberCount: kotlin.Int? = null,

    @Json(name = "channel_message_count")
    val channelMessageCount: kotlin.Int? = null,

    @Json(name = "channel_type")
    val channelType: kotlin.String? = null,

    @Json(name = "cid")
    val cid: kotlin.String? = null,

    @Json(name = "message_id")
    val messageId: kotlin.String? = null,

    @Json(name = "received_at")
    val receivedAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "team")
    val team: kotlin.String? = null,

    @Json(name = "channel_custom")
    val channelCustom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "message")
    val message: io.getstream.chat.android.network.models.MessageResponse? = null,

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponseCommonFields? = null
)
: io.getstream.chat.android.network.models.ChatEvent()
{

    override fun getEventType(): kotlin.String {
        return type
    }
}
