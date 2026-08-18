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
internal data class MessageRequest(
    @Json(name = "id")
    internal val id: String? = null,

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

    @Json(name = "pinned_at")
    internal val pinnedAt: java.util.Date? = null,

    @Json(name = "poll_id")
    internal val pollId: String? = null,

    @Json(name = "quoted_message_id")
    internal val quotedMessageId: String? = null,

    @Json(name = "show_in_channel")
    internal val showInChannel: Boolean? = null,

    @Json(name = "silent")
    internal val silent: Boolean? = null,

    @Json(name = "text")
    internal val text: String? = null,

    @Json(name = "type")
    internal val type: Type? = null,

    @Json(name = "attachments")
    internal val attachments: List<io.getstream.chat.android.network.models.Attachment>? = emptyList(),

    @Json(name = "mentioned_group_ids")
    internal val mentionedGroupIds: List<String>? = emptyList(),

    @Json(name = "mentioned_roles")
    internal val mentionedRoles: List<String>? = emptyList(),

    @Json(name = "mentioned_users")
    internal val mentionedUsers: List<String>? = emptyList(),

    @Json(name = "restricted_visibility")
    internal val restrictedVisibility: List<String>? = emptyList(),

    @Json(name = "custom")
    internal val custom: Map<String, Any?>? = emptyMap(),

    @Json(name = "shared_location")
    internal val sharedLocation: io.getstream.chat.android.network.models.SharedLocation? = null,
) {

    /**
     * Type Enum
     */
    internal sealed class Type(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Type = when (s) {
                "regular" -> Regular
                "system" -> System
                else -> Unknown(s)
            }
        }
        internal object Regular : Type("regular")
        internal object System : Type("system")
        internal data class Unknown(val unknownValue: String) : Type(unknownValue)

        internal class TypeAdapter : JsonAdapter<Type>() {
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
