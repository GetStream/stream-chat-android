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
 *
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class MarkReadResponseEvent (
    @Json(name = "channel_id")
    val channelId: kotlin.String,

    @Json(name = "channel_type")
    val channelType: kotlin.String,

    @Json(name = "cid")
    val cid: kotlin.String,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "type")
    val type: kotlin.String,

    @Json(name = "channel_last_message_at")
    val channelLastMessageAt: org.threeten.bp.OffsetDateTime? = null,

    @Json(name = "last_read_message_id")
    val lastReadMessageId: kotlin.String? = null,

    @Json(name = "team")
    val team: kotlin.String? = null,

    @Json(name = "channel")
    val channel: io.getstream.chat.android.network.models.ChannelResponse? = null,

    @Json(name = "thread")
    val thread: io.getstream.chat.android.network.models.ThreadResponse? = null,

    @Json(name = "user")
    val user: io.getstream.chat.android.network.models.UserResponseCommonFields? = null
)
