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
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * [Why sending a scheduled message failed.]
 */
/**
 * ScheduledMessageFailureReason Enum
 */
internal sealed class ScheduledMessageFailureReason(internal val value: String) {
    override fun toString(): String = value

    internal companion object {
        internal fun fromString(s: String): ScheduledMessageFailureReason = when (s) {
            "banned" -> Banned
            "channel_deleted" -> ChannelDeleted
            "channel_frozen" -> ChannelFrozen
            "expired" -> Expired
            "hook_rejected" -> HookRejected
            "internal_error" -> InternalError
            "invalid_message" -> InvalidMessage
            "moderation_blocked" -> ModerationBlocked
            "no_permission" -> NoPermission
            "pending_not_supported" -> PendingNotSupported
            "user_deleted" -> UserDeleted
            else -> Unknown(s)
        }
    }
    internal object Banned : ScheduledMessageFailureReason("banned")
    internal object ChannelDeleted : ScheduledMessageFailureReason("channel_deleted")
    internal object ChannelFrozen : ScheduledMessageFailureReason("channel_frozen")
    internal object Expired : ScheduledMessageFailureReason("expired")
    internal object HookRejected : ScheduledMessageFailureReason("hook_rejected")
    internal object InternalError : ScheduledMessageFailureReason("internal_error")
    internal object InvalidMessage : ScheduledMessageFailureReason("invalid_message")
    internal object ModerationBlocked : ScheduledMessageFailureReason("moderation_blocked")
    internal object NoPermission : ScheduledMessageFailureReason("no_permission")
    internal object PendingNotSupported : ScheduledMessageFailureReason("pending_not_supported")
    internal object UserDeleted : ScheduledMessageFailureReason("user_deleted")
    internal data class Unknown(val unknownValue: String) : ScheduledMessageFailureReason(unknownValue)

    internal class ScheduledMessageFailureReasonAdapter : JsonAdapter<ScheduledMessageFailureReason>() {
        @FromJson
        override fun fromJson(reader: JsonReader): ScheduledMessageFailureReason? {
            val s = reader.nextString() ?: return null
            return ScheduledMessageFailureReason.fromString(s)
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: ScheduledMessageFailureReason?) {
            writer.value(value?.value)
        }
    }
}
