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

import io.getstream.chat.android.ai.compose.conversation.AIMessage
import io.getstream.chat.android.ai.compose.conversation.AIMessageAttachment
import io.getstream.chat.android.ai.compose.conversation.AIMessageRole
import io.getstream.chat.android.ai.compose.conversation.AIPendingMessage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal class AIPendingMessageTrackerTest {

    private data class Message(
        override val id: String,
        override val role: AIMessageRole,
        override val text: String = "Text",
        override val isGenerating: Boolean = false,
    ) : AIMessage

    private val tracker = AIPendingMessageTracker()
    private val photo = AIMessageAttachment(kind = AIMessageAttachment.Kind.Image, name = "photo.jpg")
    private val pending = AIPendingMessage(id = "pending", text = "Plan a trip", attachments = listOf(photo))

    private fun question(id: String, text: String = "Text") = Message(id, AIMessageRole.User, text)
    private fun answer(id: String) = Message(id, AIMessageRole.Assistant)

    private fun ids(messages: List<AIMessage>, pending: AIPendingMessage?) =
        tracker.entries(messages, pending).map { it.id }

    @Test
    fun `entries leave out replies with nothing to show yet`() {
        val entries = tracker.entries(
            listOf(question("q1"), Message("a1", AIMessageRole.Assistant, text = "", isGenerating = true)),
            pending = null,
        )

        assertEquals(listOf("q1"), entries.map { it.id })
    }

    @Test
    fun `entries show the pending message after the messages`() {
        val entries = tracker.entries(listOf(question("q1"), answer("a1")), pending)

        assertEquals(listOf("q1", "a1", "pending"), entries.map { it.id })
        assertTrue(entries[2].isFromUser)
        assertEquals(pending, entries[2].pending)
    }

    @Test
    fun `when the copy arrives it takes over the pending row with its attachments`() {
        val before = listOf(question("q1"), answer("a1"))
        tracker.entries(before, pending)

        val entries = tracker.entries(before + question("q2"), pending)

        // So the turn carries on rather than starting again.
        assertEquals(listOf("q1", "a1", "pending"), entries.map { it.id })
        assertEquals("q2", entries[2].message?.id)
        // So the photos show from the phone rather than load again.
        assertEquals(listOf(photo), entries[2].sentAttachments)
    }

    @Test
    fun `when the pending message is no longer passed the copy keeps its row`() {
        tracker.entries(listOf(question("q1")), pending)
        tracker.entries(listOf(question("q1"), question("q2")), pending)

        assertEquals(listOf("q1", "pending"), ids(listOf(question("q1"), question("q2")), pending = null))
    }

    @Test
    fun `replies do not take over the pending row`() {
        tracker.entries(listOf(question("q1")), pending)

        assertEquals(listOf("q1", "a1", "pending"), ids(listOf(question("q1"), answer("a1")), pending))
    }

    @Test
    fun `older messages loading above do not take over the pending row`() {
        val shown = listOf(question("q1"), answer("a1"))
        tracker.entries(shown, pending)

        val older = listOf(question("q0"), answer("a0"))
        assertEquals(listOf("q0", "a0", "q1", "a1", "pending"), ids(older + shown, pending))

        // The copy still takes over the row.
        assertEquals(listOf("q0", "a0", "q1", "a1", "pending"), ids(older + shown + question("q2"), pending))
    }

    @Test
    fun `when older messages arrive with the copy the copy takes over the pending row`() {
        tracker.entries(emptyList(), pending)

        val entries = ids(listOf(question("q0"), answer("a0"), question("q1", text = "Plan a trip")), pending)

        assertEquals(listOf("q0", "a0", "pending"), entries)
    }

    @Test
    fun `when only older messages arrive they do not take over the pending row`() {
        tracker.entries(emptyList(), pending)

        val entries = ids(listOf(question("q0"), answer("a0"), question("q1")), pending)

        assertEquals(listOf("q0", "a0", "q1", "pending"), entries)
    }

    @Test
    fun `when the copy arrives with the pending message it takes over the pending row`() {
        val before = listOf(question("q1"), answer("a1"))
        tracker.entries(before, pending = null)

        val messages = before + question("q2")

        assertEquals(listOf("q1", "a1", "pending"), ids(messages, pending))
        assertEquals(listOf("q1", "a1", "pending"), ids(messages, pending))
    }

    @Test
    fun `a message sent again with the same text waits for its own copy`() {
        val before = listOf(question("q1", text = "Plan a trip"), answer("a1"))
        tracker.entries(before, pending = null)

        assertEquals(listOf("q1", "a1", "pending"), ids(before, pending))
        assertEquals(listOf("q1", "a1", "pending"), ids(before + question("q2", text = "Plan a trip"), pending))
        assertEquals("q2", tracker.entries(before + question("q2", text = "Plan a trip"), pending)[2].message?.id)
    }

    @Test
    fun `when the copy has the pending message's id it takes over wherever it lands`() {
        val identified = AIPendingMessage(id = "q1", text = "Plan a trip")
        tracker.entries(emptyList(), identified)

        assertEquals(listOf("q1", "q0"), ids(listOf(question("q1"), question("q0")), identified))
    }

    @Test
    fun `a message sent after the copy does not take over the row too`() {
        tracker.entries(listOf(question("q1")), pending)
        tracker.entries(listOf(question("q1"), question("q2")), pending)

        val entries = ids(listOf(question("q1"), question("q2"), question("q3")), pending)

        assertEquals(listOf("q1", "pending", "q3"), entries)
    }

    @Test
    fun `the latest entry is the latest reply when the agent wrote it`() {
        val entries = tracker.entries(listOf(question("q1"), answer("a1"), question("q2"), answer("a2")), null)

        assertEquals(listOf(false, false, false, true), entries.map { it.isLatestReply })
        assertFalse(tracker.entries(listOf(question("q1"), answer("a1")), pending).any { it.isLatestReply })
    }

    @Test
    fun `isAwaitingReply while the pending message waits for its copy`() {
        val messages = listOf(question("q1"), answer("a1"))
        tracker.entries(messages, pending)

        assertTrue(tracker.isAwaitingReply(messages, pending))
    }

    @Test
    fun `isAwaitingReply when the copy is the latest message`() {
        tracker.entries(listOf(question("q1")), pending)
        val messages = listOf(question("q1"), question("q2"))
        tracker.entries(messages, pending)

        assertTrue(tracker.isAwaitingReply(messages, pending))
        // Once the app stops passing it, the app says.
        assertFalse(tracker.isAwaitingReply(messages, pending = null))
    }

    @Test
    fun `isAwaitingReply while the reply is written but shows nothing`() {
        val messages = listOf(question("q1"), Message("a1", AIMessageRole.Assistant, text = "", isGenerating = true))

        assertTrue(tracker.isAwaitingReply(messages, pending = null))
    }

    @Test
    fun `isAwaitingReply once the reply shows something is false`() {
        val messages = listOf(question("q1"), Message("a1", AIMessageRole.Assistant, text = "Sure", isGenerating = true))

        assertFalse(tracker.isAwaitingReply(messages, pending = null))
    }

    @Test
    fun `isAwaitingReply when the latest message is the person's own is left to the app`() {
        assertFalse(tracker.isAwaitingReply(listOf(question("q1")), pending = null))
    }
}
