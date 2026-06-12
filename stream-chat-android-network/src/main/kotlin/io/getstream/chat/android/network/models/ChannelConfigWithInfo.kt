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

import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 *
 */

data class ChannelConfigWithInfo (
    @Json(name = "automod")
    val automod: Automod,

    @Json(name = "automod_behavior")
    val automodBehavior: AutomodBehavior,

    @Json(name = "connect_events")
    val connectEvents: kotlin.Boolean,

    @Json(name = "count_messages")
    val countMessages: kotlin.Boolean,

    @Json(name = "created_at")
    val createdAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "custom_events")
    val customEvents: kotlin.Boolean,

    @Json(name = "delivery_events")
    val deliveryEvents: kotlin.Boolean,

    @Json(name = "mark_messages_pending")
    val markMessagesPending: kotlin.Boolean,

    @Json(name = "max_message_length")
    val maxMessageLength: kotlin.Int,

    @Json(name = "mutes")
    val mutes: kotlin.Boolean,

    @Json(name = "name")
    val name: kotlin.String,

    @Json(name = "polls")
    val polls: kotlin.Boolean,

    @Json(name = "push_notifications")
    val pushNotifications: kotlin.Boolean,

    @Json(name = "quotes")
    val quotes: kotlin.Boolean,

    @Json(name = "reactions")
    val reactions: kotlin.Boolean,

    @Json(name = "read_events")
    val readEvents: kotlin.Boolean,

    @Json(name = "reminders")
    val reminders: kotlin.Boolean,

    @Json(name = "replies")
    val replies: kotlin.Boolean,

    @Json(name = "search")
    val search: kotlin.Boolean,

    @Json(name = "shared_locations")
    val sharedLocations: kotlin.Boolean,

    @Json(name = "skip_last_msg_update_for_system_msgs")
    val skipLastMsgUpdateForSystemMsgs: kotlin.Boolean,

    @Json(name = "typing_events")
    val typingEvents: kotlin.Boolean,

    @Json(name = "updated_at")
    val updatedAt: org.threeten.bp.OffsetDateTime,

    @Json(name = "uploads")
    val uploads: kotlin.Boolean,

    @Json(name = "url_enrichment")
    val urlEnrichment: kotlin.Boolean,

    @Json(name = "user_message_reminders")
    val userMessageReminders: kotlin.Boolean,

    @Json(name = "commands")
    val commands: kotlin.collections.List<io.getstream.chat.android.network.models.Command> = emptyList(),

    @Json(name = "blocklist")
    val blocklist: kotlin.String? = null,

    @Json(name = "blocklist_behavior")
    val blocklistBehavior: BlocklistBehavior? = null,

    @Json(name = "partition_size")
    val partitionSize: kotlin.Int? = null,

    @Json(name = "partition_ttl")
    val partitionTtl: kotlin.String? = null,

    @Json(name = "push_level")
    val pushLevel: PushLevel? = null,

    @Json(name = "allowed_flag_reasons")
    val allowedFlagReasons: kotlin.collections.List<kotlin.String>? = emptyList(),

    @Json(name = "blocklists")
    val blocklists: kotlin.collections.List<io.getstream.chat.android.network.models.BlockListOptions>? = emptyList(),

    @Json(name = "automod_thresholds")
    val automodThresholds: io.getstream.chat.android.network.models.Thresholds? = null,

    @Json(name = "chat_preferences")
    val chatPreferences: io.getstream.chat.android.network.models.ChatPreferences? = null,

    @Json(name = "grants")
    val grants: kotlin.collections.Map<kotlin.String, kotlin.collections.List<kotlin.String>>? = emptyMap()
)
{

    /**
    * Automod Enum
    */
    sealed class Automod(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Automod = when (s) {
                    "AI" -> AI
                    "disabled" -> Disabled
                    "simple" -> Simple
                    else -> Unknown(s)
                }
            }
            object AI : Automod("AI")
            object Disabled : Automod("disabled")
            object Simple : Automod("simple")
            data class Unknown(val unknownValue: kotlin.String) : Automod(unknownValue)


        class AutomodAdapter : JsonAdapter<Automod>() {
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
    sealed class AutomodBehavior(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): AutomodBehavior = when (s) {
                    "block" -> Block
                    "flag" -> Flag
                    "shadow_block" -> ShadowBlock
                    else -> Unknown(s)
                }
            }
            object Block : AutomodBehavior("block")
            object Flag : AutomodBehavior("flag")
            object ShadowBlock : AutomodBehavior("shadow_block")
            data class Unknown(val unknownValue: kotlin.String) : AutomodBehavior(unknownValue)


        class AutomodBehaviorAdapter : JsonAdapter<AutomodBehavior>() {
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
    sealed class BlocklistBehavior(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): BlocklistBehavior = when (s) {
                    "block" -> Block
                    "flag" -> Flag
                    "shadow_block" -> ShadowBlock
                    else -> Unknown(s)
                }
            }
            object Block : BlocklistBehavior("block")
            object Flag : BlocklistBehavior("flag")
            object ShadowBlock : BlocklistBehavior("shadow_block")
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
