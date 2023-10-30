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
        private val now: Date,
        private val is24Hour: Boolean = false,
        private val dateTimePattern: String = "yyyy-MM-dd",
    ) : DefaultDateFormatter.DateContext {
        override fun now(): Date = now
        override fun yesterdayString() = YESTERDAY_STRING
        override fun is24Hour(): Boolean = is24Hour
        override fun dateTimePattern(): String = dateTimePattern
    }

    @Test
    fun `Date formatting is correct`(@TestParameter testCase: TestCase) {
        val formatter: DateFormatter = DefaultDateFormatter(
            TestDateContext(testCase.now.toDate(), testCase.is24Hour, testCase.dateTimePattern),
            Locale.US,
        )

        val formattedDate = formatter.formatDate(testCase.dateToFormat?.toDate())

        formattedDate shouldBeEqualTo testCase.expectedResult
    }

    @Suppress("unused")
    enum class TestCase(
        val dateToFormat: LocalDateTime?,
        val expectedResult: String,
        val now: LocalDate,
        val is24Hour: Boolean = true,
        val dateTimePattern: String = "",
    ) {
        NULL(
            dateToFormat = null,
            now = LocalDate.of(2020, 12, 7),
            expectedResult = "",
        ),
        TODAY_12H(
            dateToFormat = LocalDateTime.of(2020, 12, 7, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            is24Hour = false,
            expectedResult = "9:25 AM",
        ),
        TODAY_24H(
            dateToFormat = LocalDateTime.of(2020, 12, 7, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            is24Hour = true,
            expectedResult = "09:25",
        ),
        YESTERDAY(
            dateToFormat = LocalDateTime.of(2020, 12, 6, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            expectedResult = YESTERDAY_STRING,
        ),
        DAY_BEFORE_YESTERDAY(
            dateToFormat = LocalDateTime.of(2020, 12, 5, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            expectedResult = "Saturday",
        ),
        SIX_DAYS_AGO(
            dateToFormat = LocalDateTime.of(2020, 12, 1, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            expectedResult = "Tuesday",
        ),
        SEVEN_DAYS_AGO(
            dateToFormat = LocalDateTime.of(2020, 11, 30, 9, 25),
            now = LocalDate.of(2020, 12, 7),
            dateTimePattern = "dd/MM/yy",
            expectedResult = "30/11/20",
        ),
    }

    companion object {
        const val YESTERDAY_STRING = "YESTERDAY_STRING"

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
