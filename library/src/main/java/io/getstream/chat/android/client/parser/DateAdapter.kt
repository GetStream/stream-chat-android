package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

internal class DateAdapter(val gson: Gson) : TypeAdapter<Date>() {

    val dateFormat = SimpleDateFormat(ChatParser.DATE_FORMAT).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    @Synchronized
    override fun write(out: JsonWriter, value: Date?) {

        if (value == null) {
            out.nullValue()
        } else {
            val rawValue = dateFormat.format(value)
            out.value(rawValue)
        }
    }

    @Synchronized
    override fun read(reader: JsonReader): Date? {
        val rawValue = reader.nextString()
        return if (rawValue.isNullOrEmpty()) {
            null
        } else {
            dateFormat.parse(rawValue)
        }

    }
}