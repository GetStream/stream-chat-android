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

package io.getstream.chat.android.client.api2

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.model.requests.FlagMessageRequest
import io.getstream.chat.android.client.api2.model.requests.FlagRequest
import io.getstream.chat.android.client.api2.model.requests.FlagUserRequest
import java.lang.reflect.Type

/**
 * A [JsonAdapter.Factory] which provide [JsonAdapter] to serialize/deserialize [FlagRequest] entities.
 */
internal object FlagRequestAdapterFactory : JsonAdapter.Factory {
    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? =
        when (type) {
            FlagRequest::class.java -> FlagRequestAdapter(moshi)
            else -> null
        }

    /**
     * A [JsonAdapter] to serialize/deserialize [FlagRequest] entities.
     */
    private class FlagRequestAdapter(private val moshi: Moshi) : JsonAdapter<FlagRequest>() {
        override fun fromJson(reader: JsonReader): FlagRequest? {
            reader.readJsonValue()
            return null
        }

        override fun toJson(writer: JsonWriter, value: FlagRequest?) {
            when (value) {
                is FlagMessageRequest -> moshi.adapter(FlagMessageRequest::class.java).toJson(writer, value)
                is FlagUserRequest -> moshi.adapter(FlagUserRequest::class.java).toJson(writer, value)
                else -> {}
            }
        }
    }
}
