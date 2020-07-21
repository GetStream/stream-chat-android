package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.api.models.QueryChannelsRequest

class QueryChannelsAdapter(
    val gson: Gson,
    val type: TypeToken<*>
) : TypeAdapter<QueryChannelsRequest>() {

    override fun write(writer: JsonWriter, value: QueryChannelsRequest) {
        gson.toJson(value, type.type, writer)
    }

    override fun read(reader: JsonReader): QueryChannelsRequest {
        return gson.fromJson(reader, type.rawType)
    }
}
