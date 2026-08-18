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
internal data class PushPreferenceInput(
    @Json(name = "call_level")
    internal val callLevel: CallLevel? = null,

    @Json(name = "channel_cid")
    internal val channelCid: String? = null,

    @Json(name = "chat_level")
    internal val chatLevel: ChatLevel? = null,

    @Json(name = "disabled_until")
    internal val disabledUntil: java.util.Date? = null,

    @Json(name = "feeds_level")
    internal val feedsLevel: FeedsLevel? = null,

    @Json(name = "remove_disable")
    internal val removeDisable: Boolean? = null,

    @Json(name = "user_id")
    internal val userId: String? = null,

    @Json(name = "chat_preferences")
    internal val chatPreferences: io.getstream.chat.android.network.models.ChatPreferencesInput? = null,

    @Json(name = "feeds_preferences")
    internal val feedsPreferences: io.getstream.chat.android.network.models.FeedsPreferences? = null,
) {

    /**
     * CallLevel Enum
     */
    internal sealed class CallLevel(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): CallLevel = when (s) {
                "all" -> All
                "default" -> Default
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : CallLevel("all")
        internal object Default : CallLevel("default")
        internal object None : CallLevel("none")
        internal data class Unknown(val unknownValue: String) : CallLevel(unknownValue)

        internal class CallLevelAdapter : JsonAdapter<CallLevel>() {
            @FromJson
            override fun fromJson(reader: JsonReader): CallLevel? {
                val s = reader.nextString() ?: return null
                return CallLevel.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: CallLevel?) {
                writer.value(value?.value)
            }
        }
    }

    /**
     * ChatLevel Enum
     */
    internal sealed class ChatLevel(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): ChatLevel = when (s) {
                "all" -> All
                "all_mentions" -> AllMentions
                "default" -> Default
                "direct_mentions" -> DirectMentions
                "mentions" -> Mentions
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : ChatLevel("all")
        internal object AllMentions : ChatLevel("all_mentions")
        internal object Default : ChatLevel("default")
        internal object DirectMentions : ChatLevel("direct_mentions")
        internal object Mentions : ChatLevel("mentions")
        internal object None : ChatLevel("none")
        internal data class Unknown(val unknownValue: String) : ChatLevel(unknownValue)

        internal class ChatLevelAdapter : JsonAdapter<ChatLevel>() {
            @FromJson
            override fun fromJson(reader: JsonReader): ChatLevel? {
                val s = reader.nextString() ?: return null
                return ChatLevel.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: ChatLevel?) {
                writer.value(value?.value)
            }
        }
    }

    /**
     * FeedsLevel Enum
     */
    internal sealed class FeedsLevel(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): FeedsLevel = when (s) {
                "all" -> All
                "default" -> Default
                "none" -> None
                else -> Unknown(s)
            }
        }
        internal object All : FeedsLevel("all")
        internal object Default : FeedsLevel("default")
        internal object None : FeedsLevel("none")
        internal data class Unknown(val unknownValue: String) : FeedsLevel(unknownValue)

        internal class FeedsLevelAdapter : JsonAdapter<FeedsLevel>() {
            @FromJson
            override fun fromJson(reader: JsonReader): FeedsLevel? {
                val s = reader.nextString() ?: return null
                return FeedsLevel.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: FeedsLevel?) {
                writer.value(value?.value)
            }
        }
    }
}
