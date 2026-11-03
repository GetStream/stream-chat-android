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

package io.getstream.chat.android.ai.openui.utils

import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals

internal class NumberFormattingTest {

    @Test
    fun `numbers follow the locale`() {
        assertEquals("1,234.5", NumberFormatting.decimal(1234.5, Locale.US))
        assertEquals("1.234,5", NumberFormatting.decimal(1234.5, Locale.GERMANY))
        assertEquals("12", NumberFormatting.integer(12, Locale.US))
        assertEquals("12.5%", NumberFormatting.percent(12.5, Locale.US))
    }

    @Test
    fun `numbers that aren't finite read as JavaScript writes them`() {
        assertEquals("NaN", NumberFormatting.decimal(Double.NaN, Locale.US))
        assertEquals("Infinity%", NumberFormatting.percent(Double.POSITIVE_INFINITY, Locale.US))
    }
}
