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
internal data class ChatPreferencesInput(
    @Json(name = "channel_mentions")
    internal val channelMentions: ChannelMentions? = null,

    @Json(name = "default_preference")
    internal val defaultPreference: DefaultPreference? = null,

    @Json(name = "direct_mentions")
    internal val directMentions: DirectMentions? = null,

    @Json(name = "group_mentions")
    internal val groupMentions: GroupMentions? = null,

    @Json(name = "here_mentions")
    internal val hereMentions: HereMentions? = null,

    @Json(name = "role_mentions")
    internal val roleMentions: RoleMentions? = null,

    @Json(name = "thread_replies")
    internal val threadReplies: ThreadReplies? = null,
) {

    /**
     * ChannelMentions Enum
     */
    internal sealed class ChannelMentions(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): ChannelMentions = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : ChannelMentions("all")
        internal object None : ChannelMentions("none")
        internal data class Unknown(val unknownValue: String) : ChannelMentions(unknownValue)

        internal class ChannelMentionsAdapter : JsonAdapter<ChannelMentions>() {
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
    internal sealed class DefaultPreference(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): DefaultPreference = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : DefaultPreference("all")
        internal object None : DefaultPreference("none")
        internal data class Unknown(val unknownValue: String) : DefaultPreference(unknownValue)

        internal class DefaultPreferenceAdapter : JsonAdapter<DefaultPreference>() {
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
    internal sealed class DirectMentions(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): DirectMentions = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : DirectMentions("all")
        internal object None : DirectMentions("none")
        internal data class Unknown(val unknownValue: String) : DirectMentions(unknownValue)

        internal class DirectMentionsAdapter : JsonAdapter<DirectMentions>() {
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
    internal sealed class GroupMentions(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): GroupMentions = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : GroupMentions("all")
        internal object None : GroupMentions("none")
        internal data class Unknown(val unknownValue: String) : GroupMentions(unknownValue)

        internal class GroupMentionsAdapter : JsonAdapter<GroupMentions>() {
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
    internal sealed class HereMentions(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): HereMentions = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : HereMentions("all")
        internal object None : HereMentions("none")
        internal data class Unknown(val unknownValue: String) : HereMentions(unknownValue)

        internal class HereMentionsAdapter : JsonAdapter<HereMentions>() {
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
    internal sealed class RoleMentions(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): RoleMentions = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : RoleMentions("all")
        internal object None : RoleMentions("none")
        internal data class Unknown(val unknownValue: String) : RoleMentions(unknownValue)

        internal class RoleMentionsAdapter : JsonAdapter<RoleMentions>() {
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
    internal sealed class ThreadReplies(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): ThreadReplies = when (s) {
                "all" -> All
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : ThreadReplies("all")
        internal object None : ThreadReplies("none")
        internal data class Unknown(val unknownValue: String) : ThreadReplies(unknownValue)

        internal class ThreadRepliesAdapter : JsonAdapter<ThreadReplies>() {
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
