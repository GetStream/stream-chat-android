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

package io.getstream.chat.android.compose.sample.feature.scheduled

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.TimeUnit

internal class ScheduledMessageDatesTest {

    private val timeZone = TimeZone.getTimeZone("Europe/Lisbon")

    @Test
    fun `the presets are tomorrow at 9 and at 13, and the next Monday at 9`() {
        // Wednesday
        val now = date(2026, Calendar.OCTOBER, 28, 15, 30)

        val presets = scheduleMessagePresets(now, timeZone)

        presets shouldBeEqualTo listOf(
            date(2026, Calendar.OCTOBER, 29, 9, 0),
            date(2026, Calendar.OCTOBER, 29, 13, 0),
            date(2026, Calendar.NOVEMBER, 2, 9, 0),
        )
    }

    @Test
    fun `when tomorrow is Monday, the Monday preset is a week later`() {
        // Sunday
        val now = date(2026, Calendar.NOVEMBER, 1, 22, 0)

        val presets = scheduleMessagePresets(now, timeZone)

        presets shouldBeEqualTo listOf(
            date(2026, Calendar.NOVEMBER, 2, 9, 0),
            date(2026, Calendar.NOVEMBER, 2, 13, 0),
            date(2026, Calendar.NOVEMBER, 9, 9, 0),
        )
    }

    @Test
    fun `tomorrow at 9 keeps the local time across a daylight saving change`() {
        // Lisbon leaves summer time on 25 October 2026.
        val now = date(2026, Calendar.OCTOBER, 24, 20, 0)

        scheduleMessagePresets(now, timeZone).first() shouldBeEqualTo date(2026, Calendar.OCTOBER, 25, 9, 0)
    }

    @Test
    fun `the day of a date counts calendar days from today`() {
        val now = date(2026, Calendar.OCTOBER, 28, 23, 50)

        scheduledDay(date(2026, Calendar.OCTOBER, 28, 0, 5), now, timeZone) shouldBeEqualTo ScheduledDay.Today
        scheduledDay(date(2026, Calendar.OCTOBER, 29, 0, 5), now, timeZone) shouldBeEqualTo ScheduledDay.Tomorrow
        scheduledDay(date(2026, Calendar.OCTOBER, 30, 9, 0), now, timeZone) shouldBeEqualTo ScheduledDay.ThisWeek
        scheduledDay(date(2026, Calendar.NOVEMBER, 3, 9, 0), now, timeZone) shouldBeEqualTo ScheduledDay.ThisWeek
        scheduledDay(date(2026, Calendar.NOVEMBER, 4, 9, 0), now, timeZone) shouldBeEqualTo ScheduledDay.ThisYear
        scheduledDay(date(2026, Calendar.OCTOBER, 20, 9, 0), now, timeZone) shouldBeEqualTo ScheduledDay.ThisYear
        scheduledDay(date(2027, Calendar.JANUARY, 2, 9, 0), now, timeZone) shouldBeEqualTo ScheduledDay.OtherYear
    }

    @Test
    fun `the day of a date counts whole days across a daylight saving change`() {
        val now = date(2026, Calendar.OCTOBER, 24, 23, 30)

        scheduledDay(date(2026, Calendar.OCTOBER, 25, 23, 30), now, timeZone) shouldBeEqualTo ScheduledDay.Tomorrow
        scheduledDay(date(2026, Calendar.OCTOBER, 26, 0, 30), now, timeZone) shouldBeEqualTo ScheduledDay.ThisWeek
    }

    @Test
    fun `a date must be at least a minute away and at most 120 days away`() {
        val now = date(2026, Calendar.OCTOBER, 28, 15, 30)

        scheduleDateError(Date(now.time + 59_000), now) shouldBeEqualTo ScheduleDateError.TooSoon
        scheduleDateError(Date(now.time + 60_000), now).shouldBeNull()
        scheduleDateError(Date(now.time + TimeUnit.DAYS.toMillis(120)), now).shouldBeNull()
        scheduleDateError(Date(now.time + TimeUnit.DAYS.toMillis(120) + 1), now) shouldBeEqualTo
            ScheduleDateError.TooFar
    }

    private fun date(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date =
        Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month, day, hour, minute)
        }.time
}
