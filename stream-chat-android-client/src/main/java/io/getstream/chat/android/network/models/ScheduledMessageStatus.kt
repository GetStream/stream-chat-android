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
 * [Status of a scheduled message.]
 */
/**
 * ScheduledMessageStatus Enum
 */
internal sealed class ScheduledMessageStatus(internal val value: String) {
    override fun toString(): String = value

    internal companion object {
        internal fun fromString(s: String): ScheduledMessageStatus = when (s) {
            "canceled" -> Canceled
            "failed" -> Failed
            "scheduled" -> Scheduled
            "sending" -> Sending
            "sent" -> Sent
            else -> Unknown(s)
        }
    }
    internal object Canceled : ScheduledMessageStatus("canceled")
    internal object Failed : ScheduledMessageStatus("failed")
    internal object Scheduled : ScheduledMessageStatus("scheduled")
    internal object Sending : ScheduledMessageStatus("sending")
    internal object Sent : ScheduledMessageStatus("sent")
    internal data class Unknown(val unknownValue: String) : ScheduledMessageStatus(unknownValue)

    internal class ScheduledMessageStatusAdapter : JsonAdapter<ScheduledMessageStatus>() {
        @FromJson
        override fun fromJson(reader: JsonReader): ScheduledMessageStatus? {
            val s = reader.nextString() ?: return null
            return ScheduledMessageStatus.fromString(s)
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: ScheduledMessageStatus?) {
            writer.value(value?.value)
        }
    }
}
