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
data class ChatDraftPayloadResponse (
    @Json(name = "id")
    val id: kotlin.String,

    @Json(name = "text")
    val text: kotlin.String,

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?> = emptyMap(),

    @Json(name = "html")
    val html: kotlin.String? = null,

    @Json(name = "mml")
    val mml: kotlin.String? = null,

    @Json(name = "parent_id")
    val parentId: kotlin.String? = null,

    @Json(name = "poll_id")
    val pollId: kotlin.String? = null,

    @Json(name = "quoted_message_id")
    val quotedMessageId: kotlin.String? = null,

    @Json(name = "show_in_channel")
    val showInChannel: kotlin.Boolean? = null,

    @Json(name = "silent")
    val silent: kotlin.Boolean? = null,

    @Json(name = "type")
    val type: kotlin.String? = null,

    @Json(name = "attachments")
    val attachments: kotlin.collections.List<io.getstream.chat.android.network.models.Attachment>? = emptyList(),

    @Json(name = "mentioned_users")
    val mentionedUsers: kotlin.collections.List<io.getstream.chat.android.network.models.UserResponse>? = emptyList()
)
