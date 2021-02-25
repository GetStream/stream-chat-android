package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.client.api2.model.dto.DownstreamMessageDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamMessageDto

internal object DownstreamMessageDtoAdapter :
    CustomObjectDtoAdapter<DownstreamMessageDto>(DownstreamMessageDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<DownstreamMessageDto>,
    ): DownstreamMessageDto = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: DownstreamMessageDto): Unit = error("Can't convert this to Json")
}

internal object UpstreamMessageDtoAdapter :
    CustomObjectDtoAdapter<UpstreamMessageDto>(UpstreamMessageDto::class) {

    @FromJson
    fun fromJson(jsonReader: JsonReader): UpstreamMessageDto = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: UpstreamMessageDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<UpstreamMessageDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}
