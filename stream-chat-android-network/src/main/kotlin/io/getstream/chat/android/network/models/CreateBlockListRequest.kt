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
import kotlin.collections.List

/**
 * Block list contains restricted words
 */

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class CreateBlockListRequest(
    @Json(name = "name")
    val name: kotlin.String,

    @Json(name = "words")
    val words: kotlin.collections.List<kotlin.String> = emptyList(),

    @Json(name = "is_leet_check_enabled")
    val isLeetCheckEnabled: kotlin.Boolean? = null,

    @Json(name = "is_plural_check_enabled")
    val isPluralCheckEnabled: kotlin.Boolean? = null,

    @Json(name = "team")
    val team: kotlin.String? = null,

    @Json(name = "type")
    val type: Type? = null,
) {

    /**
     * Type Enum
     */
    sealed class Type(val value: kotlin.String) {
        override fun toString(): String = value

        companion object {
            fun fromString(s: kotlin.String): Type = when (s) {
                "domain" -> Domain
                "domain_allowlist" -> DomainAllowlist
                "email" -> Email
                "email_allowlist" -> EmailAllowlist
                "regex" -> Regex
                "word" -> Word
                else -> Unknown(s)
            }
        }
        object Domain : Type("domain")
        object DomainAllowlist : Type("domain_allowlist")
        object Email : Type("email")
        object EmailAllowlist : Type("email_allowlist")
        object Regex : Type("regex")
        object Word : Type("word")
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
