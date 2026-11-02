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

package io.getstream.chat.android.models

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class QuietHoursDayTest {

    @Test
    fun `fromValue returns the day for each of the seven values`() {
        val days = listOf("mon", "tue", "wed", "thu", "fri", "sat", "sun").map(QuietHoursDay::fromValue)

        assertEquals(QuietHoursDay.weekdays + QuietHoursDay.weekend, days)
    }

    @Test
    fun `fromValue returns null for a value that is not a day`() {
        assertNull(QuietHoursDay.fromValue("monday"))
        assertNull(QuietHoursDay.fromValue("Mon"))
        assertNull(QuietHoursDay.fromValue(null))
    }
}
