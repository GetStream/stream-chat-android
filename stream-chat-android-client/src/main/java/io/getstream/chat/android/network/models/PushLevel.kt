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
 * [How much a channel notifies its members.]
 */
/**
 * PushLevel Enum
 */
internal sealed class PushLevel(internal val value: String) {
    override fun toString(): String = value

    internal companion object {
        internal fun fromString(s: String): PushLevel = when (s) {
            "all" -> All
            "all_mentions" -> AllMentions
            "direct_mentions" -> DirectMentions
            "mentions" -> Mentions
            "none" -> None
            else -> Unknown(s)
        }
    }
    internal object All : PushLevel("all")
    internal object AllMentions : PushLevel("all_mentions")
    internal object DirectMentions : PushLevel("direct_mentions")
    internal object Mentions : PushLevel("mentions")
    internal object None : PushLevel("none")
    internal data class Unknown(val unknownValue: String) : PushLevel(unknownValue)

    internal class PushLevelAdapter : JsonAdapter<PushLevel>() {
        @FromJson
        override fun fromJson(reader: JsonReader): PushLevel? {
            val s = reader.nextString() ?: return null
            return PushLevel.fromString(s)
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: PushLevel?) {
            writer.value(value?.value)
        }
    }
}
