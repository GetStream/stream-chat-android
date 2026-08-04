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
import io.getstream.chat.android.network.models.CreatePollOptionRequest
import io.getstream.chat.android.network.models.CreatePollRequest
import io.getstream.chat.android.network.models.PollOptionInput
import io.getstream.chat.android.network.models.PollOptionRequest
import io.getstream.chat.android.network.models.UpdatePollOptionRequest
import io.getstream.chat.android.network.models.UpdatePollRequest

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

// The generated poll write-request models carry custom data in a `custom` field that must be
// flattened to the JSON root on the wire; extraDataPropertyName matches their @Json(name = "custom").

internal object CreatePollRequestAdapter :
    CustomObjectDtoAdapter<CreatePollRequest>(CreatePollRequest::class, extraDataPropertyName = "custom") {

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

internal object UpdatePollRequestAdapter :
    CustomObjectDtoAdapter<UpdatePollRequest>(UpdatePollRequest::class, extraDataPropertyName = "custom") {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): UpdatePollRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: UpdatePollRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<UpdatePollRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}

internal object CreatePollOptionRequestAdapter :
    CustomObjectDtoAdapter<CreatePollOptionRequest>(CreatePollOptionRequest::class, extraDataPropertyName = "custom") {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): CreatePollOptionRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: CreatePollOptionRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<CreatePollOptionRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}

internal object UpdatePollOptionRequestAdapter :
    CustomObjectDtoAdapter<UpdatePollOptionRequest>(UpdatePollOptionRequest::class, extraDataPropertyName = "custom") {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): UpdatePollOptionRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: UpdatePollOptionRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<UpdatePollOptionRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}

internal object PollOptionInputAdapter :
    CustomObjectDtoAdapter<PollOptionInput>(PollOptionInput::class, extraDataPropertyName = "custom") {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): PollOptionInput = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: PollOptionInput?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<PollOptionInput>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}

internal object PollOptionRequestAdapter :
    CustomObjectDtoAdapter<PollOptionRequest>(PollOptionRequest::class, extraDataPropertyName = "custom") {

    @FromJson
    @Suppress("UNUSED_PARAMETER")
    fun fromJson(jsonReader: JsonReader): PollOptionRequest = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        request: PollOptionRequest?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        requestAdapter: JsonAdapter<PollOptionRequest>,
    ) = serializeWithExtraData(jsonWriter, request, mapAdapter, requestAdapter)
}
