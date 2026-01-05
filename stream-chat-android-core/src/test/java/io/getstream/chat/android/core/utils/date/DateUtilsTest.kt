/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.core.utils.date

import io.getstream.chat.android.models.TimeDuration
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.time.Instant
import java.util.Date

internal class DateUtilsTest {

    @ParameterizedTest
    @MethodSource("dateUtilsAfterArguments")
    fun testDateUtilsAfter(
        date1: Date?,
        date2: Date?,
        isAfter: Boolean,
    ) {
        date1 after date2 `should be equal to` isAfter
    }

    @ParameterizedTest
    @MethodSource("dateUtilsMaxArguments")
    fun testDateUtilsMax(
        date1: Date?,
        date2: Date?,
        max: Date?,
    ) {
        max(date1, date2) `should be equal to` max
    }

    @ParameterizedTest
    @MethodSource("dateUtilsMinArguments")
    fun testDateUtilsMin(
        date1: Date?,
        date2: Date?,
        min: Date?,
    ) {
        min(date1, date2) `should be equal to` min
    }

    @ParameterizedTest
    @MethodSource("dateUtilsMaxOfArguments")
    fun testDateUtilsMaxOf(
        dates: Array<Date?>,
        max: Date?,
    ) {
        maxOf(*dates) `should be equal to` max
    }

    @ParameterizedTest
    @MethodSource("dateUtilsMinOfArguments")
    fun testDateUtilsMinOf(
        dates: Array<Date?>,
        min: Date?,
    ) {
        minOf(*dates) `should be equal to` min
    }

    @ParameterizedTest
    @MethodSource("dateUtilsDiffWithOtherTimeArguments")
    fun testDateUtilsDiffWithOtherTime(
        date: Date,
        time: Long,
        diff: TimeDuration,
    ) {
        date.diff(time) `should be equal to` diff
    }

    @ParameterizedTest
    @MethodSource("dateUtilsDiffWithOtherDateArguments")
    fun testDateUtilsDiffWithOtherDate(
        date1: Date,
        date2: Date,
        diff: TimeDuration,
    ) {
        date1.diff(date2) `should be equal to` diff
    }

    @ParameterizedTest
    @MethodSource("dateUtilsIsWithinDurationFromNowArguments")
    fun testDateUtilsIsWithinDurationFromNow(
        date: Date?,
        duration: TimeDuration,
        now: Long,
        isWithinDuration: Boolean,
    ) {
        date.isWithinDurationFromNow(duration) { now } `should be equal to` isWithinDuration
    }

    @ParameterizedTest
    @MethodSource("dateUtilsTruncateFutureArguments")
    fun testDateUtilsTruncateFuture(
        date: Date,
        now: Long,
        truncatedDate: Date,
    ) {
        date.truncateFuture { now } `should be equal to` truncatedDate
    }

    companion object {

        private val dateBefore = Date.from(Instant.ofEpochMilli(1737389237))
        private val date = Date.from(Instant.ofEpochMilli(1737389238))
        private val dateAfter = Date.from(Instant.ofEpochMilli(1737389239))

        @JvmStatic
        fun dateUtilsAfterArguments(): List<Arguments> {
            return listOf(
                Arguments.of(null, null, false),
                Arguments.of(null, dateBefore, false),
                Arguments.of(dateBefore, null, true),
                Arguments.of(dateBefore, dateBefore, false),
                Arguments.of(dateBefore, dateAfter, false),
                Arguments.of(dateAfter, dateBefore, true),
            )
        }

        @JvmStatic
        fun dateUtilsMaxArguments() = listOf(
            Arguments.of(dateBefore, dateBefore, dateBefore),
            Arguments.of(dateBefore, dateAfter, dateAfter),
            Arguments.of(dateAfter, dateBefore, dateAfter),
        )

        @JvmStatic
        fun dateUtilsMinArguments() = listOf(
            Arguments.of(dateBefore, dateBefore, dateBefore),
            Arguments.of(dateBefore, dateAfter, dateBefore),
            Arguments.of(dateAfter, dateBefore, dateBefore),
        )

        @JvmStatic
        fun dateUtilsMaxOfArguments() = listOf(
            Arguments.of(arrayOf(dateAfter, dateAfter), dateAfter),
            Arguments.of(arrayOf(dateBefore, date, dateAfter), dateAfter),
            Arguments.of(arrayOf(dateAfter, date, dateBefore), dateAfter),
            Arguments.of(arrayOf<Date?>(null), null),
        )

        @JvmStatic
        fun dateUtilsMinOfArguments() = listOf(
            Arguments.of(arrayOf(dateBefore, dateBefore), dateBefore),
            Arguments.of(arrayOf(dateBefore, date, dateAfter), dateBefore),
            Arguments.of(arrayOf(dateAfter, date, dateBefore), dateBefore),
            Arguments.of(arrayOf<Date?>(null), null),
        )

        @JvmStatic
        fun dateUtilsDiffWithOtherTimeArguments() = listOf(
            Arguments.of(dateBefore, dateAfter.time, TimeDuration.millis(2)),
            Arguments.of(dateAfter, dateBefore.time, TimeDuration.millis(2)),
        )

        @JvmStatic
        fun dateUtilsDiffWithOtherDateArguments() = listOf(
            Arguments.of(dateBefore, dateAfter, TimeDuration.millis(2)),
            Arguments.of(dateAfter, dateBefore, TimeDuration.millis(2)),
        )

        @JvmStatic
        fun dateUtilsIsWithinDurationFromNowArguments() = listOf(
            Arguments.of(null, TimeDuration.millis(1), date.time, false),
            Arguments.of(dateBefore, TimeDuration.millis(1), date.time, false),
            Arguments.of(dateBefore, TimeDuration.millis(2), date.time, true),
        )

        @JvmStatic
        fun dateUtilsTruncateFutureArguments() = listOf(
            // Note: clone the 'this' date, because it is
            // mutated internally in the `truncateFuture` method
            Arguments.of(dateBefore.clone(), date.time, dateBefore),
            Arguments.of(date.clone(), date.time, date),
            Arguments.of(dateAfter.clone(), date.time, date),
        )
    }
}
