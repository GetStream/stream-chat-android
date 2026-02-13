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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplPinnedMessagesTest : ChannelStateImplTestBase() {

    @Nested
    inner class AddPinnedMessage {

        @Test
        fun `addPinnedMessage should add message to pinned messages`() = runTest {
            // given
            val pinnedMessage = createMessage(1, pinned = true, pinnedAt = Date())
            // when
            channelState.addPinnedMessage(pinnedMessage)
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
            assertEquals(pinnedMessage.id, channelState.pinnedMessages.value.first().id)
        }

        @Test
        fun `addPinnedMessage should update existing pinned message`() = runTest {
            // given
            val pinnedMessage = createMessage(1, text = "Original", pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(pinnedMessage)
            // when
            val updatedPinnedMessage = pinnedMessage.copy(text = "Updated")
            channelState.addPinnedMessage(updatedPinnedMessage)
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
            assertEquals("Updated", channelState.pinnedMessages.value.first().text)
        }

        @Test
        fun `addPinnedMessage should register quoted message reference`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Quoted message")
            val pinnedMessage = createMessage(2, pinned = true, pinnedAt = Date(), replyTo = quotedMessage)
            // when
            channelState.addPinnedMessage(pinnedMessage)
            // then
            val quotingIds = channelState.quotedMessagesMap.value[quotedMessage.id]
            assertEquals(1, quotingIds?.size)
            assertEquals(pinnedMessage.id, quotingIds?.first())
        }
    }

    @Nested
    inner class AddPinnedMessages {

        @Test
        fun `addPinnedMessages should sort messages by pinnedAt`() = runTest {
            // given - messages in unsorted order
            val pinnedMessage2 = createMessage(2, pinned = true, pinnedAt = Date(2000))
            val pinnedMessage1 = createMessage(1, pinned = true, pinnedAt = Date(1000))
            val pinnedMessage3 = createMessage(3, pinned = true, pinnedAt = Date(3000))
            // when
            channelState.addPinnedMessages(listOf(pinnedMessage2, pinnedMessage1, pinnedMessage3))
            // then - should be sorted by pinnedAt ascending
            val sortedIds = channelState.pinnedMessages.value.map { it.id }
            assertEquals(listOf("message_1", "message_2", "message_3"), sortedIds)
        }

        @Test
        fun `addPinnedMessages with empty list should not change state`() = runTest {
            // given
            val existingPinnedMessage = createMessage(1, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(existingPinnedMessage)
            // when
            channelState.addPinnedMessages(emptyList())
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
        }

        @Test
        fun `addPinnedMessages should update existing messages`() = runTest {
            // given
            val pinnedMessage1 = createMessage(1, text = "Original 1", pinned = true, pinnedAt = Date(1000))
            val pinnedMessage2 = createMessage(2, text = "Original 2", pinned = true, pinnedAt = Date(2000))
            channelState.addPinnedMessages(listOf(pinnedMessage1, pinnedMessage2))
            // when
            val updatedPinnedMessage1 = pinnedMessage1.copy(text = "Updated 1")
            channelState.addPinnedMessages(listOf(updatedPinnedMessage1))
            // then
            assertEquals(2, channelState.pinnedMessages.value.size)
            assertEquals("Updated 1", channelState.pinnedMessages.value.find { it.id == pinnedMessage1.id }?.text)
            assertEquals("Original 2", channelState.pinnedMessages.value.find { it.id == pinnedMessage2.id }?.text)
        }

        @Test
        fun `addPinnedMessages should register quoted message references for all messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Quoted message")
            val pinnedMessage1 = createMessage(2, pinned = true, pinnedAt = Date(1000), replyTo = quotedMessage)
            val pinnedMessage2 = createMessage(3, pinned = true, pinnedAt = Date(2000), replyTo = quotedMessage)
            // when
            channelState.addPinnedMessages(listOf(pinnedMessage1, pinnedMessage2))
            // then
            val quotingIds = channelState.quotedMessagesMap.value[quotedMessage.id]
            assertEquals(2, quotingIds?.size)
            assertTrue(quotingIds?.containsAll(listOf(pinnedMessage1.id, pinnedMessage2.id)) == true)
        }
    }

    @Nested
    inner class DeletePinnedMessage {

        @Test
        fun `deletePinnedMessage should remove message from pinned messages`() = runTest {
            // given
            val pinnedMessage1 = createMessage(1, pinned = true, pinnedAt = Date(1000))
            val pinnedMessage2 = createMessage(2, pinned = true, pinnedAt = Date(2000))
            channelState.addPinnedMessages(listOf(pinnedMessage1, pinnedMessage2))
            // when
            channelState.deletePinnedMessage(pinnedMessage1.id)
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
            assertFalse(channelState.pinnedMessages.value.any { it.id == pinnedMessage1.id })
            assertTrue(channelState.pinnedMessages.value.any { it.id == pinnedMessage2.id })
        }

        @Test
        fun `deletePinnedMessage should do nothing for non-existing message`() = runTest {
            // given
            val pinnedMessage = createMessage(1, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(pinnedMessage)
            // when
            channelState.deletePinnedMessage("non_existing_id")
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
        }

        @Test
        fun `deletePinnedMessage should handle empty pinned messages list`() = runTest {
            // given - no pinned messages
            assertTrue(channelState.pinnedMessages.value.isEmpty())
            // when
            channelState.deletePinnedMessage("some_id")
            // then - should not throw
            assertTrue(channelState.pinnedMessages.value.isEmpty())
        }
    }
}
