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

data class SubmitActionRequest (
    @Json(name = "action_type")
    val actionType: ActionType,

    @Json(name = "appeal_id")
    val appealId: kotlin.String? = null,

    @Json(name = "item_id")
    val itemId: kotlin.String? = null,

    @Json(name = "ban")
    val ban: io.getstream.chat.android.network.models.BanActionRequestPayload? = null,

    @Json(name = "block")
    val block: io.getstream.chat.android.network.models.BlockActionRequestPayload? = null,

    @Json(name = "bypass")
    val bypass: io.getstream.chat.android.network.models.BypassActionRequest? = null,

    @Json(name = "custom")
    val custom: io.getstream.chat.android.network.models.CustomActionRequestPayload? = null,

    @Json(name = "delete_activity")
    val deleteActivity: io.getstream.chat.android.network.models.DeleteActivityRequestPayload? = null,

    @Json(name = "delete_comment")
    val deleteComment: io.getstream.chat.android.network.models.DeleteCommentRequestPayload? = null,

    @Json(name = "delete_message")
    val deleteMessage: io.getstream.chat.android.network.models.DeleteMessageRequestPayload? = null,

    @Json(name = "delete_reaction")
    val deleteReaction: io.getstream.chat.android.network.models.DeleteReactionRequestPayload? = null,

    @Json(name = "delete_user")
    val deleteUser: io.getstream.chat.android.network.models.DeleteUserRequestPayload? = null,

    @Json(name = "escalate")
    val escalate: io.getstream.chat.android.network.models.EscalatePayload? = null,

    @Json(name = "flag")
    val flag: io.getstream.chat.android.network.models.FlagRequest? = null,

    @Json(name = "mark_reviewed")
    val markReviewed: io.getstream.chat.android.network.models.MarkReviewedRequestPayload? = null,

    @Json(name = "reject_appeal")
    val rejectAppeal: io.getstream.chat.android.network.models.RejectAppealRequestPayload? = null,

    @Json(name = "restore")
    val restore: io.getstream.chat.android.network.models.RestoreActionRequestPayload? = null,

    @Json(name = "shadow_block")
    val shadowBlock: io.getstream.chat.android.network.models.ShadowBlockActionRequestPayload? = null,

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
                    "ban" -> Ban
                    "block" -> Block
                    "bypass" -> Bypass
                    "custom" -> Custom
                    "de_escalate" -> DeEscalate
                    "delete_activity" -> DeleteActivity
                    "delete_comment" -> DeleteComment
                    "delete_message" -> DeleteMessage
                    "delete_reaction" -> DeleteReaction
                    "delete_user" -> DeleteUser
                    "end_call" -> EndCall
                    "escalate" -> Escalate
                    "flag" -> Flag
                    "kick_user" -> KickUser
                    "mark_reviewed" -> MarkReviewed
                    "reject_appeal" -> RejectAppeal
                    "restore" -> Restore
                    "shadow_block" -> ShadowBlock
                    "unban" -> Unban
                    "unblock" -> Unblock
                    "unmask" -> Unmask
                    else -> Unknown(s)
                }
            }
            object Ban : ActionType("ban")
            object Block : ActionType("block")
            object Bypass : ActionType("bypass")
            object Custom : ActionType("custom")
            object DeEscalate : ActionType("de_escalate")
            object DeleteActivity : ActionType("delete_activity")
            object DeleteComment : ActionType("delete_comment")
            object DeleteMessage : ActionType("delete_message")
            object DeleteReaction : ActionType("delete_reaction")
            object DeleteUser : ActionType("delete_user")
            object EndCall : ActionType("end_call")
            object Escalate : ActionType("escalate")
            object Flag : ActionType("flag")
            object KickUser : ActionType("kick_user")
            object MarkReviewed : ActionType("mark_reviewed")
            object RejectAppeal : ActionType("reject_appeal")
            object Restore : ActionType("restore")
            object ShadowBlock : ActionType("shadow_block")
            object Unban : ActionType("unban")
            object Unblock : ActionType("unblock")
            object Unmask : ActionType("unmask")
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
