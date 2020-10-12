package io.getstream.chat.android.client.parser.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.api.models.QuerySort
import java.io.IOException
import java.util.ArrayList

internal class QuerySortAdapter(val gson: Gson) : TypeAdapter<QuerySort>() {
    override fun write(out: JsonWriter, value: QuerySort?) {
        val adapter = gson.getAdapter(ArrayList::class.java)
        adapter.write(out, value?.data as? ArrayList<*>)
    }

    override fun read(`in`: JsonReader?): QuerySort? {
        throw IOException("QuerySort must not be deserialized")
    }
}
