package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto

internal object DownstreamReactionDtoAdapter :
    CustomObjectDtoAdapter<DownstreamReactionDto>(DownstreamReactionDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<DownstreamReactionDto>,
    ): DownstreamReactionDto = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    fun toJson(jsonWriter: JsonWriter, value: DownstreamReactionDto): Unit = error("Can't convert this to Json")
}

internal object UpstreamReactionDtoAdapter :
    CustomObjectDtoAdapter<UpstreamReactionDto>(UpstreamReactionDto::class) {

    @FromJson
    fun fromJson(jsonReader: JsonReader): UpstreamReactionDto = error("Can't parse this from Json")

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: UpstreamReactionDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<UpstreamReactionDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}
