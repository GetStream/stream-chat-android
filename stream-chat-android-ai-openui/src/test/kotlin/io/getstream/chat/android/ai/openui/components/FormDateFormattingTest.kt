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

package io.getstream.chat.android.ai.openui.components

import io.getstream.chat.android.ai.openui.lang.OpenUIObject
import io.getstream.chat.android.ai.openui.lang.OpenUIValue
import io.getstream.chat.android.ai.openui.lang.stringValue
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class FormDateFormattingTest {

    @Test
    fun `a date or a date-time reads as its day`() {
        val day = FormDateFormatting.millis("2026-11-04")

        assertEquals("2026-11-04", day?.let(FormDateFormatting::string))
        assertEquals(day, FormDateFormatting.millis("2026-11-04T10:00:00Z"))
        assertEquals(day, FormDateFormatting.millis("2026-11-04T23:30:00-05:30"))
    }

    @Test
    fun `text that is not a date reads as no date`() {
        assertNull(FormDateFormatting.millis("tomorrow"))
        assertNull(FormDateFormatting.millis("2026-13-40"))
        assertNull(FormDateFormatting.millis(""))
        assertNull(FormDateFormatting.millis(null))
        assertNull(FormDateFormatting.millis("2026-11-04garbage"))
        assertNull(FormDateFormatting.millis("2026-11-045"))
    }

    @Test
    fun `picking the end of a range with a null start sets the start too`() {
        val day = FormDateFormatting.millis("2026-11-04")!!
        val range = OpenUIObject.of("from" to OpenUIValue.Null)

        val picked = FormDateFormatting.withTo(range, day)

        assertEquals("2026-11-04", picked["from"].stringValue)
        assertEquals("2026-11-04", picked["to"].stringValue)
    }

    @Test
    fun `the end of a range is never before its start`() {
        val range = OpenUIObject.of("from" to OpenUIValue.Str("2026-11-10"), "to" to OpenUIValue.Str("2026-11-01"))

        val picked = FormDateFormatting.withTo(range, FormDateFormatting.millis("2026-11-01")!!)
        val start = FormDateFormatting.withFrom(range, FormDateFormatting.millis("2026-11-20")!!)

        assertEquals("2026-11-10", picked["to"].stringValue)
        assertEquals("2026-11-20", start["to"].stringValue)
    }

    @Test
    fun `a picker opens on its date unless that is before the minimum`() {
        val early = FormDateFormatting.millis("2026-11-01")!!
        val late = FormDateFormatting.millis("2026-11-10")!!

        assertEquals(late, FormDateFormatting.opening(early, late))
        assertEquals(early, FormDateFormatting.opening(early, null))
        assertEquals(late, FormDateFormatting.opening(late, early))
        assertEquals(late, FormDateFormatting.opening(null, late))
    }

    @Test
    fun `today is the day in the user's time zone`() {
        // 08:00 on Nov 5 in UTC+10 is still Nov 4 in UTC.
        val now = FormDateFormatting.millis("2026-11-04")!! + 22 * HOUR

        val today = FormDateFormatting.today(now, TimeZone.getTimeZone("GMT+10:00"))

        assertEquals("2026-11-05", FormDateFormatting.string(today))
        assertEquals("2026-11-04", FormDateFormatting.string(FormDateFormatting.today(now, TimeZone.getTimeZone("UTC"))))
    }

    @Test
    fun `the label shows the stored day in the given locale`() {
        val day = FormDateFormatting.millis("2026-11-04")!!

        assertEquals("Nov 4, 2026", FormDateFormatting.label(day, Locale.US))
    }

    private companion object {
        const val HOUR = 60 * 60 * 1000L
    }
}
