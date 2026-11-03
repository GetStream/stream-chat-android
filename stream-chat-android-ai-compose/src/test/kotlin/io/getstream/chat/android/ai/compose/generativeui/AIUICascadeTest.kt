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

package io.getstream.chat.android.ai.compose.generativeui

import org.junit.Assert.assertEquals
import org.junit.Test

internal class AIUICascadeTest {

    @Test
    fun `parts that come in at once follow one another, waiting at most a moment`() {
        var now = 1_000L
        val cascade = AIUICascade { now }

        val waits = List(7) { cascade.delay() }

        assertEquals(listOf(0L, 100L, 200L, 300L, 400L, 400L, 400L), waits)
    }

    @Test
    fun `a part that comes in after the others have gone in waits for nothing`() {
        var now = 1_000L
        val cascade = AIUICascade { now }
        cascade.delay()
        cascade.delay()

        now += 1_000L

        assertEquals(0L, cascade.delay())
        assertEquals(100L, cascade.delay())
    }
}
