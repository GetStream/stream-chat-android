package io.getstream.chat.android.client.parser2

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.client.api2.model.dto.ReactionDto

internal object ReactionDtoAdapter : CustomObjectDtoAdapter<ReactionDto>(ReactionDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<ReactionDto>,
    ): ReactionDto = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: ReactionDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<ReactionDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}
