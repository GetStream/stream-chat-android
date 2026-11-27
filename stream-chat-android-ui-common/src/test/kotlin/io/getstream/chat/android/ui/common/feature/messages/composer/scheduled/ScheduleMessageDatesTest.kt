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

package io.getstream.chat.android.ui.common.feature.messages.composer.scheduled

import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.randomScheduledMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal class ScheduleMessageDatesTest {

    private val timeZone = TimeZone.getTimeZone("Europe/Amsterdam")

    @Test
    fun `the quick dates are tomorrow at 9 and 13, and the next Monday at 9`() {
        // A Wednesday.
        val now = date("2026-11-11 15:30")

        val dates = scheduleMessageQuickDates(now, timeZone)

        assertEquals(
            listOf("2026-11-12 09:00", "2026-11-12 13:00", "2026-11-16 09:00"),
            dates.map(::format),
        )
    }

    @Test
    fun `on a Sunday, Monday is tomorrow, so the third date is the Monday after`() {
        val now = date("2026-11-15 08:00")

        val dates = scheduleMessageQuickDates(now, timeZone)

        assertEquals(
            listOf("2026-11-16 09:00", "2026-11-16 13:00", "2026-11-23 09:00"),
            dates.map(::format),
        )
    }

    @Test
    fun `the quick dates keep the local hour across a daylight saving change`() {
        // The clocks go back on the night of 2026-10-25 in Amsterdam.
        val now = date("2026-10-24 22:00")

        val dates = scheduleMessageQuickDates(now, timeZone)

        assertEquals(
            listOf("2026-10-25 09:00", "2026-10-25 13:00", "2026-10-26 09:00"),
            dates.map(::format),
        )
    }

    @Test
    fun `a scheduled message is pending until its date, unless it failed`() {
        val now = date("2026-11-11 15:30")
        val later = date("2026-11-12 09:00")
        val earlier = date("2026-11-11 15:00")

        assertTrue(randomScheduledMessage(scheduledAt = later, status = ScheduledMessageStatus.Scheduled).isPending(now))
        assertTrue(
            randomScheduledMessage(scheduledAt = later, status = ScheduledMessageStatus.PreparingToSchedule)
                .isPending(now),
        )
        assertTrue(randomScheduledMessage(scheduledAt = later, status = ScheduledMessageStatus.Sending).isPending(now))
        assertFalse(randomScheduledMessage(scheduledAt = later, status = ScheduledMessageStatus.Failed).isPending(now))
        assertFalse(
            randomScheduledMessage(scheduledAt = earlier, status = ScheduledMessageStatus.Scheduled).isPending(now),
        )
    }

    private fun formatter() = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ROOT).also { it.timeZone = timeZone }

    private fun date(value: String): Date = formatter().parse(value)!!

    private fun format(date: Date): String = formatter().format(date)
}
