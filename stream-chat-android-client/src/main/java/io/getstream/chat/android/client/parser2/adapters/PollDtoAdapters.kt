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
import io.getstream.chat.android.client.api2.model.requests.UpstreamOptionDto
import io.getstream.chat.android.network.models.CreatePollRequest as GeneratedCreatePollRequest
import io.getstream.chat.android.network.models.PollOptionInput as GeneratedPollOptionInput
import io.getstream.chat.android.network.models.PollOptionRequest as GeneratedPollOptionRequest
import io.getstream.chat.android.network.models.PollOptionResponseData as DownstreamPollOptionDto
import io.getstream.chat.android.network.models.UpdatePollRequest as GeneratedUpdatePollRequest

/**
 * Deserializer for [DownstreamPollDto] that handles the [io.getstream.chat.android.client.api2.model.dto.ExtraDataDto]
 * implementation.
 */
internal object DownstreamPollDtoAdapter :
    CustomObjectDtoAdapter<DownstreamPollDto>(
        kClass = DownstreamPollDto::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        pollAdapter: JsonAdapter<DownstreamPollDto>,
    ): DownstreamPollDto? = parseWithExtraData(jsonReader, mapAdapter, pollAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        value: DownstreamPollDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        pollAdapter: JsonAdapter<DownstreamPollDto>,
    ) = serializeWithExtraData(jsonWriter, value, mapAdapter, pollAdapter)
}

/**
 * Deserializer for [DownstreamPollOptionDto] that handles the
 * [io.getstream.chat.android.client.api2.model.dto.ExtraDataDto] implementation.
 */
internal object DownstreamPollOptionDtoAdapter :
    CustomObjectDtoAdapter<DownstreamPollOptionDto>(
        kClass = DownstreamPollOptionDto::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        optionAdapter: JsonAdapter<DownstreamPollOptionDto>,
    ): DownstreamPollOptionDto? = parseWithExtraData(jsonReader, mapAdapter, optionAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        value: DownstreamPollOptionDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        optionAdapter: JsonAdapter<DownstreamPollOptionDto>,
    ) = serializeWithExtraData(jsonWriter, value, mapAdapter, optionAdapter)
}

internal object GeneratedCreatePollRequestAdapter :
    CustomObjectDtoAdapter<GeneratedCreatePollRequest>(
        kClass = GeneratedCreatePollRequest::class,
        extraDataPropertyName = "Custom",
    ) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): GeneratedCreatePollRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: GeneratedCreatePollRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<GeneratedCreatePollRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}

internal object GeneratedUpdatePollRequestAdapter :
    CustomObjectDtoAdapter<GeneratedUpdatePollRequest>(
        kClass = GeneratedUpdatePollRequest::class,
        extraDataPropertyName = "Custom",
    ) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): GeneratedUpdatePollRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: GeneratedUpdatePollRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<GeneratedUpdatePollRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}

internal object GeneratedPollOptionRequestAdapter :
    CustomObjectDtoAdapter<GeneratedPollOptionRequest>(
        kClass = GeneratedPollOptionRequest::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): GeneratedPollOptionRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        option: GeneratedPollOptionRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        optionAdapter: JsonAdapter<GeneratedPollOptionRequest>,
    ) = serializeWithExtraData(jsonWriter, option, mapAdapter, optionAdapter)
}

internal object GeneratedPollOptionInputAdapter :
    CustomObjectDtoAdapter<GeneratedPollOptionInput>(
        kClass = GeneratedPollOptionInput::class,
        extraDataPropertyName = "custom",
    ) {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): GeneratedPollOptionInput = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        option: GeneratedPollOptionInput?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        optionAdapter: JsonAdapter<GeneratedPollOptionInput>,
    ) = serializeWithExtraData(jsonWriter, option, mapAdapter, optionAdapter)
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
