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
data class VelocityFilterConfigRule (
    @Json(name = "action")
    val action: Action,

    @Json(name = "ban_duration")
    val banDuration: kotlin.Int,

    @Json(name = "cascading_action")
    val cascadingAction: CascadingAction,

    @Json(name = "cascading_threshold")
    val cascadingThreshold: kotlin.Int,

    @Json(name = "check_message_context")
    val checkMessageContext: kotlin.Boolean,

    @Json(name = "fast_spam_threshold")
    val fastSpamThreshold: kotlin.Int,

    @Json(name = "fast_spam_ttl")
    val fastSpamTtl: kotlin.Int,

    @Json(name = "ip_ban")
    val ipBan: kotlin.Boolean,

    @Json(name = "probation_period")
    val probationPeriod: kotlin.Int,

    @Json(name = "shadow_ban")
    val shadowBan: kotlin.Boolean,

    @Json(name = "slow_spam_threshold")
    val slowSpamThreshold: kotlin.Int,

    @Json(name = "slow_spam_ttl")
    val slowSpamTtl: kotlin.Int,

    @Json(name = "url_only")
    val urlOnly: kotlin.Boolean,

    @Json(name = "slow_spam_ban_duration")
    val slowSpamBanDuration: kotlin.Int? = null
)
{

    /**
    * Action Enum
    */
    sealed class Action(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): Action = when (s) {
                    "ban" -> Ban
                    "flag" -> Flag
                    "remove" -> Remove
                    "shadow" -> Shadow
                    else -> Unknown(s)
                }
            }
            object Ban : Action("ban")
            object Flag : Action("flag")
            object Remove : Action("remove")
            object Shadow : Action("shadow")
            data class Unknown(val unknownValue: kotlin.String) : Action(unknownValue)


        class ActionAdapter : JsonAdapter<Action>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Action? {
                val s = reader.nextString() ?: return null
                return Action.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Action?) {
                writer.value(value?.value)
            }
        }
    }
    /**
    * CascadingAction Enum
    */
    sealed class CascadingAction(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): CascadingAction = when (s) {
                    "ban" -> Ban
                    "flag" -> Flag
                    "remove" -> Remove
                    "shadow" -> Shadow
                    else -> Unknown(s)
                }
            }
            object Ban : CascadingAction("ban")
            object Flag : CascadingAction("flag")
            object Remove : CascadingAction("remove")
            object Shadow : CascadingAction("shadow")
            data class Unknown(val unknownValue: kotlin.String) : CascadingAction(unknownValue)


        class CascadingActionAdapter : JsonAdapter<CascadingAction>() {
            @FromJson
            override fun fromJson(reader: JsonReader): CascadingAction? {
                val s = reader.nextString() ?: return null
                return CascadingAction.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: CascadingAction?) {
                writer.value(value?.value)
            }
        }
    }
}
