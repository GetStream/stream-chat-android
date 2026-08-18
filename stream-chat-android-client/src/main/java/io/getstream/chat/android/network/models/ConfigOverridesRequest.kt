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
 * Channel configuration overrides
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ConfigOverridesRequest(
    @Json(name = "blocklist")
    internal val blocklist: String? = null,

    @Json(name = "blocklist_behavior")
    internal val blocklistBehavior: BlocklistBehavior? = null,

    @Json(name = "count_messages")
    internal val countMessages: Boolean? = null,

    @Json(name = "max_message_length")
    internal val maxMessageLength: Int? = null,

    @Json(name = "push_level")
    internal val pushLevel: PushLevel? = null,

    @Json(name = "quotes")
    internal val quotes: Boolean? = null,

    @Json(name = "reactions")
    internal val reactions: Boolean? = null,

    @Json(name = "replies")
    internal val replies: Boolean? = null,

    @Json(name = "shared_locations")
    internal val sharedLocations: Boolean? = null,

    @Json(name = "typing_events")
    internal val typingEvents: Boolean? = null,

    @Json(name = "uploads")
    internal val uploads: Boolean? = null,

    @Json(name = "url_enrichment")
    internal val urlEnrichment: Boolean? = null,

    @Json(name = "user_message_reminders")
    internal val userMessageReminders: Boolean? = null,

    @Json(name = "commands")
    internal val commands: List<String>? = emptyList(),

    @Json(name = "chat_preferences")
    internal val chatPreferences: io.getstream.chat.android.network.models.ChatPreferences? = null,

    @Json(name = "grants")
    internal val grants: Map<String, List<String>>? = emptyMap(),
) {

    /**
     * BlocklistBehavior Enum
     */
    internal sealed class BlocklistBehavior(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): BlocklistBehavior = when (s) {
                "block" -> Block
                "flag" -> Flag
                else -> Unknown(s)
            }
        }
        internal object Block : BlocklistBehavior("block")
        internal object Flag : BlocklistBehavior("flag")
        internal data class Unknown(val unknownValue: String) : BlocklistBehavior(unknownValue)

        internal class BlocklistBehaviorAdapter : JsonAdapter<BlocklistBehavior>() {
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
    internal sealed class PushLevel(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): PushLevel = when (s) {
                "all" -> All
                "all_mentions" -> AllMentions
                "direct_mentions" -> DirectMentions
                "mentions" -> Mentions
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : PushLevel("all")
        internal object AllMentions : PushLevel("all_mentions")
        internal object DirectMentions : PushLevel("direct_mentions")
        internal object Mentions : PushLevel("mentions")
        internal object None : PushLevel("none")
        internal data class Unknown(val unknownValue: String) : PushLevel(unknownValue)

        internal class PushLevelAdapter : JsonAdapter<PushLevel>() {
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
