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

package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.network.models.UserResponse

// Downstream (read-only) adapter for the generated UserResponse: collects root-level custom fields
// into `custom`, matching the wire's flattened extra data. extraDataPropertyName is its @Json name.
internal object UserResponseAdapter :
    CustomObjectDtoAdapter<UserResponse>(UserResponse::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<UserResponse>,
    ): UserResponse? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    // Upstream models embed a nullable UserResponse, so a null has to serialize as an omitted field
    // instead of tripping the non-null check. An actual value is still not serializable.
    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: UserResponse?) {
        if (value != null) error("Can't convert this to Json")
        jsonWriter.nullValue()
    }
}
