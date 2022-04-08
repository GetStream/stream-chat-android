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

import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

@RunWith(TestParameterInjector::class)
internal class DefaultDateFormatterTest {

    companion object {
        const val YESTERDAY_STRING = "YESTERDAY_STRING"
    }

    class TestDateContext(
        private val now: LocalDate,
        private val is24Hour: Boolean = false,
        private val dateTimePattern: String = "yyyy-MM-dd",
    ) : DefaultDateFormatter.DateContext {
        override fun now(): LocalDate = now
        override fun yesterdayString() = YESTERDAY_STRING
        override fun is24Hour(): Boolean = is24Hour
        override fun dateTimePattern(): String = dateTimePattern
    }

    @Test
    fun `Date formatting is correct`(@TestParameter testCase: TestCase) {
        val formatter: DateFormatter = DefaultDateFormatter(
            TestDateContext(testCase.now, testCase.is24Hour, testCase.dateTimePattern)
        )

        val formattedDate = formatter.formatDate(testCase.dateToFormat)

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
}
