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
import io.getstream.chat.android.network.models.GetOGResponse

/**
 * Collects the root-level custom fields of an OG scrape into `custom`, which is how the API sends them.
 *
 * The endpoint also ships `file_size`, `image`, `mime_type` and `name` at the root even though the spec
 * does not declare them, so they arrive here too; `GetOGResponse.toDomain()` reads them back out.
 */
internal object GetOGResponseAdapter :
    CustomObjectDtoAdapter<GetOGResponse>(GetOGResponse::class, extraDataPropertyName = "custom") {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        valueAdapter: JsonAdapter<GetOGResponse>,
    ): GetOGResponse? = parseWithExtraData(jsonReader, mapAdapter, valueAdapter)

    @ToJson
    @Suppress("UNUSED_PARAMETER")
    fun toJson(jsonWriter: JsonWriter, value: GetOGResponse): Unit = error("Can't convert this to Json")
}
