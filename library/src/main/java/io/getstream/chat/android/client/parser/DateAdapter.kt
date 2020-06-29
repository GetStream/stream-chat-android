package io.getstream.chat.android.client.parser

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

internal class DateAdapter(val gson: Gson) : TypeAdapter<Date>() {

    val dateFormat = SimpleDateFormat(ChatParser.DATE_FORMAT, Locale.getDefault()).apply {
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
            try {
                dateFormat.parse(rawValue)
            } catch (t: Throwable) {
                // there're cases when backend returns invalid date
                // https://getstream.slack.com/archives/CE5N802GP/p1593472741106400
                null
            }
        }

    }
}