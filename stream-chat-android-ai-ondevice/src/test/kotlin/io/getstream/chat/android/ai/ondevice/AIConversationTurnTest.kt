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

package io.getstream.chat.android.ai.ondevice

import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("StringShouldBeRawString") // The line breaks are part of what is checked.
internal class AIConversationTurnTest {

    @Test
    fun `the newest turns fit the budget`() {
        val long = "word ".repeat(300) // about 500 tokens
        val turns = listOf(
            AIConversationTurn.user("First question $long"),
            AIConversationTurn.assistant("First answer $long"),
            AIConversationTurn.user("Second question"),
            AIConversationTurn.assistant("Second answer"),
            AIConversationTurn.user("Third question"),
        )

        assertEquals(turns, AIConversationTurn.fitting(turns, tokens = 4000))
        assertEquals(
            listOf("First answer $long", "Second question", "Second answer", "Third question"),
            AIConversationTurn.fitting(turns, tokens = 600).map { it.text },
        )
        assertEquals(
            "the question always stays",
            listOf(AIConversationTurn.user("Third question")),
            AIConversationTurn.fitting(turns, tokens = 1),
        )
        assertEquals(emptyList<AIConversationTurn>(), AIConversationTurn.fitting(emptyList(), tokens = 100))
    }

    @Test
    fun `repeated turns of one role merge and empty ones go`() {
        val turns = listOf(
            AIConversationTurn.user("Are you there?"),
            AIConversationTurn.user("  "),
            AIConversationTurn.user("Hello?"),
            AIConversationTurn.assistant(""),
            AIConversationTurn.user("Anyone?"),
        )

        assertEquals(
            listOf(AIConversationTurn.user("Are you there?\n\nHello?\n\nAnyone?")),
            AIConversationTurn.fitting(turns, tokens = 1000),
        )
    }
}
