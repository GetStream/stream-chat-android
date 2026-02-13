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

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplMessagesTest {

    private val userFlow = MutableStateFlow(currentUser)
    private lateinit var channelState: ChannelStateImpl

    @BeforeEach
    fun setUp() {
        channelState = ChannelStateImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            currentUser = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            mutedUsers = MutableStateFlow(emptyList()),
            liveLocations = MutableStateFlow(emptyList()),
            messageLimit = null,
        )
    }

    @Nested
    inner class SetMessages {

        @Test
        fun `setMessages should replace existing messages`() = runTest {
            // given
            val initialMessages = createMessages(3)
            channelState.setMessages(initialMessages)
            // when
            val newMessages = createMessages(5, startIndex = 10)
            channelState.setMessages(newMessages)
            // then
            assertEquals(5, channelState.messages.value.size)
            assertEquals(newMessages.map { it.id }.toSet(), channelState.messages.value.map { it.id }.toSet())
        }

        @Test
        fun `setMessages should filter out thread replies not shown in channel`() = runTest {
            // given
            val regularMessage = createMessage(1)
            val threadReplyShownInChannel = createMessage(2, parentId = "parent1", showInChannel = true)
            val threadReplyNotShownInChannel = createMessage(3, parentId = "parent1", showInChannel = false)
            // when
            channelState.setMessages(listOf(regularMessage, threadReplyShownInChannel, threadReplyNotShownInChannel))
            // then
            assertEquals(2, channelState.messages.value.size)
            assertTrue(channelState.messages.value.any { it.id == regularMessage.id })
            assertTrue(channelState.messages.value.any { it.id == threadReplyShownInChannel.id })
            assertFalse(channelState.messages.value.any { it.id == threadReplyNotShownInChannel.id })
        }

        @Test
        fun `setMessages should filter out shadowed messages from other users`() = runTest {
            // given
            val regularMessage = createMessage(1)
            val shadowedMessageFromOtherUser = createMessage(2, user = randomUser(), shadowed = true)
            val shadowedMessageFromCurrentUser = createMessage(3, user = currentUser, shadowed = true)
            // when
            channelState.setMessages(listOf(regularMessage, shadowedMessageFromOtherUser, shadowedMessageFromCurrentUser))

            // then
            assertEquals(2, channelState.messages.value.size)
            assertTrue(channelState.messages.value.any { it.id == regularMessage.id })
            assertFalse(channelState.messages.value.any { it.id == shadowedMessageFromOtherUser.id })
            assertTrue(channelState.messages.value.any { it.id == shadowedMessageFromCurrentUser.id })
        }

        @Test
        fun `setMessages with empty list should clear messages`() = runTest {
            // given
            val initialMessages = createMessages(3)
            channelState.setMessages(initialMessages)
            // when
            channelState.setMessages(emptyList())
            // then
            assertTrue(channelState.messages.value.isEmpty())
        }
    }

    @Nested
    inner class UpsertMessage {

        @Test
        fun `upsertMessage should add new message to empty list`() = runTest {
            // given
            val message = createMessage(1)
            // when
            channelState.upsertMessage(message)
            // then
            assertEquals(1, channelState.messages.value.size)
            assertEquals(message.id, channelState.messages.value.first().id)
        }

        @Test
        fun `upsertMessage should add new message in sorted order`() = runTest {
            // given
            val message1 = createMessage(1, timestamp = 1000)
            val message3 = createMessage(3, timestamp = 3000)
            channelState.setMessages(listOf(message1, message3))
            // when
            val message2 = createMessage(2, timestamp = 2000)
            channelState.upsertMessage(message2)
            // then
            assertEquals(3, channelState.messages.value.size)
            val sortedIds = channelState.messages.value.map { it.id }
            assertEquals(listOf("message_1", "message_2", "message_3"), sortedIds)
        }

        @Test
        fun `upsertMessage should update existing message`() = runTest {
            // given
            val message = createMessage(1, text = "Original text")
            channelState.setMessages(listOf(message))
            // when
            val updatedMessage = message.copy(text = "Updated text")
            channelState.upsertMessage(updatedMessage)
            // then
            assertEquals(1, channelState.messages.value.size)
            assertEquals("Updated text", channelState.messages.value.first().text)
        }

        @Test
        fun `upsertMessage should ignore thread reply not shown in channel`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            // when
            val threadReply = createMessage(2, parentId = "parent1", showInChannel = false)
            channelState.upsertMessage(threadReply)
            // then
            assertEquals(1, channelState.messages.value.size)
            assertEquals(message.id, channelState.messages.value.first().id)
        }

        @Test
        fun `upsertMessage should ignore shadowed message from other user`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            // when
            val shadowedMessage = createMessage(2, user = randomUser(), shadowed = true)
            channelState.upsertMessage(shadowedMessage)
            // then
            assertEquals(1, channelState.messages.value.size)
        }
    }

    @Nested
    inner class UpsertMessages {

        @Test
        fun `upsertMessages should add multiple messages in sorted order`() = runTest {
            // given
            val message1 = createMessage(1, timestamp = 1000)
            val message5 = createMessage(5, timestamp = 5000)
            channelState.setMessages(listOf(message1, message5))
            // when
            val newMessages = listOf(
                createMessage(2, timestamp = 2000),
                createMessage(3, timestamp = 3000),
                createMessage(4, timestamp = 4000),
            )
            channelState.upsertMessages(newMessages)
            // then
            assertEquals(5, channelState.messages.value.size)
            val sortedIds = channelState.messages.value.map { it.id }
            assertEquals(listOf("message_1", "message_2", "message_3", "message_4", "message_5"), sortedIds)
        }

        @Test
        fun `upsertMessages should update existing messages`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            // when
            val updatedMessages = messages.map { it.copy(text = "Updated: ${it.text}") }
            channelState.upsertMessages(updatedMessages)
            // then
            assertEquals(3, channelState.messages.value.size)
            channelState.messages.value.forEach { message ->
                assertTrue(message.text.startsWith("Updated:"))
            }
        }

        @Test
        fun `upsertMessages with empty list should not change state`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            // when
            channelState.upsertMessages(emptyList())
            // then
            assertEquals(3, channelState.messages.value.size)
        }
    }

    @Nested
    inner class UpdateMessage {

        @Test
        fun `updateMessage should update existing message`() = runTest {
            // given
            val message = createMessage(1, text = "Original")
            channelState.setMessages(listOf(message))
            // when
            val updatedMessage = message.copy(text = "Updated")
            channelState.updateMessage(updatedMessage)
            // then
            assertEquals("Updated", channelState.messages.value.first().text)
        }

        @Test
        fun `updateMessage should not add message if it does not exist`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            // when
            val nonExistingMessage = createMessage(999, text = "New message")
            channelState.updateMessage(nonExistingMessage)
            // then
            assertEquals(1, channelState.messages.value.size)
            assertFalse(channelState.messages.value.any { it.id == nonExistingMessage.id })
        }
    }

    @Nested
    inner class UpdateMessageById {

        @Test
        fun `updateMessageById should transform existing message`() = runTest {
            // given
            val message = createMessage(1, text = "Original")
            channelState.setMessages(listOf(message))
            // when
            channelState.updateMessageById(message.id) { it.copy(text = "Transformed") }
            // then
            assertEquals("Transformed", channelState.messages.value.first().text)
        }

        @Test
        fun `updateMessageById should do nothing for non-existing message`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            val initialMessages = channelState.messages.value
            // when
            channelState.updateMessageById("non_existing_id") { it.copy(text = "Changed") }
            // then
            assertEquals(initialMessages, channelState.messages.value)
        }

        @Test
        fun `updateMessageById should also update message in cached and pinned messages`() = runTest {
            // given
            val message = createMessage(1, text = "Original")
            channelState.setMessages(listOf(message))
            channelState.cacheLatestMessages()
            channelState.addPinnedMessage(message.copy(pinned = true, pinnedAt = Date()))
            // when
            channelState.updateMessageById(message.id) { it.copy(text = "Updated") }
            // then
            assertEquals("Updated", channelState.messages.value.first().text)
            assertEquals("Updated", channelState.pinnedMessages.value.first().text)
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals("Updated", cachedMessages.first().text)
        }
    }

    @Nested
    inner class DeleteMessage {

        @Test
        fun `deleteMessage should remove message from state`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            // when
            channelState.deleteMessage(messages[1].id)
            // then
            assertEquals(2, channelState.messages.value.size)
            assertFalse(channelState.messages.value.any { it.id == messages[1].id })
        }

        @Test
        fun `deleteMessage should do nothing for non-existing message`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            // when
            channelState.deleteMessage("non_existing_id")
            // then
            assertEquals(3, channelState.messages.value.size)
        }

        @Test
        fun `deleteMessage should also remove from cached and pinned messages`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            channelState.cacheLatestMessages()
            channelState.addPinnedMessage(message.copy(pinned = true, pinnedAt = Date()))
            // when
            channelState.deleteMessage(message.id)
            // then
            assertTrue(channelState.messages.value.isEmpty())
            assertTrue(channelState.pinnedMessages.value.isEmpty())
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertTrue(cachedMessages.isEmpty())
        }
    }

    @Nested
    inner class RemoveMessagesBefore {

        @Test
        fun `removeMessagesBefore should remove messages before given date`() = runTest {
            // given
            val message1 = createMessage(1, timestamp = 1000)
            val message2 = createMessage(2, timestamp = 2000)
            val message3 = createMessage(3, timestamp = 3000)
            channelState.setMessages(listOf(message1, message2, message3))
            // when
            channelState.removeMessagesBefore(Date(2500), systemMessage = null)
            // then
            assertEquals(1, channelState.messages.value.size)
            assertEquals("message_3", channelState.messages.value.first().id)
        }

        @Test
        fun `removeMessagesBefore should add system message after removal`() = runTest {
            // given
            val message1 = createMessage(1, timestamp = 1000)
            val message2 = createMessage(2, timestamp = 2000)
            channelState.setMessages(listOf(message1, message2))
            // when
            val systemMessage = createMessage(99, timestamp = 2500, text = "System message")
            channelState.removeMessagesBefore(Date(1500), systemMessage = systemMessage)
            // then
            assertEquals(2, channelState.messages.value.size)
            assertTrue(channelState.messages.value.any { it.id == "message_2" })
            assertTrue(channelState.messages.value.any { it.id == "message_99" })
        }

        @Test
        fun `removeMessagesBefore should not change state if all messages are after date`() = runTest {
            // given
            val messages = createMessages(3, startIndex = 1, baseTimestamp = 5000)
            channelState.setMessages(messages)
            // when
            channelState.removeMessagesBefore(Date(1000), systemMessage = null)
            // then
            assertEquals(3, channelState.messages.value.size)
        }

        @Test
        fun `removeMessagesBefore should also remove from cached messages`() = runTest {
            // given
            val message1 = createMessage(1, timestamp = 1000)
            val message2 = createMessage(2, timestamp = 2000)
            val message3 = createMessage(3, timestamp = 3000)
            channelState.setMessages(listOf(message1, message2, message3))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages to test cached
            // when
            channelState.removeMessagesBefore(Date(2500), systemMessage = null)
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals(1, cachedMessages.size)
            assertEquals("message_3", cachedMessages.first().id)
        }

        @Test
        fun `removeMessagesBefore should also remove from pinned messages`() = runTest {
            // given
            val pinnedMessage1 = createMessage(1, timestamp = 1000, pinned = true, pinnedAt = Date(1000))
            val pinnedMessage2 = createMessage(2, timestamp = 2000, pinned = true, pinnedAt = Date(2000))
            val pinnedMessage3 = createMessage(3, timestamp = 3000, pinned = true, pinnedAt = Date(3000))
            channelState.addPinnedMessage(pinnedMessage1)
            channelState.addPinnedMessage(pinnedMessage2)
            channelState.addPinnedMessage(pinnedMessage3)
            // when
            channelState.removeMessagesBefore(Date(2500), systemMessage = null)
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
            assertEquals("message_3", channelState.pinnedMessages.value.first().id)
        }
    }

    @Nested
    inner class DeleteMessagesFromUser {

        @Test
        fun `deleteMessagesFromUser with hard=true should remove all messages from user`() = runTest {
            // given
            val user1 = randomUser(id = "user1")
            val user2 = randomUser(id = "user2")
            val message1 = createMessage(1, user = user1)
            val message2 = createMessage(2, user = user2)
            val message3 = createMessage(3, user = user1)
            channelState.setMessages(listOf(message1, message2, message3))
            // when
            channelState.deleteMessagesFromUser("user1", hard = true, deletedAt = Date())
            // then
            assertEquals(1, channelState.messages.value.size)
            assertEquals("message_2", channelState.messages.value.first().id)
        }

        @Test
        fun `deleteMessagesFromUser with hard=false should soft delete messages`() = runTest {
            // given
            val user1 = randomUser(id = "user1")
            val message1 = createMessage(1, user = user1)
            val message2 = createMessage(2, user = user1)
            channelState.setMessages(listOf(message1, message2))
            val deletedAt = Date()
            // when
            channelState.deleteMessagesFromUser("user1", hard = false, deletedAt = deletedAt)
            // then
            assertEquals(2, channelState.messages.value.size)
            channelState.messages.value.forEach { message ->
                assertEquals(deletedAt, message.deletedAt)
            }
        }

        @Test
        fun `deleteMessagesFromUser should do nothing for user with no messages`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            // when
            channelState.deleteMessagesFromUser("non_existing_user", hard = true, deletedAt = Date())
            // then
            assertEquals(3, channelState.messages.value.size)
        }

        @Test
        fun `deleteMessagesFromUser with hard=true should also remove from cached messages`() = runTest {
            // given
            val user1 = randomUser(id = "user1")
            val user2 = randomUser(id = "user2")
            val message1 = createMessage(1, user = user1)
            val message2 = createMessage(2, user = user2)
            val message3 = createMessage(3, user = user1)
            channelState.setMessages(listOf(message1, message2, message3))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages to test cached
            // when
            channelState.deleteMessagesFromUser("user1", hard = true, deletedAt = Date())
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals(1, cachedMessages.size)
            assertEquals("message_2", cachedMessages.first().id)
        }

        @Test
        fun `deleteMessagesFromUser with hard=true should also remove from pinned messages`() = runTest {
            // given
            val user1 = randomUser(id = "user1")
            val user2 = randomUser(id = "user2")
            val pinnedMessage1 = createMessage(1, user = user1, pinned = true, pinnedAt = Date())
            val pinnedMessage2 = createMessage(2, user = user2, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(pinnedMessage1)
            channelState.addPinnedMessage(pinnedMessage2)
            // when
            channelState.deleteMessagesFromUser("user1", hard = true, deletedAt = Date())
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
            assertEquals("message_2", channelState.pinnedMessages.value.first().id)
        }

        @Test
        fun `deleteMessagesFromUser with hard=false should soft delete in cached messages`() = runTest {
            // given
            val user1 = randomUser(id = "user1")
            val message1 = createMessage(1, user = user1)
            val message2 = createMessage(2, user = user1)
            channelState.setMessages(listOf(message1, message2))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages to test cached
            val deletedAt = Date()
            // when
            channelState.deleteMessagesFromUser("user1", hard = false, deletedAt = deletedAt)
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals(2, cachedMessages.size)
            cachedMessages.forEach { message ->
                assertEquals(deletedAt, message.deletedAt)
            }
        }

        @Test
        fun `deleteMessagesFromUser with hard=false should soft delete in pinned messages`() = runTest {
            // given
            val user1 = randomUser(id = "user1")
            val pinnedMessage = createMessage(1, user = user1, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(pinnedMessage)
            val deletedAt = Date()
            // when
            channelState.deleteMessagesFromUser("user1", hard = false, deletedAt = deletedAt)
            // then
            assertEquals(1, channelState.pinnedMessages.value.size)
            assertEquals(deletedAt, channelState.pinnedMessages.value.first().deletedAt)
        }
    }

    @Nested
    inner class UpsertCachedMessage {

        @Test
        fun `upsertCachedMessage should add message to cached messages`() = runTest {
            // given
            val messages = createMessages(3)
            channelState.setMessages(messages)
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList()) // Clear main messages
            // when
            val newMessage = createMessage(99)
            channelState.upsertCachedMessage(newMessage)
            // then
            // Verify via toChannel() which exposes cachedLatestMessages
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals(4, cachedMessages.size)
            assertTrue(cachedMessages.any { it.id == newMessage.id })
        }

        @Test
        fun `upsertCachedMessage should ignore thread replies not shown in channel`() = runTest {
            // given
            channelState.cacheLatestMessages()
            // when
            val threadReply = createMessage(1, parentId = "parent", showInChannel = false)
            channelState.upsertCachedMessage(threadReply)
            // then
            assertNull(channelState.getMessageById(threadReply.id))
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertTrue(cachedMessages.isEmpty())
        }

        @Test
        fun `upsertCachedMessage should update existing cached message`() = runTest {
            // given
            val message = createMessage(1, text = "Original")
            channelState.setMessages(listOf(message))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList())
            // when
            val updatedMessage = message.copy(text = "Updated via cache")
            channelState.upsertCachedMessage(updatedMessage)
            // then
            val cachedMessages = channelState.toChannel().cachedLatestMessages
            assertEquals(1, cachedMessages.size)
            assertEquals("Updated via cache", cachedMessages.first().text)
        }

        @Test
        fun `upsertCachedMessage should be findable via getMessageById`() = runTest {
            // given
            channelState.setMessages(emptyList())
            // when
            val newMessage = createMessage(99)
            channelState.upsertCachedMessage(newMessage)
            // then
            assertNotNull(channelState.getMessageById(newMessage.id))
            assertEquals(newMessage.id, channelState.getMessageById(newMessage.id)?.id)
        }
    }

    @Nested
    inner class GetMessageById {

        @Test
        fun `getMessageById should return message from main messages`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            // when
            val result = channelState.getMessageById(message.id)
            // then
            assertEquals(message.id, result?.id)
        }

        @Test
        fun `getMessageById should return message from cached messages`() = runTest {
            // given
            val message = createMessage(1)
            channelState.setMessages(listOf(message))
            channelState.cacheLatestMessages()
            channelState.setMessages(emptyList())
            // when
            val result = channelState.getMessageById(message.id)
            // then
            assertEquals(message.id, result?.id)
        }

        @Test
        fun `getMessageById should return message from pinned messages`() = runTest {
            // given
            val message = createMessage(1, pinned = true, pinnedAt = Date())
            channelState.addPinnedMessage(message)
            // when
            val result = channelState.getMessageById(message.id)
            // then
            assertEquals(message.id, result?.id)
        }

        @Test
        fun `getMessageById should return null for non-existing message`() = runTest {
            // given
            channelState.setMessages(createMessages(3))
            // when
            val result = channelState.getMessageById("non_existing_id")
            // then
            assertNull(result)
        }
    }

    @Nested
    inner class HideMessagesBefore {

        @Test
        fun `hideMessagesBefore should remove messages before given date`() = runTest {
            // given
            val message1 = createMessage(1, timestamp = 1000)
            val message2 = createMessage(2, timestamp = 2000)
            channelState.setMessages(listOf(message1, message2))
            // when
            channelState.hideMessagesBefore(Date(1500))
            // then
            assertEquals(1, channelState.messages.value.size)
            assertEquals("message_2", channelState.messages.value.first().id)
        }
    }

    private fun createMessage(
        index: Int,
        timestamp: Long = currentTime() + index * 1000L,
        text: String = "Test message $index",
        user: User = currentUser,
        parentId: String? = null,
        showInChannel: Boolean = true,
        shadowed: Boolean = false,
        pinned: Boolean = false,
        pinnedAt: Date? = null,
    ): Message = randomMessage(
        id = "message_$index",
        cid = CID,
        createdAt = Date(timestamp),
        createdLocallyAt = null,
        text = text,
        user = user,
        parentId = parentId,
        showInChannel = showInChannel,
        shadowed = shadowed,
        pinned = pinned,
        pinnedAt = pinnedAt,
        deletedAt = null,
    )

    private fun createMessages(
        count: Int,
        startIndex: Int = 1,
        baseTimestamp: Long = currentTime(),
    ): List<Message> {
        return (startIndex until startIndex + count).map { i ->
            createMessage(i, timestamp = baseTimestamp + i * 1000L)
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "messaging:123"

        private val currentUser = User(id = "tom", name = "Tom")

        private fun currentTime() = testCoroutines.dispatcher.scheduler.currentTime
    }
}
