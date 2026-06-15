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
data class PushPreferenceInput (
    @Json(name = "call_level")
    val callLevel: CallLevel? = null,

    @Json(name = "channel_cid")
    val channelCid: kotlin.String? = null,

    @Json(name = "chat_level")
    val chatLevel: ChatLevel? = null,

    @Json(name = "disabled_until")
    val disabledUntil: java.util.Date? = null,

    @Json(name = "feeds_level")
    val feedsLevel: FeedsLevel? = null,

    @Json(name = "remove_disable")
    val removeDisable: kotlin.Boolean? = null,

    @Json(name = "user_id")
    val userId: kotlin.String? = null,

    @Json(name = "chat_preferences")
    val chatPreferences: io.getstream.chat.android.network.models.ChatPreferencesInput? = null,

    @Json(name = "feeds_preferences")
    val feedsPreferences: io.getstream.chat.android.network.models.FeedsPreferences? = null
)
{
    
    /**
    * CallLevel Enum
    */
    sealed class CallLevel(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): CallLevel = when (s) {
                    "all" -> All
                    "default" -> Default
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : CallLevel("all")
            object Default : CallLevel("default")
            object None : CallLevel("none")
            data class Unknown(val unknownValue: kotlin.String) : CallLevel(unknownValue)
        

        class CallLevelAdapter : JsonAdapter<CallLevel>() {
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
    sealed class ChatLevel(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): ChatLevel = when (s) {
                    "all" -> All
                    "all_mentions" -> AllMentions
                    "default" -> Default
                    "direct_mentions" -> DirectMentions
                    "mentions" -> Mentions
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : ChatLevel("all")
            object AllMentions : ChatLevel("all_mentions")
            object Default : ChatLevel("default")
            object DirectMentions : ChatLevel("direct_mentions")
            object Mentions : ChatLevel("mentions")
            object None : ChatLevel("none")
            data class Unknown(val unknownValue: kotlin.String) : ChatLevel(unknownValue)
        

        class ChatLevelAdapter : JsonAdapter<ChatLevel>() {
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
    sealed class FeedsLevel(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): FeedsLevel = when (s) {
                    "all" -> All
                    "default" -> Default
                    "none" -> None
                    else -> Unknown(s)
                }
            }
            object All : FeedsLevel("all")
            object Default : FeedsLevel("default")
            object None : FeedsLevel("none")
            data class Unknown(val unknownValue: kotlin.String) : FeedsLevel(unknownValue)
        

        class FeedsLevelAdapter : JsonAdapter<FeedsLevel>() {
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
