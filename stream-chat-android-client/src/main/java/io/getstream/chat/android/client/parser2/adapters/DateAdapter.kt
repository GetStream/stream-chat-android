package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import io.getstream.chat.android.client.utils.threadLocal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal class DateAdapter : JsonAdapter<Date>() {

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        private const val DATE_FORMAT_WITHOUT_NANOSECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'"
    }

    private val dateFormat: SimpleDateFormat by threadLocal {
        SimpleDateFormat(DATE_FORMAT, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    private val dateFormatWithoutNanoseconds: SimpleDateFormat by threadLocal {
        SimpleDateFormat(DATE_FORMAT_WITHOUT_NANOSECONDS, Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }

    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val rawValue = dateFormat.format(value)
            writer.value(rawValue)
        }
    }

    override fun fromJson(reader: JsonReader): Date? {
        val nextValue = reader.peek()
        if (nextValue == JsonReader.Token.NULL) {
            reader.skipValue()
            return null
        }

        val rawValue = reader.nextString()
        return if (rawValue.isEmpty()) {
            null
        } else {
            try {
                dateFormat.parse(rawValue)
            } catch (t: Throwable) {
                try {
                    dateFormatWithoutNanoseconds.parse(rawValue)
                } catch (t: Throwable) {
                    null
                }
            }
        }
    }
}
