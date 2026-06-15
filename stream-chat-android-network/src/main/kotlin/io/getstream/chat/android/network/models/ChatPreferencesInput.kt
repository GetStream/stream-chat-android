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
 * 
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ChatPreferencesInput (
    @Json(name = "channel_mentions")
    val channelMentions: ChannelMentions? = null,

    @Json(name = "default_preference")
    val defaultPreference: DefaultPreference? = null,

    @Json(name = "direct_mentions")
    val directMentions: DirectMentions? = null,

    @Json(name = "group_mentions")
    val groupMentions: GroupMentions? = null,

    @Json(name = "here_mentions")
    val hereMentions: HereMentions? = null,

    @Json(name = "role_mentions")
    val roleMentions: RoleMentions? = null,

    @Json(name = "thread_replies")
    val threadReplies: ThreadReplies? = null
)
{
    
    /**
    * ChannelMentions Enum
    */
    sealed class ChannelMentions(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): ChannelMentions = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : ChannelMentions("all")
            object None : ChannelMentions("none")
            data class Unknown(val unknownValue: kotlin.String) : ChannelMentions(unknownValue)
        

        class ChannelMentionsAdapter : JsonAdapter<ChannelMentions>() {
            @FromJson
            override fun fromJson(reader: JsonReader): ChannelMentions? {
                val s = reader.nextString() ?: return null
                return ChannelMentions.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: ChannelMentions?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * DefaultPreference Enum
    */
    sealed class DefaultPreference(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): DefaultPreference = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : DefaultPreference("all")
            object None : DefaultPreference("none")
            data class Unknown(val unknownValue: kotlin.String) : DefaultPreference(unknownValue)
        

        class DefaultPreferenceAdapter : JsonAdapter<DefaultPreference>() {
            @FromJson
            override fun fromJson(reader: JsonReader): DefaultPreference? {
                val s = reader.nextString() ?: return null
                return DefaultPreference.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: DefaultPreference?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * DirectMentions Enum
    */
    sealed class DirectMentions(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): DirectMentions = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : DirectMentions("all")
            object None : DirectMentions("none")
            data class Unknown(val unknownValue: kotlin.String) : DirectMentions(unknownValue)
        

        class DirectMentionsAdapter : JsonAdapter<DirectMentions>() {
            @FromJson
            override fun fromJson(reader: JsonReader): DirectMentions? {
                val s = reader.nextString() ?: return null
                return DirectMentions.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: DirectMentions?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * GroupMentions Enum
    */
    sealed class GroupMentions(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): GroupMentions = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : GroupMentions("all")
            object None : GroupMentions("none")
            data class Unknown(val unknownValue: kotlin.String) : GroupMentions(unknownValue)
        

        class GroupMentionsAdapter : JsonAdapter<GroupMentions>() {
            @FromJson
            override fun fromJson(reader: JsonReader): GroupMentions? {
                val s = reader.nextString() ?: return null
                return GroupMentions.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: GroupMentions?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * HereMentions Enum
    */
    sealed class HereMentions(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): HereMentions = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : HereMentions("all")
            object None : HereMentions("none")
            data class Unknown(val unknownValue: kotlin.String) : HereMentions(unknownValue)
        

        class HereMentionsAdapter : JsonAdapter<HereMentions>() {
            @FromJson
            override fun fromJson(reader: JsonReader): HereMentions? {
                val s = reader.nextString() ?: return null
                return HereMentions.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: HereMentions?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * RoleMentions Enum
    */
    sealed class RoleMentions(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): RoleMentions = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : RoleMentions("all")
            object None : RoleMentions("none")
            data class Unknown(val unknownValue: kotlin.String) : RoleMentions(unknownValue)
        

        class RoleMentionsAdapter : JsonAdapter<RoleMentions>() {
            @FromJson
            override fun fromJson(reader: JsonReader): RoleMentions? {
                val s = reader.nextString() ?: return null
                return RoleMentions.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: RoleMentions?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * ThreadReplies Enum
    */
    sealed class ThreadReplies(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): ThreadReplies = when (s) {
                    "all" -> All
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : ThreadReplies("all")
            object None : ThreadReplies("none")
            data class Unknown(val unknownValue: kotlin.String) : ThreadReplies(unknownValue)
        

        class ThreadRepliesAdapter : JsonAdapter<ThreadReplies>() {
            @FromJson
            override fun fromJson(reader: JsonReader): ThreadReplies? {
                val s = reader.nextString() ?: return null
                return ThreadReplies.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: ThreadReplies?) {
                writer.value(value?.value)
            }
        }
    }    
}
