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
data class BulkActionAppealsRequest (
    @Json(name = "action_type")
    val actionType: ActionType,

    @Json(name = "appeal_ids")
    val appealIds: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "mark_reviewed")
    val markReviewed: io.getstream.chat.android.network.models.MarkReviewedRequestPayload? = null,

    @Json(name = "reject_appeal")
    val rejectAppeal: io.getstream.chat.android.network.models.RejectAppealRequestPayload? = null,

    @Json(name = "restore")
    val restore: io.getstream.chat.android.network.models.RestoreActionRequestPayload? = null,

    @Json(name = "unban")
    val unban: io.getstream.chat.android.network.models.UnbanActionRequestPayload? = null,

    @Json(name = "unblock")
    val unblock: io.getstream.chat.android.network.models.UnblockActionRequestPayload? = null
)
{

    /**
    * ActionType Enum
    */
    sealed class ActionType(val value: kotlin.String) {
            override fun toString(): String = value

            companion object {
                fun fromString(s: kotlin.String): ActionType = when (s) {
                    "mark_reviewed" -> MarkReviewed
                    "reject_appeal" -> RejectAppeal
                    "restore" -> Restore
                    "unban" -> Unban
                    "unblock" -> Unblock
                    else -> Unknown(s)
                }
            }
            object MarkReviewed : ActionType("mark_reviewed")
            object RejectAppeal : ActionType("reject_appeal")
            object Restore : ActionType("restore")
            object Unban : ActionType("unban")
            object Unblock : ActionType("unblock")
            data class Unknown(val unknownValue: kotlin.String) : ActionType(unknownValue)


        class ActionTypeAdapter : JsonAdapter<ActionType>() {
            @FromJson
            override fun fromJson(reader: JsonReader): ActionType? {
                val s = reader.nextString() ?: return null
                return ActionType.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: ActionType?) {
                writer.value(value?.value)
            }
        }
    }
}
