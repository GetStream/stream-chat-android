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
 * [Push provider.]
 */
/**
 * PushProviderName Enum
 */
internal sealed class PushProviderName(internal val value: String) {
    override fun toString(): String = value

    internal companion object {
        internal fun fromString(s: String): PushProviderName = when (s) {
            "apn" -> Apn
            "firebase" -> Firebase
            "huawei" -> Huawei
            "xiaomi" -> Xiaomi
            else -> Unknown(s)
        }
    }
    internal object Apn : PushProviderName("apn")
    internal object Firebase : PushProviderName("firebase")
    internal object Huawei : PushProviderName("huawei")
    internal object Xiaomi : PushProviderName("xiaomi")
    internal data class Unknown(val unknownValue: String) : PushProviderName(unknownValue)

    internal class PushProviderNameAdapter : JsonAdapter<PushProviderName>() {
        @FromJson
        override fun fromJson(reader: JsonReader): PushProviderName? {
            val s = reader.nextString() ?: return null
            return PushProviderName.fromString(s)
        }

        @ToJson
        override fun toJson(writer: JsonWriter, value: PushProviderName?) {
            writer.value(value?.value)
        }
    }
}
