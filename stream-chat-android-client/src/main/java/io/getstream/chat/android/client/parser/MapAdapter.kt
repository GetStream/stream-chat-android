package io.getstream.chat.android.client.parser

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal class MapAdapter(private val delegateMapAdapter: TypeAdapter<Map<*, *>>) : TypeAdapter<Map<*, *>>() {
    override fun read(reader: JsonReader?): Map<*, *> = delegateMapAdapter.read(reader)
    override fun write(writer: JsonWriter?, value: Map<*, *>?) =
        delegateMapAdapter.write(writer, value?.filterValues { it != null }?.takeIf { it.isNotEmpty() })
}
