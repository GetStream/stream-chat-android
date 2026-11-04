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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class TableDateFormattingTest {

    @Test
    fun `a date or a date-time reads as its day`() {
        val day = TableDateFormatting.date("2026-11-04")

        assertEquals("2026-11-04", day?.let(TableDateFormatting::string))
        assertEquals(day, TableDateFormatting.date("2026-11-04T10:00:00Z"))
        assertEquals(day, TableDateFormatting.date("2026-11-04T23:30:00-05:30"))
    }

    @Test
    fun `text that is not a date reads as no date`() {
        assertNull(TableDateFormatting.date("tomorrow"))
        assertNull(TableDateFormatting.date(""))
        assertNull(TableDateFormatting.date(null))
    }
}
