/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.parser2.adapters

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson
import io.getstream.chat.android.client.utils.threadLocal
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@InternalStreamChatApi
public class DateAdapter : JsonAdapter<Date>() {

    private companion object {
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        const val DATE_FORMAT_WITHOUT_NANOSECONDS = "yyyy-MM-dd'T'HH:mm:ss'Z'"
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

    @ToJson
    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val rawValue = dateFormat.format(value)
            writer.value(rawValue)
        }
    }

    @Suppress("TooGenericExceptionCaught")
    @FromJson
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
            } catch (_: Throwable) {
                try {
                    dateFormatWithoutNanoseconds.parse(rawValue)
                } catch (_: Throwable) {
                    null
                }
            }
        }
    }
}
