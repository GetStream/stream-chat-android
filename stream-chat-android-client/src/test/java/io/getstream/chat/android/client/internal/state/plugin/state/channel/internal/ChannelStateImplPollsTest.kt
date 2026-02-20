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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.randomPoll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplPollsTest : ChannelStateImplTestBase() {

    @Nested
    inner class GetPoll {

        @Test
        fun `getPoll should return null when no polls exist`() = runTest {
            // when
            val result = channelState.getPoll("non_existing_poll")
            // then
            assertNull(result)
        }

        @Test
        fun `getPoll should return poll after it is registered via setMessages`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val message = createMessage(1).copy(poll = poll)
            // when
            channelState.setMessages(listOf(message))
            // then
            val result = channelState.getPoll(poll.id)
            assertNotNull(result)
            assertEquals(poll.id, result?.id)
        }

        @Test
        fun `getPoll should return poll after upsertPoll`() = runTest {
            // given
            val poll = createPoll("poll_1")
            // when
            channelState.upsertPoll(poll)
            // then
            val result = channelState.getPoll(poll.id)
            assertNotNull(result)
            assertEquals(poll.id, result?.id)
        }

        @Test
        fun `getPoll should return null for non-existing poll ID`() = runTest {
            // given
            val poll = createPoll("poll_1")
            channelState.upsertPoll(poll)
            // when
            val result = channelState.getPoll("non_existing_id")
            // then
            assertNull(result)
        }
    }

    @Nested
    inner class UpsertPoll {

        @Test
        fun `upsertPoll should add new poll`() = runTest {
            // given
            val poll = createPoll("poll_1")
            // when
            channelState.upsertPoll(poll)
            // then
            assertEquals(poll.id, channelState.getPoll(poll.id)?.id)
        }

        @Test
        fun `upsertPoll should update existing poll`() = runTest {
            // given
            val poll = createPoll("poll_1", name = "Original Name")
            channelState.upsertPoll(poll)
            // when
            val updatedPoll = poll.copy(name = "Updated Name")
            channelState.upsertPoll(updatedPoll)
            // then
            val result = channelState.getPoll(poll.id)
            assertEquals("Updated Name", result?.name)
        }

        @Test
        fun `upsertPoll should update poll in associated messages`() = runTest {
            // given
            val poll = createPoll("poll_1", name = "Original")
            val messageWithPoll = createMessage(1).copy(poll = poll)
            channelState.setMessages(listOf(messageWithPoll))
            // when
            val updatedPoll = poll.copy(name = "Updated")
            channelState.upsertPoll(updatedPoll)
            // then
            val message = channelState.getMessageById(messageWithPoll.id)
            assertEquals("Updated", message?.poll?.name)
        }

        @Test
        fun `upsertPoll should update poll in multiple associated messages`() = runTest {
            // given
            val poll = createPoll("poll_1", name = "Original")
            val message1 = createMessage(1).copy(poll = poll)
            val message2 = createMessage(2).copy(poll = poll)
            channelState.setMessages(listOf(message1, message2))
            // when
            val updatedPoll = poll.copy(name = "Updated")
            channelState.upsertPoll(updatedPoll)
            // then
            assertEquals("Updated", channelState.getMessageById(message1.id)?.poll?.name)
            assertEquals("Updated", channelState.getMessageById(message2.id)?.poll?.name)
        }

        @Test
        fun `upsertPoll should not affect messages without this poll`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val messageWithPoll = createMessage(1).copy(poll = poll)
            val messageWithoutPoll = createMessage(2)
            channelState.setMessages(listOf(messageWithPoll, messageWithoutPoll))
            // when
            val updatedPoll = poll.copy(name = "Updated")
            channelState.upsertPoll(updatedPoll)
            // then
            assertNull(channelState.getMessageById(messageWithoutPoll.id)?.poll)
        }

        @Test
        fun `upsertPoll should update poll in pinned messages`() = runTest {
            // given
            val poll = createPoll("poll_1", name = "Original")
            val pinnedMessage = createMessage(1, pinned = true, pinnedAt = Date()).copy(poll = poll)
            channelState.setMessages(listOf(pinnedMessage))
            channelState.addPinnedMessage(pinnedMessage)
            // when
            val updatedPoll = poll.copy(name = "Updated")
            channelState.upsertPoll(updatedPoll)
            // then
            val pinnedMsg = channelState.pinnedMessages.value.find { it.id == pinnedMessage.id }
            assertEquals("Updated", pinnedMsg?.poll?.name)
        }

        @Test
        fun `upsertPoll should update poll in cached messages`() = runTest {
            // given
            val poll = createPoll("poll_1", name = "Original")
            val messageWithPoll = createMessage(1).copy(poll = poll)
            channelState.setMessages(listOf(messageWithPoll))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages to test cached
            // when
            val updatedPoll = poll.copy(name = "Updated")
            channelState.upsertPoll(updatedPoll)
            // then
            val cachedMessage = channelState.getMessageById(messageWithPoll.id)
            assertEquals("Updated", cachedMessage?.poll?.name)
        }
    }

    @Nested
    inner class DeletePoll {

        @Test
        fun `deletePoll should remove poll from state`() = runTest {
            // given
            val poll = createPoll("poll_1")
            channelState.upsertPoll(poll)
            // when
            channelState.deletePoll(poll)
            // then
            assertNull(channelState.getPoll(poll.id))
        }

        @Test
        fun `deletePoll should set poll to null in associated messages`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val messageWithPoll = createMessage(1).copy(poll = poll)
            channelState.setMessages(listOf(messageWithPoll))
            // when
            channelState.deletePoll(poll)
            // then
            val message = channelState.getMessageById(messageWithPoll.id)
            assertNull(message?.poll)
        }

        @Test
        fun `deletePoll should set poll to null in multiple associated messages`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val message1 = createMessage(1).copy(poll = poll)
            val message2 = createMessage(2).copy(poll = poll)
            channelState.setMessages(listOf(message1, message2))
            // when
            channelState.deletePoll(poll)
            // then
            assertNull(channelState.getMessageById(message1.id)?.poll)
            assertNull(channelState.getMessageById(message2.id)?.poll)
        }

        @Test
        fun `deletePoll should not affect messages without this poll`() = runTest {
            // given
            val poll1 = createPoll("poll_1")
            val poll2 = createPoll("poll_2")
            val messageWithPoll1 = createMessage(1).copy(poll = poll1)
            val messageWithPoll2 = createMessage(2).copy(poll = poll2)
            channelState.setMessages(listOf(messageWithPoll1, messageWithPoll2))
            // when
            channelState.deletePoll(poll1)
            // then
            assertNull(channelState.getMessageById(messageWithPoll1.id)?.poll)
            assertNotNull(channelState.getMessageById(messageWithPoll2.id)?.poll)
        }

        @Test
        fun `deletePoll should handle non-existing poll gracefully`() = runTest {
            // given
            val poll = createPoll("non_existing_poll")
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            // when
            channelState.deletePoll(poll)
            // then - should not throw
            assertNull(channelState.getPoll(poll.id))
        }

        @Test
        fun `deletePoll should set poll to null in pinned messages`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val pinnedMessage = createMessage(1, pinned = true, pinnedAt = Date()).copy(poll = poll)
            channelState.setMessages(listOf(pinnedMessage))
            channelState.addPinnedMessage(pinnedMessage)
            // when
            channelState.deletePoll(poll)
            // then
            val pinnedMsg = channelState.pinnedMessages.value.find { it.id == pinnedMessage.id }
            assertNull(pinnedMsg?.poll)
        }

        @Test
        fun `deletePoll should set poll to null in cached messages`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val messageWithPoll = createMessage(1).copy(poll = poll)
            channelState.setMessages(listOf(messageWithPoll))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages to test cached
            // when
            channelState.deletePoll(poll)
            // then
            val cachedMessage = channelState.getMessageById(messageWithPoll.id)
            assertNull(cachedMessage?.poll)
        }
    }

    @Nested
    inner class PollRegistrationViaMessages {

        @Test
        fun `setMessages should register poll-message associations`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val message = createMessage(1).copy(poll = poll)
            // when
            channelState.setMessages(listOf(message))
            // then - poll should be retrievable
            assertNotNull(channelState.getPoll(poll.id))
        }

        @Test
        fun `upsertMessage should register poll-message association`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val message = createMessage(1).copy(poll = poll)
            // when
            channelState.upsertMessage(message)
            // then
            assertNotNull(channelState.getPoll(poll.id))
        }

        @Test
        fun `upsertMessages should register poll-message associations`() = runTest {
            // given
            val poll1 = createPoll("poll_1")
            val poll2 = createPoll("poll_2")
            val message1 = createMessage(1).copy(poll = poll1)
            val message2 = createMessage(2).copy(poll = poll2)
            // when
            channelState.upsertMessages(listOf(message1, message2))
            // then
            assertNotNull(channelState.getPoll(poll1.id))
            assertNotNull(channelState.getPoll(poll2.id))
        }

        @Test
        fun `upsertCachedMessage should register poll-message association`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val message = createMessage(1).copy(poll = poll)
            // when
            channelState.upsertCachedMessage(message)
            // then
            assertNotNull(channelState.getPoll(poll.id))
        }

        @Test
        fun `addPinnedMessage should register poll-message association`() = runTest {
            // given
            val poll = createPoll("poll_1")
            val pinnedMessage = createMessage(1, pinned = true, pinnedAt = Date()).copy(poll = poll)
            // when
            channelState.addPinnedMessage(pinnedMessage)
            // then
            assertNotNull(channelState.getPoll(poll.id))
        }
    }

    private fun createPoll(id: String, name: String = "Poll $id") = randomPoll(
        id = id,
        name = name,
    )
}
