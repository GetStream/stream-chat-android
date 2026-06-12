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
 * Channel configuration overrides
 */

data class ChannelConfig (
    @Json(name = "blocklist")
    val blocklist: kotlin.String? = null,

    @Json(name = "blocklist_behavior")
    val blocklistBehavior: BlocklistBehavior? = null,

    @Json(name = "count_messages")
    val countMessages: kotlin.Boolean? = null,

    @Json(name = "max_message_length")
    val maxMessageLength: kotlin.Int? = null,

    @Json(name = "push_level")
    val pushLevel: PushLevel? = null,

    @Json(name = "quotes")
    val quotes: kotlin.Boolean? = null,

    @Json(name = "reactions")
    val reactions: kotlin.Boolean? = null,

    @Json(name = "replies")
    val replies: kotlin.Boolean? = null,

    @Json(name = "shared_locations")
    val sharedLocations: kotlin.Boolean? = null,

    @Json(name = "typing_events")
    val typingEvents: kotlin.Boolean? = null,

    @Json(name = "uploads")
    val uploads: kotlin.Boolean? = null,

    @Json(name = "url_enrichment")
    val urlEnrichment: kotlin.Boolean? = null,

    @Json(name = "user_message_reminders")
    val userMessageReminders: kotlin.Boolean? = null,

    @Json(name = "commands")
    val commands: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "chat_preferences")
    val chatPreferences: io.getstream.chat.android.network.models.ChatPreferences? = null,

    @Json(name = "grants")
    val grants: kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.String>>? = emptyMap()
)
{
    
    /**
    * BlocklistBehavior Enum
    */
    sealed class BlocklistBehavior(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): BlocklistBehavior = when (s) {
                    "block" -> Block
                    "flag" -> Flag
                    else -> Unknown(s)
                }
            }
            object Block : BlocklistBehavior("block")
            object Flag : BlocklistBehavior("flag")
            data class Unknown(val unknownValue: kotlin.String) : BlocklistBehavior(unknownValue)
        

        class BlocklistBehaviorAdapter : JsonAdapter<BlocklistBehavior>() {
            @FromJson
            override fun fromJson(reader: JsonReader): BlocklistBehavior? {
                val s = reader.nextString() ?: return null
                return BlocklistBehavior.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: BlocklistBehavior?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * PushLevel Enum
    */
    sealed class PushLevel(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): PushLevel = when (s) {
                    "all" -> All
                    "all_mentions" -> AllMentions
                    "direct_mentions" -> DirectMentions
                    "mentions" -> Mentions
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : PushLevel("all")
            object AllMentions : PushLevel("all_mentions")
            object DirectMentions : PushLevel("direct_mentions")
            object Mentions : PushLevel("mentions")
            object None : PushLevel("none")
            data class Unknown(val unknownValue: kotlin.String) : PushLevel(unknownValue)
        

        class PushLevelAdapter : JsonAdapter<PushLevel>() {
            @FromJson
            override fun fromJson(reader: JsonReader): PushLevel? {
                val s = reader.nextString() ?: return null
                return PushLevel.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: PushLevel?) {
                writer.value(value?.value)
            }
        }
    }    
}
