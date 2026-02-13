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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplQuotedMessagesTest : ChannelStateImplTestBase() {

    @Nested
    inner class SetRepliedMessage {

        @Test
        fun `setRepliedMessage should set the replied message`() = runTest {
            // given
            val message = createMessage(1)
            // when
            channelState.setRepliedMessage(message)
            // then
            assertEquals(message.id, channelState.repliedMessage.value?.id)
        }

        @Test
        fun `setRepliedMessage with null should clear the replied message`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setRepliedMessage(message)
            // when
            channelState.setRepliedMessage(null)
            // then
            assertNull(channelState.repliedMessage.value)
        }
    }

    @Nested
    inner class AddQuotedMessage {

        @Test
        fun `addQuotedMessage should add mapping from quoted to quoting message`() = runTest {
            // given
            val quotedMessageId = "quoted_1"
            val quotingMessageId = "quoting_1"
            // when
            channelState.addQuotedMessage(quotedMessageId, quotingMessageId)
            // then
            val quotingIds = channelState.quotedMessagesMap.value[quotedMessageId]
            assertEquals(1, quotingIds?.size)
            assertEquals(quotingMessageId, quotingIds?.first())
        }

        @Test
        fun `addQuotedMessage should accumulate multiple quoting messages`() = runTest {
            // given
            val quotedMessageId = "quoted_1"
            // when
            channelState.addQuotedMessage(quotedMessageId, "quoting_1")
            channelState.addQuotedMessage(quotedMessageId, "quoting_2")
            channelState.addQuotedMessage(quotedMessageId, "quoting_3")
            // then
            val quotingIds = channelState.quotedMessagesMap.value[quotedMessageId]
            assertEquals(3, quotingIds?.size)
            assertTrue(quotingIds?.containsAll(listOf("quoting_1", "quoting_2", "quoting_3")) == true)
        }

        @Test
        fun `addQuotedMessage should handle multiple quoted messages`() = runTest {
            // given & when
            channelState.addQuotedMessage("quoted_1", "quoting_1")
            channelState.addQuotedMessage("quoted_2", "quoting_2")
            // then
            assertEquals(2, channelState.quotedMessagesMap.value.size)
            assertEquals(listOf("quoting_1"), channelState.quotedMessagesMap.value["quoted_1"])
            assertEquals(listOf("quoting_2"), channelState.quotedMessagesMap.value["quoted_2"])
        }
    }

    @Nested
    inner class UpdateQuotedMessageReferences {

        @Test
        fun `updateQuotedMessageReferences should update quoting messages in main messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Original quoted text")
            val quotingMessage = createMessage(2, replyTo = quotedMessage)
            channelState.setMessages(listOf(quotedMessage, quotingMessage))
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage.id)
            // when
            val updatedQuotedMessage = quotedMessage.copy(text = "Updated quoted text")
            channelState.updateQuotedMessageReferences(updatedQuotedMessage)
            // then
            val updatedQuotingMessage = channelState.messages.value.find { it.id == quotingMessage.id }
            assertEquals("Updated quoted text", updatedQuotingMessage?.replyTo?.text)
        }

        @Test
        fun `updateQuotedMessageReferences should update quoting messages in cached messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Original quoted text")
            val quotingMessage = createMessage(2, replyTo = quotedMessage)
            channelState.setMessages(listOf(quotedMessage, quotingMessage))
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage.id)
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList())
            // when
            val updatedQuotedMessage = quotedMessage.copy(text = "Updated quoted text")
            channelState.updateQuotedMessageReferences(updatedQuotedMessage)
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            val updatedQuotingMessage = cachedMessages.find { it.id == quotingMessage.id }
            assertEquals("Updated quoted text", updatedQuotingMessage?.replyTo?.text)
        }

        @Test
        fun `updateQuotedMessageReferences should update quoting messages in pinned messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Original quoted text")
            val quotingMessage = createMessage(2, replyTo = quotedMessage, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(quotingMessage)
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage.id)
            // when
            val updatedQuotedMessage = quotedMessage.copy(text = "Updated quoted text")
            channelState.updateQuotedMessageReferences(updatedQuotedMessage)
            // then
            val updatedQuotingMessage = channelState.pinnedMessages.value.find { it.id == quotingMessage.id }
            assertEquals("Updated quoted text", updatedQuotingMessage?.replyTo?.text)
        }

        @Test
        fun `updateQuotedMessageReferences should update multiple quoting messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Original quoted text")
            val quotingMessage1 = createMessage(2, replyTo = quotedMessage)
            val quotingMessage2 = createMessage(3, replyTo = quotedMessage)
            channelState.setMessages(listOf(quotedMessage, quotingMessage1, quotingMessage2))
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage1.id)
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage2.id)
            // when
            val updatedQuotedMessage = quotedMessage.copy(text = "Updated quoted text")
            channelState.updateQuotedMessageReferences(updatedQuotedMessage)
            // then
            val messages = channelState.messages.value
            assertEquals("Updated quoted text", messages.find { it.id == quotingMessage1.id }?.replyTo?.text)
            assertEquals("Updated quoted text", messages.find { it.id == quotingMessage2.id }?.replyTo?.text)
        }

        @Test
        fun `updateQuotedMessageReferences should do nothing if no quoting messages exist`() = runTest {
            // given
            val message1 = createMessage(1)
            val message2 = createMessage(2)
            channelState.setMessages(listOf(message1, message2))
            val initialMessages = channelState.messages.value
            // when
            val quotedMessage = createMessage(99, text = "Non-existent quoted message")
            channelState.updateQuotedMessageReferences(quotedMessage)
            // then
            assertEquals(initialMessages, channelState.messages.value)
        }
    }

    @Nested
    inner class DeleteQuotedMessageReferences {

        @Test
        fun `deleteQuotedMessageReferences should clear replyTo in quoting messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Quoted text")
            val quotingMessage = createMessage(2, replyTo = quotedMessage)
            channelState.setMessages(listOf(quotedMessage, quotingMessage))
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage.id)
            // when
            channelState.deleteQuotedMessageReferences(quotedMessage.id)
            // then
            val updatedQuotingMessage = channelState.messages.value.find { it.id == quotingMessage.id }
            assertNull(updatedQuotingMessage?.replyTo)
        }

        @Test
        fun `deleteQuotedMessageReferences should clear replyTo in cached messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Quoted text")
            val quotingMessage = createMessage(2, replyTo = quotedMessage)
            channelState.setMessages(listOf(quotedMessage, quotingMessage))
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage.id)
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList())
            // when
            channelState.deleteQuotedMessageReferences(quotedMessage.id)
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            val updatedQuotingMessage = cachedMessages.find { it.id == quotingMessage.id }
            assertNull(updatedQuotingMessage?.replyTo)
        }

        @Test
        fun `deleteQuotedMessageReferences should clear replyTo in pinned messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Quoted text")
            val quotingMessage = createMessage(2, replyTo = quotedMessage, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(quotingMessage)
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage.id)
            // when
            channelState.deleteQuotedMessageReferences(quotedMessage.id)
            // then
            val updatedQuotingMessage = channelState.pinnedMessages.value.find { it.id == quotingMessage.id }
            assertNull(updatedQuotingMessage?.replyTo)
        }

        @Test
        fun `deleteQuotedMessageReferences should remove entry from quotedMessagesMap`() = runTest {
            // given
            val quotedMessageId = "quoted_1"
            channelState.addQuotedMessage(quotedMessageId, "quoting_1")
            channelState.addQuotedMessage(quotedMessageId, "quoting_2")
            // when
            channelState.deleteQuotedMessageReferences(quotedMessageId)
            // then
            assertNull(channelState.quotedMessagesMap.value[quotedMessageId])
        }

        @Test
        fun `deleteQuotedMessageReferences should clean up map entry even if no quoting messages exist`() = runTest {
            // given
            val quotedMessageId = "non_existent_quoted"
            // when
            channelState.deleteQuotedMessageReferences(quotedMessageId)
            // then - should not throw and map should not contain the entry
            assertNull(channelState.quotedMessagesMap.value[quotedMessageId])
        }

        @Test
        fun `deleteQuotedMessageReferences should clear multiple quoting messages`() = runTest {
            // given
            val quotedMessage = createMessage(1, text = "Quoted text")
            val quotingMessage1 = createMessage(2, replyTo = quotedMessage)
            val quotingMessage2 = createMessage(3, replyTo = quotedMessage)
            channelState.setMessages(listOf(quotedMessage, quotingMessage1, quotingMessage2))
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage1.id)
            channelState.addQuotedMessage(quotedMessage.id, quotingMessage2.id)
            // when
            channelState.deleteQuotedMessageReferences(quotedMessage.id)
            // then
            val messages = channelState.messages.value
            assertNull(messages.find { it.id == quotingMessage1.id }?.replyTo)
            assertNull(messages.find { it.id == quotingMessage2.id }?.replyTo)
        }
    }
}
