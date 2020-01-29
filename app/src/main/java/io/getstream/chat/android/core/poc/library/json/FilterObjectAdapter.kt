package io.getstream.chat.android.core.poc.library.json

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.core.poc.library.FilterObject


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