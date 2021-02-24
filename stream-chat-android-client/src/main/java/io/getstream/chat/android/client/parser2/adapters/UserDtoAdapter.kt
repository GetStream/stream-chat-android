package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.client.api2.model.dto.DownstreamUserDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamUserDto

internal object DownstreamUserDtoAdapter :
    CustomObjectDtoAdapter<DownstreamUserDto>(DownstreamUserDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<DownstreamUserDto>,
    ): DownstreamUserDto = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: DownstreamUserDto): Unit = error("Can't convert this to Json")
}

internal object UpstreamUserDtoAdapter :
    CustomObjectDtoAdapter<UpstreamUserDto>(UpstreamUserDto::class) {

    @FromJson
    fun fromJson(jsonReader: JsonReader): UpstreamUserDto = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: UpstreamUserDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<UpstreamUserDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}
