package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.utils.FilterObject


class FilterObjectAdapter(val gson: Gson) : TypeAdapter<FilterObject>() {

    override fun write(out: JsonWriter, value: FilterObject) {
        val adapter = gson.getAdapter(HashMap::class.java)
        adapter.write(out, value.getData())
    }

    override fun read(reader: JsonReader): FilterObject {
        val adapter = gson.getAdapter(HashMap::class.java)
        val data = adapter.read(reader) as HashMap<String, Any>
        return FilterObject(data)
    }
}