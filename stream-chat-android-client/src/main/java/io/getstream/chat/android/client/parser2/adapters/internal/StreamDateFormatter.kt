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

package io.getstream.chat.android.client.parser2.adapters.internal

import io.getstream.chat.android.client.utils.threadLocal
import io.getstream.chat.android.core.internal.InternalStreamChatApi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * This class handles parse and format date in the standard way of Stream.
 */
@InternalStreamChatApi
public class StreamDateFormatter {

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

    internal val datePattern = DATE_FORMAT

    /**
     * Parses the [String] to [Date] in the standard way to Stream's API
     */
    internal fun parse(rawValue: String): Date? {
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

    /**
     * Formats the [Date] in the standard way to Stream's API
     */
    public fun format(date: Date): String = dateFormat.format(date)
}
