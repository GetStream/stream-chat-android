package io.getstream.chat.android.client.parser

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.client.utils.threadLocal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
private const val DATE_FORMAT_WITHOUTH_NANOSECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'"

internal class DateAdapter : TypeAdapter<Date>() {

    private val dateFormat: SimpleDateFormat by threadLocal {
        SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val dateFormatWithoutNanoseconds: SimpleDateFormat by threadLocal {
        SimpleDateFormat(DATE_FORMAT_WITHOUTH_NANOSECONDS, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
        } else {
            val rawValue = dateFormat.format(value)
            out.value(rawValue)
        }
    }

    override fun read(reader: JsonReader): Date? =
        try {
            val rawValue = reader.nextString()
            if (rawValue.isNullOrEmpty()) {
                null
            } else {
                try {
                    dateFormat.parse(rawValue)
                } catch (t: Throwable) {
                    try {
                        dateFormatWithoutNanoseconds.parse(rawValue)
                    } catch (t: Throwable) {
                        // there're cases when backend returns invalid date
                        // https://getstream.slack.com/archives/CE5N802GP/p1593472741106400
                        null
                    }
                }
            }
        } catch (t: Throwable) {
            null
        }
}
