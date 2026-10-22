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
import org.junit.Test

internal class AIConversationScrollTest {

    @Test
    fun `a turn added after the last one scrolls to the top`() {
        assertEquals(AIConversationScroll.ToTop("q2"), conversationScroll("q1", listOf("q1", "q2")))
    }

    @Test
    fun `several turns added scroll the last one to the top`() {
        assertEquals(AIConversationScroll.ToTop("q3"), conversationScroll("q1", listOf("q1", "q2", "q3")))
    }

    @Test
    fun `a conversation that loads shows its end`() {
        assertEquals(AIConversationScroll.ToEnd, conversationScroll(null, listOf("q1", "q2")))
    }

    @Test
    fun `a conversation that is replaced shows its end`() {
        assertEquals(AIConversationScroll.ToEnd, conversationScroll("q1", listOf("other-q1", "other-q2")))
    }

    @Test
    fun `older turns or a streaming reply keep the position`() {
        assertEquals(AIConversationScroll.None, conversationScroll("q2", listOf("q0", "q1", "q2")))
    }

    @Test
    fun `a conversation that is cleared keeps the position`() {
        assertEquals(AIConversationScroll.None, conversationScroll("q1", emptyList()))
    }
}
