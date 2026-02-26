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

package io.getstream.chat.android.compose.ui.threads

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Unit tests for [ThreadTimestampFormatter].
 *
 * A fixed "now" of Monday 2024-01-15 14:30:00 UTC is used throughout so that day-of-week
 * assertions are deterministic. Both [Locale.US] and UTC timezone are pinned for the
 * duration of each test and restored afterwards.
 */
@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ThreadTimestampFormatterTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    private lateinit var savedLocale: Locale
    private lateinit var savedTimeZone: TimeZone

    // Monday 2024-01-15 14:30:00.000 UTC
    private val now: Date = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        set(2024, Calendar.JANUARY, 15, 14, 30, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    @Before
    fun setUp() {
        savedLocale = Locale.getDefault()
        savedTimeZone = TimeZone.getDefault()
        Locale.setDefault(Locale.US)
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        Locale.setDefault(savedLocale)
        TimeZone.setDefault(savedTimeZone)
    }

    // region Just now (< 1 minute)

    @Test
    fun `returns Just now for date 30 seconds ago`() {
        val date = Date(now.time - 30_000L)
        assertEquals("Just now", format(date))
    }

    @Test
    fun `returns Just now for date 59 seconds and 999ms ago`() {
        val date = Date(now.time - 59_999L)
        assertEquals("Just now", format(date))
    }

    @Test
    fun `returns Just now for date 30 seconds in the future`() {
        val date = Date(now.time + 30_000L)
        assertEquals("Just now", format(date))
    }

    // endregion

    // region Future (> 1 minute ahead)

    @Test
    fun `returns just the time for a date more than one minute in the future`() {
        // 2 minutes ahead → 14:32
        val date = Date(now.time + 2 * 60_000L)
        assertEquals("14:32", format(date))
    }

    // endregion

    // region Same calendar day

    @Test
    fun `returns Today at time for a date two hours earlier on the same day`() {
        // 14:30 - 2h = 12:30, still Jan 15
        val date = Date(now.time - 2 * 60 * 60 * 1_000L)
        assertEquals("Today at 12:30", format(date))
    }

    @Test
    fun `returns Today at time for a date just over one minute ago on the same day`() {
        val date = Date(now.time - 61_000L)
        assertEquals("Today at 14:28", format(date))
    }

    // endregion

    // region Yesterday

    @Test
    fun `returns Yesterday at time for a date on the previous calendar day`() {
        // Jan 14 14:30 UTC
        val date = nowOffset(Calendar.DAY_OF_YEAR, -1)
        assertEquals("Yesterday at 14:30", format(date))
    }

    // endregion

    // region Within last 7 days (day-of-week format)

    @Test
    fun `returns day of week at time for a date two days ago`() {
        // Jan 13 14:30 UTC = Saturday
        val date = nowOffset(Calendar.DAY_OF_YEAR, -2)
        assertEquals("Saturday at 14:30", format(date))
    }

    @Test
    fun `returns day of week at time for a date six days ago`() {
        // Jan 9 14:30 UTC = Tuesday
        val date = nowOffset(Calendar.DAY_OF_YEAR, -6)
        assertEquals("Tuesday at 14:30", format(date))
    }

    // endregion

    // region Older than 7 days (full date format)

    @Test
    fun `returns full date at time for exactly seven days ago`() {
        // Jan 8 14:30 UTC — elapsedMs == WEEK_IN_MS, so NOT in the day-of-week branch.
        // DateUtils includes the year because 2024 != the current year when the test runs.
        val date = nowOffset(Calendar.DAY_OF_YEAR, -7)
        assertEquals("Jan 8, 2024 at 14:30", format(date))
    }

    @Test
    fun `returns full date at time for a date older than one month`() {
        // Dec 16 2023 14:30 UTC
        val date = nowOffset(Calendar.DAY_OF_YEAR, -30)
        assertEquals("Dec 16, 2023 at 14:30", format(date))
    }

    // endregion

    // region Helpers

    private fun format(date: Date) = ThreadTimestampFormatter.format(date, context, now)

    private fun nowOffset(field: Int, amount: Int): Date =
        Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            time = now
            add(field, amount)
        }.time

    // endregion
}
