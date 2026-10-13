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
internal data class BanRequest(
    @Json(name = "target_user_id")
    internal val targetUserId: String,

    @Json(name = "ban_from_future_channels")
    internal val banFromFutureChannels: Boolean? = null,

    @Json(name = "channel_cid")
    internal val channelCid: String? = null,

    @Json(name = "delete_messages")
    internal val deleteMessages: DeleteMessages? = null,

    @Json(name = "delete_reactions")
    internal val deleteReactions: Boolean? = null,

    @Json(name = "ip_ban")
    internal val ipBan: Boolean? = null,

    @Json(name = "reason")
    internal val reason: String? = null,

    @Json(name = "shadow")
    internal val shadow: Boolean? = null,

    @Json(name = "timeout")
    internal val timeout: Int? = null,
) {

    /**
     * DeleteMessages Enum
     */
    internal sealed class DeleteMessages(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): DeleteMessages = when (s) {
                "hard" -> Hard
                "pruning" -> Pruning
                "soft" -> Soft
                else -> Unknown(s)
            }
        }
        internal object Hard : DeleteMessages("hard")
        internal object Pruning : DeleteMessages("pruning")
        internal object Soft : DeleteMessages("soft")
        internal data class Unknown(val unknownValue: String) : DeleteMessages(unknownValue)

        internal class DeleteMessagesAdapter : JsonAdapter<DeleteMessages>() {
            @FromJson
            override fun fromJson(reader: JsonReader): DeleteMessages? {
                val s = reader.nextString() ?: return null
                return DeleteMessages.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: DeleteMessages?) {
                writer.value(value?.value)
            }
        }
    }
}
