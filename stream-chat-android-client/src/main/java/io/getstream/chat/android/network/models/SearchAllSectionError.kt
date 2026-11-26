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
internal data class SearchAllSectionError(
    @Json(name = "code")
    internal val code: Code,

    @Json(name = "message")
    internal val message: String,
) {

    /**
     * Code Enum
     */
    internal sealed class Code(internal val value: String) {
        override fun toString(): String = value

        internal companion object {
            internal fun fromString(s: String): Code = when (s) {
                "backend_timeout" -> BackendTimeout
                "backend_unavailable" -> BackendUnavailable
                "internal_error" -> InternalError
                "unsupported" -> Unsupported
                else -> Unknown(s)
            }
        }
        internal object BackendTimeout : Code("backend_timeout")
        internal object BackendUnavailable : Code("backend_unavailable")
        internal object InternalError : Code("internal_error")
        internal object Unsupported : Code("unsupported")
        internal data class Unknown(val unknownValue: String) : Code(unknownValue)

        internal class CodeAdapter : JsonAdapter<Code>() {
            @FromJson
            override fun fromJson(reader: JsonReader): Code? {
                val s = reader.nextString() ?: return null
                return Code.fromString(s)
            }

            @ToJson
            override fun toJson(writer: JsonWriter, value: Code?) {
                writer.value(value?.value)
            }
        }
    }
}
