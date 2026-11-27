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

package io.getstream.chat.android.compose.ui.messages.composer.schedule

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.test.MockedChatClientTest
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ScheduleMessagePickersTest : MockedChatClientTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /** Tomorrow at 10:15, as the pickers start from a future date. */
    private val tomorrow: Date = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, 1)
        set(Calendar.HOUR_OF_DAY, 10)
        set(Calendar.MINUTE, 15)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    @Test
    fun `confirming the date picker keeps the date`() {
        var picked: Date? = null
        setDatePicker(date = tomorrow, maxDate = daysAfter(tomorrow, 30), onDatePicked = { picked = it })

        composeTestRule.onNodeWithText("OK").performClick()

        assertEquals(tomorrow, picked)
    }

    @Test
    fun `picking another day moves the date to it and keeps the time`() {
        // Today is in the month the picker shows unless tomorrow starts a new month.
        assumeTrue(sameMonth(tomorrow, Date()))
        var picked: Date? = null
        setDatePicker(date = tomorrow, maxDate = daysAfter(tomorrow, 30), onDatePicked = { picked = it })

        composeTestRule.onNode(hasText(dayLabel(Date()), substring = true)).performClick()
        composeTestRule.onNodeWithText("OK").performClick()

        assertEquals(daysAfter(tomorrow, -1), picked)
    }

    @Test
    fun `a day after the last schedulable date can't be picked`() {
        val dayAfter = daysAfter(tomorrow, 1)
        assumeTrue(sameMonth(tomorrow, dayAfter))
        setDatePicker(date = tomorrow, maxDate = tomorrow, onDatePicked = {})

        composeTestRule.onNode(hasText(dayLabel(dayAfter), substring = true)).assertIsNotEnabled()
    }

    @Test
    fun `cancelling the date picker dismisses it without a date`() {
        var picked: Date? = null
        var dismissed = false
        setDatePicker(
            date = tomorrow,
            maxDate = daysAfter(tomorrow, 30),
            onDatePicked = { picked = it },
            onDismiss = { dismissed = true },
        )

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertNull(picked)
        assertTrue(dismissed)
    }

    @Test
    fun `confirming the time picker keeps the time`() {
        var picked: Date? = null
        setTimePicker(onTimePicked = { picked = it })

        composeTestRule.onNodeWithText("OK").performClick()

        assertEquals(tomorrow, picked)
    }

    @Test
    fun `typing a time moves the date to it on the same day`() {
        var picked: Date? = null
        setTimePicker(onTimePicked = { picked = it })

        val fields = composeTestRule.onAllNodes(hasSetTextAction())
        fields[0].performTextReplacement("11")
        fields[1].performTextReplacement("45")
        composeTestRule.onNodeWithText("OK").performClick()

        val expected = Calendar.getInstance().apply {
            time = tomorrow
            set(Calendar.HOUR_OF_DAY, 11)
            set(Calendar.MINUTE, 45)
        }.time
        assertEquals(expected, picked)
    }

    @Test
    fun `cancelling the time picker dismisses it without a time`() {
        var picked: Date? = null
        var dismissed = false
        setTimePicker(onTimePicked = { picked = it }, onDismiss = { dismissed = true })

        composeTestRule.onNodeWithText("Cancel").performClick()

        assertNull(picked)
        assertTrue(dismissed)
    }

    private fun setDatePicker(
        date: Date,
        maxDate: Date,
        onDatePicked: (Date) -> Unit,
        onDismiss: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            ChatTheme {
                ScheduleMessageDatePicker(
                    date = date,
                    maxDate = maxDate,
                    onDatePicked = onDatePicked,
                    onDismiss = onDismiss,
                )
            }
        }
    }

    private fun setTimePicker(onTimePicked: (Date) -> Unit, onDismiss: () -> Unit = {}) {
        composeTestRule.setContent {
            ChatTheme {
                ScheduleMessageTimePicker(date = tomorrow, onTimePicked = onTimePicked, onDismiss = onDismiss)
            }
        }
    }

    private fun daysAfter(date: Date, days: Int): Date =
        Calendar.getInstance().apply {
            time = date
            add(Calendar.DAY_OF_YEAR, days)
        }.time

    private fun sameMonth(a: Date, b: Date): Boolean {
        val first = Calendar.getInstance().apply { time = a }
        val second = Calendar.getInstance().apply { time = b }
        return first.get(Calendar.MONTH) == second.get(Calendar.MONTH) &&
            first.get(Calendar.YEAR) == second.get(Calendar.YEAR)
    }

    /** The date as the Material date picker describes a day, like "November 13, 2026". */
    private fun dayLabel(date: Date): String = SimpleDateFormat("MMMM d, yyyy", Locale.US).format(date)
}
