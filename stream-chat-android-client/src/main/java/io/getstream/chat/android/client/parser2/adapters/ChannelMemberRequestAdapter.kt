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
import io.getstream.chat.android.network.models.ChannelMemberRequest

// The generated ChannelMemberRequest carries custom data in a `custom` field that must be flattened to the
// JSON root on the wire; extraDataPropertyName matches its @Json(name = "custom").
internal object ChannelMemberRequestAdapter :
    CustomObjectDtoAdapter<ChannelMemberRequest>(ChannelMemberRequest::class, extraDataPropertyName = "custom") {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): ChannelMemberRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: ChannelMemberRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<ChannelMemberRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}
