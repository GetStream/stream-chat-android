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
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIMessageTurnTest {

    private data class Message(val id: String, val isFromUser: Boolean)

    private fun turns(vararg messages: Message) = messageTurns(messages.toList(), Message::id, Message::isFromUser)

    @Test
    fun `no messages make no turns`() {
        assertTrue(turns().isEmpty())
    }

    @Test
    fun `each message the person sent starts a turn`() {
        val turns = turns(
            Message("q1", isFromUser = true),
            Message("a1", isFromUser = false),
            Message("q2", isFromUser = true),
            Message("a2", isFromUser = false),
            Message("a2-more", isFromUser = false),
        )

        assertEquals(listOf("q1", "q2"), turns.map { it.id })
        assertEquals(listOf(listOf("q1", "a1"), listOf("q2", "a2", "a2-more")), turns.ids())
    }

    @Test
    fun `replies before the person's first message make a leading turn`() {
        val turns = turns(
            Message("greeting", isFromUser = false),
            Message("tip", isFromUser = false),
            Message("q1", isFromUser = true),
        )

        assertEquals(listOf(listOf("greeting", "tip"), listOf("q1")), turns.ids())
        assertEquals("greeting", turns.first().id)
    }

    @Test
    fun `several messages in a row from the person start a turn each`() {
        val turns = turns(
            Message("q1", isFromUser = true),
            Message("q2", isFromUser = true),
            Message("a2", isFromUser = false),
        )

        assertEquals(listOf(listOf("q1"), listOf("q2", "a2")), turns.ids())
    }

    @Test
    fun `a message in turnStarts starts a turn whoever sent it`() {
        val messages = listOf(
            Message("q1", isFromUser = true),
            Message("a1", isFromUser = false),
            Message("a1-more", isFromUser = false),
        )

        val turns = messageTurns(messages, Message::id, Message::isFromUser, turnStarts = setOf("a1"))

        assertEquals(listOf(listOf("q1"), listOf("a1", "a1-more")), turns.ids())
    }

    private fun List<AIMessageTurn<Message>>.ids() = map { turn -> turn.messages.map { it.id } }
}
