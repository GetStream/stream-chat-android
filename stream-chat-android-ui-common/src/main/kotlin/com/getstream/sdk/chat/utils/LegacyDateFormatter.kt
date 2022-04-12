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

package com.getstream.sdk.chat.utils

import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.util.Date

public abstract class LegacyDateFormatter : DateFormatter {
    /**
     * Formats the given date as a String.
     */
    public abstract fun formatDate(date: Date?): String

    /**
     * Formats the given time as a String.
     *
     * Note that only the hour, minute, and hour values contained by the parameter
     * [Date] object are valid.
     */
    public abstract fun formatTime(time: Date?): String

    final override fun formatDate(localDateTime: LocalDateTime?): String =
        formatDate(localDateTime?.let(DateConverter::toDate))
    final override fun formatTime(localTime: LocalTime?): String =
        formatTime(localTime?.let(::toTime))

    private fun toTime(localTime: LocalTime): Date {
        @Suppress("DEPRECATION")
        return Date(0L).apply {
            hours = localTime.hour
            minutes = localTime.minute
            seconds = localTime.second
        }
    }
}
