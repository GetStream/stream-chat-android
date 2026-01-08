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
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollDto
import io.getstream.chat.android.client.api2.model.dto.DownstreamPollOptionDto
import io.getstream.chat.android.client.api2.model.requests.CreatePollRequest
import io.getstream.chat.android.client.api2.model.requests.UpstreamOptionDto

/**
 * Deserializer for [DownstreamPollDto] that handles the [io.getstream.chat.android.client.api2.model.dto.ExtraDataDto]
 * implementation.
 */
internal object DownstreamPollDtoAdapter : CustomObjectDtoAdapter<DownstreamPollDto>(DownstreamPollDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        pollAdapter: JsonAdapter<DownstreamPollDto>,
    ): DownstreamPollDto? = parseWithExtraData(jsonReader, mapAdapter, pollAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: DownstreamPollDto): Unit = error("Can't convert this to Json")
}

/**
 * Deserializer for [DownstreamPollOptionDto] that handles the
 * [io.getstream.chat.android.client.api2.model.dto.ExtraDataDto] implementation.
 */
internal object DownstreamPollOptionDtoAdapter :
    CustomObjectDtoAdapter<DownstreamPollOptionDto>(DownstreamPollOptionDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        optionAdapter: JsonAdapter<DownstreamPollOptionDto>,
    ): DownstreamPollOptionDto? = parseWithExtraData(jsonReader, mapAdapter, optionAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: DownstreamPollOptionDto): Unit = error("Can't convert this to Json")
}

/**
 * Serializer for [CreatePollRequest] that handles the [io.getstream.chat.android.client.api2.model.dto.ExtraDataDto]
 * implementation.
 */
internal object CreatePollRequestAdapter : CustomObjectDtoAdapter<CreatePollRequest>(CreatePollRequest::class) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): CreatePollRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: CreatePollRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<CreatePollRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}

/**
 * Serializer for [UpstreamOptionDto] that handles the [io.getstream.chat.android.client.api2.model.dto.ExtraDataDto]
 * implementation.
 */
internal object UpstreamOptionDtoAdapter :
    CustomObjectDtoAdapter<UpstreamOptionDto>(UpstreamOptionDto::class) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): UpstreamOptionDto = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        option: UpstreamOptionDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        optionAdapter: JsonAdapter<UpstreamOptionDto>,
    ) = serializeWithExtraData(jsonWriter, option, mapAdapter, optionAdapter)
}
