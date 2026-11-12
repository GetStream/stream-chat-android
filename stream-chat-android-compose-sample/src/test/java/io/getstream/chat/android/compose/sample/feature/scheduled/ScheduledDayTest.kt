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
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

internal class ScheduledDayTest {

    private val timeZone = TimeZone.getTimeZone("Europe/Lisbon")

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

    private fun date(year: Int, month: Int, day: Int, hour: Int, minute: Int): Date =
        Calendar.getInstance(timeZone).apply {
            clear()
            set(year, month, day, hour, minute)
        }.time
}
