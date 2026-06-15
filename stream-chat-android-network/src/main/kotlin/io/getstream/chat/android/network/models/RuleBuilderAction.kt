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
data class RuleBuilderAction (
    @Json(name = "skip_inbox")
    val skipInbox: kotlin.Boolean? = null,

    @Json(name = "type")
    val type: Type? = null,

    @Json(name = "ban_options")
    val banOptions: io.getstream.chat.android.network.models.BanOptions? = null,

    @Json(name = "call_options")
    val callOptions: io.getstream.chat.android.network.models.CallActionOptions? = null,

    @Json(name = "flag_user_options")
    val flagUserOptions: io.getstream.chat.android.network.models.FlagUserOptions? = null
)
{
    
    /**
    * Type Enum
    */
    sealed class Type(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Type = when (s) {
                    "ban_user" -> BanUser
                    "block_content" -> BlockContent
                    "blur" -> Blur
                    "bounce_content" -> BounceContent
                    "bounce_flag_content" -> BounceFlagContent
                    "bounce_remove_content" -> BounceRemoveContent
                    "call_blur" -> CallBlur
                    "call_warning" -> CallWarning
                    "end_call" -> EndCall
                    "flag_content" -> FlagContent
                    "flag_user" -> FlagUser
                    "kick_user" -> KickUser
                    "mute_audio" -> MuteAudio
                    "mute_video" -> MuteVideo
                    "shadow_content" -> ShadowContent
                    "warning" -> Warning
                    "webhook_only" -> WebhookOnly
                    else -> Unknown(s)
                }
            }
            object BanUser : Type("ban_user")
            object BlockContent : Type("block_content")
            object Blur : Type("blur")
            object BounceContent : Type("bounce_content")
            object BounceFlagContent : Type("bounce_flag_content")
            object BounceRemoveContent : Type("bounce_remove_content")
            object CallBlur : Type("call_blur")
            object CallWarning : Type("call_warning")
            object EndCall : Type("end_call")
            object FlagContent : Type("flag_content")
            object FlagUser : Type("flag_user")
            object KickUser : Type("kick_user")
            object MuteAudio : Type("mute_audio")
            object MuteVideo : Type("mute_video")
            object ShadowContent : Type("shadow_content")
            object Warning : Type("warning")
            object WebhookOnly : Type("webhook_only")
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
