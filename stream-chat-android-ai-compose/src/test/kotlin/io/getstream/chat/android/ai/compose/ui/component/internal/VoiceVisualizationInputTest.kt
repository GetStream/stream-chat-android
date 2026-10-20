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

package io.getstream.chat.android.ai.compose.ui.component.internal

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class VoiceVisualizationInputTest {

    @Test
    fun `a level outside 0 to 1 is clamped`() {
        assertEquals(1f, audioLevelInput(1.5f))
        assertEquals(0f, audioLevelInput(-0.2f))
    }

    @Test
    fun `a level that is not a number counts as no live audio`() {
        assertNull(audioLevelInput(Float.NaN))
        assertNull(audioLevelInput(Float.POSITIVE_INFINITY))
    }

    @Test
    fun `expressivity is clamped to 0 to 2, and one that is not a number is the default`() {
        assertEquals(2f, expressivityInput(3f))
        assertEquals(1f, expressivityInput(Float.NaN))
        assertEquals(1f, expressivityInput(Float.NEGATIVE_INFINITY))
    }
}
