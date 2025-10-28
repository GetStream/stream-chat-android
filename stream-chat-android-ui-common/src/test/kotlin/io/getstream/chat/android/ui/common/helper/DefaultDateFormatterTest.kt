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

package io.getstream.chat.android.ui.common.helper

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import io.getstream.chat.android.core.utils.date.isWithinDurationFromNow
import io.getstream.chat.android.models.TimeDuration
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomString
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.DateTimeUtils
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.Date
import java.util.Locale

@RunWith(TestParameterInjector::class)
internal class DefaultDateFormatterTest {

    class TestDateContext(
        private val now: Date = LocalDate.of(2020, 12, 7).toDate(),
        private val is24Hour: Boolean = false,
        private val dateTimePattern: String = "yyyy-MM-dd",
        private val relativeTime: String = RELATIVE_TIME_STRING,
        private val relativeDate: String = RELATIVE_DATE_STRING,
    ) : DefaultDateFormatter.DateContext {
        private val oneMinuteDuration = TimeDuration.minutes(1)

        override fun now(): Date = now
        override fun isWithinLastMinute(date: Date?): Boolean = date.isWithinDurationFromNow(oneMinuteDuration) {
            now.time
        }

        override fun yesterdayString() = YESTERDAY_STRING
        override fun justNowString(): String = JUST_NOW_STRING
        override fun is24Hour(): Boolean = is24Hour
        override fun dateTimePattern(): String = dateTimePattern
        override fun relativeTime(date: Date): String = relativeTime
        override fun relativeDate(date: Date): String = relativeDate
    }

    @Test
    fun `Date formatting is correct`(@TestParameter testCase: TestCase) {
        val formatter: DateFormatter = DefaultDateFormatter(
            TestDateContext(testCase.now.toDate(), testCase.is24Hour, testCase.dateTimePattern),
            Locale.US,
        )

        val formattedDate = formatter.formatDate(testCase.dateToFormat?.toDate())

        formattedDate shouldBeEqualTo testCase.expectedFormatDateResult
    }

    @Test
    fun `Time formatting is correct`(@TestParameter testCase: TestCase) {
        val formatter: DateFormatter = DefaultDateFormatter(
            TestDateContext(testCase.now.toDate(), testCase.is24Hour, testCase.dateTimePattern),
            Locale.US,
        )

        val formattedDate = formatter.formatTime(testCase.dateToFormat?.toDate())

        formattedDate shouldBeEqualTo testCase.expectedFormatTimeResult
    }

    @Test
    fun `Relative Time formatting is correct`(@TestParameter testCase: TestCase) {
        val formatter: DateFormatter = DefaultDateFormatter(
            TestDateContext(testCase.now.toDate(), testCase.is24Hour, testCase.dateTimePattern),
            Locale.US,
        )

        val formattedDate = formatter.formatRelativeTime(testCase.dateToFormat?.toDate())

        formattedDate shouldBeEqualTo testCase.expectedFormatRelativeTimeResult
    }

    @Test
    fun `Relative date formatting is correct`() {
        val formatter: DateFormatter = DefaultDateFormatter(
            TestDateContext(),
            Locale.US,
        )

        formatter.formatRelativeDate(randomDate()) `should be equal to` RELATIVE_DATE_STRING
    }

    @Suppress("unused")
    enum class TestCase(
        val dateToFormat: LocalDateTime?,
        val expectedFormatDateResult: String,
        val expectedFormatTimeResult: String,
        val expectedFormatRelativeTimeResult: String,
        val now: LocalDate,
        val is24Hour: Boolean = true,
        val dateTimePattern: String = "",
    ) {
        NULL(
            dateToFormat = null,
            now = LocalDate.of(2020, 12, 7),
            expectedFormatDateResult = "",
            expectedFormatTimeResult = "",
            expectedFormatRelativeTimeResult = "",
        ),
        TODAY_12H(
            dateToFormat = LocalDateTime.of(2020, 12, 7, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            is24Hour = false,
            expectedFormatDateResult = "9:25 AM",
            expectedFormatTimeResult = "9:25 AM",
            expectedFormatRelativeTimeResult = JUST_NOW_STRING,
        ),
        TODAY_24H(
            dateToFormat = LocalDateTime.of(2020, 12, 7, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            is24Hour = true,
            expectedFormatDateResult = "09:25",
            expectedFormatTimeResult = "09:25",
            expectedFormatRelativeTimeResult = JUST_NOW_STRING,
        ),
        YESTERDAY(
            dateToFormat = LocalDateTime.of(2020, 12, 6, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            expectedFormatDateResult = YESTERDAY_STRING,
            expectedFormatTimeResult = "09:25",
            expectedFormatRelativeTimeResult = RELATIVE_TIME_STRING,
        ),
        DAY_BEFORE_YESTERDAY(
            dateToFormat = LocalDateTime.of(2020, 12, 5, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            expectedFormatDateResult = "Saturday",
            expectedFormatTimeResult = "09:25",
            expectedFormatRelativeTimeResult = RELATIVE_TIME_STRING,
        ),
        SIX_DAYS_AGO(
            dateToFormat = LocalDateTime.of(2020, 12, 1, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            expectedFormatDateResult = "Tuesday",
            expectedFormatTimeResult = "09:25",
            expectedFormatRelativeTimeResult = RELATIVE_TIME_STRING,
        ),
        SEVEN_DAYS_AGO(
            dateToFormat = LocalDateTime.of(2020, 11, 30, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            dateTimePattern = "dd/MM/yy",
            expectedFormatDateResult = "30/11/20",
            expectedFormatTimeResult = "09:25",
            expectedFormatRelativeTimeResult = RELATIVE_TIME_STRING,
        ),
    }

    companion object {
        const val YESTERDAY_STRING = "YESTERDAY_STRING"
        const val JUST_NOW_STRING = "JUST_NOW_STRING"
        val RELATIVE_DATE_STRING: String = randomString()
        val RELATIVE_TIME_STRING: String = randomString()

        private fun LocalDateTime.toDate(): Date {
            val instant = atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            return Date(instant)
        }

        private fun LocalDate.toDate(): Date {
            val instant: Instant = atTime(LocalTime.of(0, 0))
                .atZone(ZoneId.systemDefault())
                .toInstant()
            return DateTimeUtils.toDate(instant)
        }
    }
}
