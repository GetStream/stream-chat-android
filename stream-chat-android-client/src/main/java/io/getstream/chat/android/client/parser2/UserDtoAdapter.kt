package io.getstream.chat.android.client.parser2

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.client.api2.model.dto.UserDto

internal object UserDtoAdapter : CustomObjectDtoAdapter<UserDto>(UserDto::class) {

    @FromJson
    fun fromJson(
        jsonReader: JsonReader,
        mapAdapter: JsonAdapter<MutableMap<String, Any>>,
        messageAdapter: JsonAdapter<UserDto>,
    ): UserDto = parseWithExtraData(jsonReader, mapAdapter, messageAdapter)

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        message: UserDto?,
        mapAdapter: JsonAdapter<MutableMap<String, Any?>>,
        messageAdapter: JsonAdapter<UserDto>,
    ) = serializeWithExtraData(jsonWriter, message, mapAdapter, messageAdapter)
}
