package io.getstream.chat.android.core.poc.library.json.adapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.core.poc.library.requests.QuerySort
import java.io.IOException
import java.util.*

class QuerySortAdapter : TypeAdapter<QuerySort>() {
    override fun write(out: JsonWriter?, value: QuerySort?) {
        val adapter = Gson().getAdapter(ArrayList::class.java)
        adapter.write(out, value?.data as? ArrayList<*>)
    }

    override fun read(`in`: JsonReader?): QuerySort? {
        IOException("not supported")
        return null
    }
}