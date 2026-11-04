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

internal class CardComponentsTest {

    @Test
    fun `image sizes stay between 8 and 200`() {
        assertEquals(40.0, imageTextSize(null))
        assertEquals(40.0, imageTextSize(Double.POSITIVE_INFINITY))
        assertEquals(8.0, imageTextSize(-5.0))
        assertEquals(200.0, imageTextSize(1e9))
        assertEquals(64.0, imageTextSize(64.0))
    }

    @Test
    fun `card block gaps stay between 0 and 48`() {
        assertEquals(48.0, cardBlockGap(100_000_000.0))
        assertEquals(0.0, cardBlockGap(-8.0))
        assertEquals(12.0, cardBlockGap(12.0))
        assertNull(cardBlockGap(Double.POSITIVE_INFINITY))
        assertNull(cardBlockGap(Double.NaN))
    }
}
