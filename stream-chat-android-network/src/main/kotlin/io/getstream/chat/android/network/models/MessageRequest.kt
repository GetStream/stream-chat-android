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
 * Message data for creating or updating a message
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class MessageRequest (
    @Json(name = "id")
    val id: kotlin.String? = null,

    @Json(name = "mentioned_channel")
    val mentionedChannel: kotlin.Boolean? = null,

    @Json(name = "mentioned_here")
    val mentionedHere: kotlin.Boolean? = null,

    @Json(name = "mml")
    val mml: kotlin.String? = null,

    @Json(name = "parent_id")
    val parentId: kotlin.String? = null,

    @Json(name = "pin_expires")
    val pinExpires: java.util.Date? = null,

    @Json(name = "pinned")
    val pinned: kotlin.Boolean? = null,

    @Json(name = "pinned_at")
    val pinnedAt: java.util.Date? = null,

    @Json(name = "poll_id")
    val pollId: kotlin.String? = null,

    @Json(name = "quoted_message_id")
    val quotedMessageId: kotlin.String? = null,

    @Json(name = "show_in_channel")
    val showInChannel: kotlin.Boolean? = null,

    @Json(name = "silent")
    val silent: kotlin.Boolean? = null,

    @Json(name = "text")
    val text: kotlin.String? = null,

    @Json(name = "type")
    val type: Type? = null,

    @Json(name = "attachments")
    val attachments: kotlin.collections.List<io.getstream.chat.android.network.models.Attachment>? = emptyList(),

    @Json(name = "mentioned_group_ids")
    val mentionedGroupIds: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "mentioned_roles")
    val mentionedRoles: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "mentioned_users")
    val mentionedUsers: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "restricted_visibility")
    val restrictedVisibility: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "custom")
    val custom: kotlin.collections.Map<kotlin.String, Any?>? = emptyMap(),

    @Json(name = "shared_location")
    val sharedLocation: io.getstream.chat.android.network.models.SharedLocation? = null
)
{
    
    /**
    * Type Enum
    */
    sealed class Type(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Type = when (s) {
                    "''" -> Empty
                    "regular" -> Regular
                    "system" -> System
                    else -> Unknown(s)
                }
            }
            object Empty : Type("''")
            object Regular : Type("regular")
            object System : Type("system")
            data class Unknown(val unknownValue: kotlin.String) : Type(unknownValue)
        

        class TypeAdapter : JsonAdapter<Type>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Type? {
                val s = reader.nextString() ?: return null
                return Type.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Type?) {
                writer.value(value?.value)
            }
        }
    }    
}
