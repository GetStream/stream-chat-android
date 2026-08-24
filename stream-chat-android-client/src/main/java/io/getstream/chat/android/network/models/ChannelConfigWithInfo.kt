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
 *
 */
@com.squareup.moshi.JsonClass(generateAdapter = true)
internal data class ChannelConfigWithInfo(
    @Json(name = "automod")
    internal val automod: Automod,

    @Json(name = "automod_behavior")
    internal val automodBehavior: AutomodBehavior,

    @Json(name = "connect_events")
    internal val connectEvents: Boolean,

    @Json(name = "count_messages")
    internal val countMessages: Boolean,

    @Json(name = "created_at")
    internal val createdAt: java.util.Date,

    @Json(name = "custom_events")
    internal val customEvents: Boolean,

    @Json(name = "delivery_events")
    internal val deliveryEvents: Boolean,

    @Json(name = "mark_messages_pending")
    internal val markMessagesPending: Boolean,

    @Json(name = "max_message_length")
    internal val maxMessageLength: Int,

    @Json(name = "message_retention")
    internal val messageRetention: String,

    @Json(name = "mutes")
    internal val mutes: Boolean,

    @Json(name = "name")
    internal val name: String,

    @Json(name = "polls")
    internal val polls: Boolean,

    @Json(name = "push_notifications")
    internal val pushNotifications: Boolean,

    @Json(name = "quotes")
    internal val quotes: Boolean,

    @Json(name = "reactions")
    internal val reactions: Boolean,

    @Json(name = "read_events")
    internal val readEvents: Boolean,

    @Json(name = "reminders")
    internal val reminders: Boolean,

    @Json(name = "replies")
    internal val replies: Boolean,

    @Json(name = "search")
    internal val search: Boolean,

    @Json(name = "shared_locations")
    internal val sharedLocations: Boolean,

    @Json(name = "skip_last_msg_update_for_system_msgs")
    internal val skipLastMsgUpdateForSystemMsgs: Boolean,

    @Json(name = "typing_events")
    internal val typingEvents: Boolean,

    @Json(name = "updated_at")
    internal val updatedAt: java.util.Date,

    @Json(name = "uploads")
    internal val uploads: Boolean,

    @Json(name = "url_enrichment")
    internal val urlEnrichment: Boolean,

    @Json(name = "user_message_reminders")
    internal val userMessageReminders: Boolean,

    @Json(name = "commands")
    internal val commands: List<Command> = emptyList(),

    @Json(name = "blocklist")
    internal val blocklist: String? = null,

    @Json(name = "blocklist_behavior")
    internal val blocklistBehavior: BlocklistBehavior? = null,

    @Json(name = "partition_size")
    internal val partitionSize: Int? = null,

    @Json(name = "partition_ttl")
    internal val partitionTtl: String? = null,

    @Json(name = "push_level")
    internal val pushLevel: PushLevel? = null,

    @Json(name = "allowed_flag_reasons")
    internal val allowedFlagReasons: List<String>? = emptyList(),

    @Json(name = "blocklists")
    internal val blocklists: List<BlockListOptions>? = emptyList(),

    @Json(name = "automod_thresholds")
    internal val automodThresholds: Thresholds? = null,

    @Json(name = "chat_preferences")
    internal val chatPreferences: ChatPreferences? = null,

    @Json(name = "grants")
    internal val grants: Map<String, List<String>>? = emptyMap(),
) {

    /**
     * Automod Enum
     */
    internal sealed class Automod(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Automod = when (s) {
                "AI" -> AI
                "disabled" -> Disabled
                "simple" -> Simple
                else -> Unknown(s)
            }
        }
        internal object AI : Automod("AI")
        internal object Disabled : Automod("disabled")
        internal object Simple : Automod("simple")
        internal data class Unknown(val unknownValue: String) : Automod(unknownValue)

        internal class AutomodAdapter : JsonAdapter<Automod>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Automod? {
                val s = reader.nextString() ?: return null
                return Automod.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Automod?) {
                writer.value(value?.value)
            }
        }
    }

    /**
     * AutomodBehavior Enum
     */
    internal sealed class AutomodBehavior(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): AutomodBehavior = when (s) {
                "block" -> Block
                "flag" -> Flag
                "shadow_block" -> ShadowBlock
                else -> Unknown(s)
            }
        }
        internal object Block : AutomodBehavior("block")
        internal object Flag : AutomodBehavior("flag")
        internal object ShadowBlock : AutomodBehavior("shadow_block")
        internal data class Unknown(val unknownValue: String) : AutomodBehavior(unknownValue)

        internal class AutomodBehaviorAdapter : JsonAdapter<AutomodBehavior>() {
            @FromJson
            override fun fromJson(reader: JsonReader): AutomodBehavior? {
                val s = reader.nextString() ?: return null
                return AutomodBehavior.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: AutomodBehavior?) {
                writer.value(value?.value)
            }
        }
    }

    /**
     * BlocklistBehavior Enum
     */
    internal sealed class BlocklistBehavior(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): BlocklistBehavior = when (s) {
                "block" -> Block
                "flag" -> Flag
                "shadow_block" -> ShadowBlock
                else -> Unknown(s)
            }
        }
        internal object Block : BlocklistBehavior("block")
        internal object Flag : BlocklistBehavior("flag")
        internal object ShadowBlock : BlocklistBehavior("shadow_block")
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
