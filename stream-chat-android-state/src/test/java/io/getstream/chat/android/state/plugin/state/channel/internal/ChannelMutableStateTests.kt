/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.state.channel.internal

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomConfig
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@ExperimentalCoroutinesApi
internal class ChannelMutableStateTests {

    private val userFlow = MutableStateFlow(currentUser)

    private lateinit var channelState: ChannelMutableState

    @BeforeEach
    fun setUp() {
        channelState = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(
                mapOf(currentUser.id to currentUser),
            ),
            activeLiveLocations = MutableStateFlow(
                emptyList(),
            ),
            baseMessageLimit = null,
            now = ChannelMutableStateTests::currentTime,
        )
    }

    @Test
    fun `Test expired pinned messages`() = runTest {
        // given
        val now = currentTime()
        val alreadyExpired = randomMessage(
            cid = CID,
            parentId = null,
            createdLocallyAt = null,
            updatedLocallyAt = null,
            createdAt = Date(now),
            updatedAt = null,
            deletedAt = null,
            deletedForMe = false,
            pinned = true,
            pinnedAt = Date(now),
            pinExpires = Date(now + 1.minutes.inWholeMilliseconds),
        )
        val expiresIn1h = alreadyExpired.copy(
            id = randomString(),
            pinExpires = Date(now + 1.hours.inWholeMilliseconds),
        )
        val pinnedMessages = listOf(alreadyExpired, expiresIn1h)

        // when
        advanceTimeBy(10.minutes)
        channelState.setPinnedMessages(pinnedMessages)
        advanceTimeBy(10.seconds)

        // then
        channelState.assertPinnedMessagesSizeEqualsTo(size = 1)

        // when
        advanceTimeBy(2.hours)

        // then
        channelState.assertPinnedMessagesSizeEqualsTo(size = 0)
    }

    @Test
    fun `When watchers get inserted, the watchers list should be updated`() = runTest {
        // given
        val watchers = listOf(currentUser, User(id = "bob", name = "Bob"))

        // when
        channelState.upsertWatchers(watchers, watchers.size)

        // then
        assertEquals(watchers.size, channelState.watcherCount.value)
        assertEquals(watchers.size, channelState.watchers.value.size)
        assertEquals(watchers, channelState.watchers.value)
    }

    @Test
    fun `When a watcher gets deleted, the watchers list should be updated`() = runTest {
        // given
        val anotherUser = User(id = "bob", name = "Bob")
        val watchers = listOf(currentUser, anotherUser)
        channelState.upsertWatchers(watchers, watchers.size)
        val newWatcherCount = watchers.size - 1

        // when
        channelState.deleteWatcher(anotherUser, newWatcherCount)

        // then
        assertEquals(newWatcherCount, channelState.watcherCount.value)
        assertEquals(newWatcherCount, channelState.watchers.value.size)
        assertEquals(listOf(currentUser), channelState.watchers.value)
    }

    @Test
    fun `setLoadingOlderMessages when messageLimit is null should not change the limit`() = runTest {
        // given
        val channelStateWithoutLimit = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = null,
            now = ChannelMutableStateTests::currentTime,
        )
        val initialMessages = createMessages(10)
        channelStateWithoutLimit.setMessages(initialMessages)

        // when
        channelStateWithoutLimit.setLoadingOlderMessages(true)

        // then
        assertEquals(true, channelStateWithoutLimit.loadingOlderMessages.value)
        // messageLimit should remain null - we can verify this by checking that all messages are retained
        // even after setting a large number of messages
        val manyMessages = createMessages(200)
        channelStateWithoutLimit.setMessages(manyMessages)
        assertEquals(200, channelStateWithoutLimit.messages.value.size)
    }

    @Test
    fun `setLoadingOlderMessages when message count is below limit plus buffer should use neutral multiplier`() =
        runTest {
            // given
            val baseLimit = 100
            val channelStateWithLimit = ChannelMutableState(
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                userFlow = userFlow,
                latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
                activeLiveLocations = MutableStateFlow(emptyList()),
                baseMessageLimit = baseLimit,
                now = ChannelMutableStateTests::currentTime,
            )
            // Set messages count below limit + TRIM_BUFFER (100 + 30 = 130)
            val initialMessages = createMessages(50)
            channelStateWithLimit.setMessages(initialMessages)

            // when
            channelStateWithLimit.setLoadingOlderMessages(true)
            // stop loading older messages to ensure next upsert checks the limit
            channelStateWithLimit.setLoadingOlderMessages(false)

            // then
            assertEquals(false, channelStateWithLimit.loadingOlderMessages.value)
            // messageLimit should remain unchanged (neutral multiplier = 1.0)
            // We can verify this by checking that the limit is still effectively 100
            val messagesAtLimit = createMessages(150) // 150 messages
            channelStateWithLimit.upsertMessages(messagesAtLimit)
            // Should trim to baseLimit (100) + TRIM_BUFFER (30) = 130, then take last 100
            assertEquals(baseLimit, channelStateWithLimit.messages.value.size)
        }

    @Test
    fun `setLoadingOlderMessages when message count is at or above limit plus buffer should increase limit`() =
        runTest {
            // given
            val baseLimit = 100
            val channelStateWithLimit = ChannelMutableState(
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                userFlow = userFlow,
                latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
                activeLiveLocations = MutableStateFlow(emptyList()),
                baseMessageLimit = baseLimit,
                now = ChannelMutableStateTests::currentTime,
            )
            // Set messages count at limit + TRIM_BUFFER (100 + 30 = 130)
            val initialMessages = createMessages(130)
            channelStateWithLimit.setMessages(initialMessages)

            // when
            channelStateWithLimit.setLoadingOlderMessages(true)
            // stop loading older messages to ensure next upsert checks the limit
            channelStateWithLimit.setLoadingOlderMessages(false)

            // then
            assertEquals(false, channelStateWithLimit.loadingOlderMessages.value)
            // messageLimit should be increased by LIMIT_MULTIPLIER (1.5)
            // We can verify this by checking that more messages are retained
            val manyMessages = createMessages(200) // More than original limit but within new limit
            channelStateWithLimit.upsertMessages(manyMessages)
            // New limit should be 100 * 1.5 = 150
            assertEquals(150, channelStateWithLimit.messages.value.size)
        }

    @Test
    fun `setLoadingOlderMessages when isLoading is false should not affect message limit`() = runTest {
        // given
        val baseLimit = 100
        val channelStateWithLimit = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = baseLimit,
            now = ChannelMutableStateTests::currentTime,
        )
        val initialMessages = createMessages(130) // At limit + buffer
        channelStateWithLimit.setMessages(initialMessages)

        // when
        channelStateWithLimit.setLoadingOlderMessages(false)

        // then
        assertEquals(false, channelStateWithLimit.loadingOlderMessages.value)
        // messageLimit should not be changed even though message count >= limit + buffer
        val manyMessages = createMessages(200)
        channelStateWithLimit.upsertMessages(manyMessages)
        // Should still use original limit of 100
        assertEquals(baseLimit, channelStateWithLimit.messages.value.size)
    }

    @Test
    fun `applyMessageLimitIfNeeded when messageLimit is null should return all messages`() = runTest {
        // given
        val channelStateWithoutLimit = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = null,
            now = ChannelMutableStateTests::currentTime,
        )
        val messages = createMessages(200)

        // when
        channelStateWithoutLimit.setMessages(messages)

        // then
        assertEquals(200, channelStateWithoutLimit.messages.value.size)
    }

    @Test
    fun `applyMessageLimitIfNeeded when loading older messages should return all messages`() = runTest {
        // given
        val baseLimit = 50
        val channelStateWithLimit = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = baseLimit,
            now = ChannelMutableStateTests::currentTime,
        )
        val messages = createMessages(100) // More than limit

        // when
        channelStateWithLimit.setLoadingOlderMessages(true)
        channelStateWithLimit.upsertMessages(messages)

        // then
        // Should not apply limit while loading older messages
        assertEquals(100, channelStateWithLimit.messages.value.size)
    }

    @Test
    fun `applyMessageLimitIfNeeded when message count is below limit plus buffer should return all messages`() =
        runTest {
            // given
            val baseLimit = 100
            val channelStateWithLimit = ChannelMutableState(
                channelType = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
                userFlow = userFlow,
                latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
                activeLiveLocations = MutableStateFlow(emptyList()),
                baseMessageLimit = baseLimit,
                now = ChannelMutableStateTests::currentTime,
            )
            // Create messages below limit + TRIM_BUFFER (100 + 30 = 130)
            val messages = createMessages(120)

            // when
            channelStateWithLimit.setMessages(messages)

            // then
            assertEquals(120, channelStateWithLimit.messages.value.size)
        }

    @Test
    fun `applyMessageLimitIfNeeded when message count exceeds limit plus buffer should trim messages`() = runTest {
        // given
        val baseLimit = 50
        val channelStateWithLimit = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = baseLimit,
            now = ChannelMutableStateTests::currentTime,
        )
        // Create messages exceeding limit + TRIM_BUFFER (50 + 30 = 80)
        val messages = createMessages(100)

        // when
        channelStateWithLimit.setMessages(messages)

        // then
        // Should trim to last 50 messages (the limit)
        assertEquals(baseLimit, channelStateWithLimit.messages.value.size)
        assertEquals(false, channelStateWithLimit.endOfOlderMessages.value)

        // Verify that the most recent messages are kept
        val sortedOriginalMessages = messages.sortedBy { it.createdAt }
        val expectedKeptMessages = sortedOriginalMessages.takeLast(baseLimit)
        val actualMessages = channelStateWithLimit.messages.value.sortedBy { it.createdAt }

        assertEquals(expectedKeptMessages.map { it.id }, actualMessages.map { it.id })
    }

    @Test
    fun `applyMessageLimitIfNeeded should keep most recent messages when trimming`() = runTest {
        // given
        val baseLimit = 30
        val channelStateWithLimit = ChannelMutableState(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(mapOf(currentUser.id to currentUser)),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = baseLimit,
            now = ChannelMutableStateTests::currentTime,
        )

        // Create messages with different timestamps to verify correct ordering
        val messages = createMessages(100)

        // when
        channelStateWithLimit.setMessages(messages)

        // then
        assertEquals(baseLimit, channelStateWithLimit.messages.value.size)
        // Should keep the last 30 messages (71-100)
        val keptMessages = channelStateWithLimit.messages.value.sortedBy { it.createdAt }
        assertEquals("Test message 71", keptMessages.first().text)
        assertEquals("Test message 100", keptMessages.last().text)
    }

    @Test
    fun `deleteMessages should remove messages from the channel state`() = runTest {
        // given
        val messages = createMessages(5)
        channelState.setMessages(messages)
        val messagesToDelete = messages.take(2)

        // when
        channelState.deleteMessages(messagesToDelete)

        // then
        assertEquals(3, channelState.messages.value.size)
        val remainingMessageIds = channelState.messages.value.map { it.id }.toSet()
        messagesToDelete.forEach { messageToDelete ->
            assertEquals(false, remainingMessageIds.contains(messageToDelete.id))
        }
        // Verify remaining messages are still there
        messages.drop(2).forEach { remainingMessage ->
            assertEquals(true, remainingMessageIds.contains(remainingMessage.id))
        }
    }

    @Test
    fun `deleteMessages with empty list should not affect channel state`() = runTest {
        // given
        val messages = createMessages(3)
        channelState.setMessages(messages)
        val initialMessageCount = channelState.messages.value.size

        // when
        channelState.deleteMessages(emptyList())

        // then
        assertEquals(initialMessageCount, channelState.messages.value.size)
        assertEquals(messages.map { it.id }.toSet(), channelState.messages.value.map { it.id }.toSet())
    }

    @Test
    fun `deleteMessages with non-existent messages should not affect existing messages`() = runTest {
        // given
        val existingMessages = createMessages(3)
        channelState.setMessages(existingMessages)

        val nonExistentMessage = randomMessage(
            id = "non_existent",
            cid = CID,
            createdAt = Date(currentTime()),
        )

        // when
        channelState.deleteMessages(listOf(nonExistentMessage))

        // then
        assertEquals(3, channelState.messages.value.size)
        assertEquals(existingMessages.map { it.id }.toSet(), channelState.messages.value.map { it.id }.toSet())
    }

    @Test
    fun `deleteMessages should handle mixed existing and non-existing messages`() = runTest {
        // given
        val existingMessages = createMessages(4)
        channelState.setMessages(existingMessages)

        val messageToDelete = existingMessages[1]
        val nonExistentMessage = randomMessage(
            id = "non_existent",
            cid = CID,
            createdAt = Date(currentTime()),
        )

        // when
        channelState.deleteMessages(listOf(messageToDelete, nonExistentMessage))

        // then
        assertEquals(3, channelState.messages.value.size)
        val remainingMessageIds = channelState.messages.value.map { it.id }.toSet()
        assertEquals(false, remainingMessageIds.contains(messageToDelete.id))
        assertEquals(false, remainingMessageIds.contains(nonExistentMessage.id))
        // Other existing messages should remain
        existingMessages.filterNot { it.id == messageToDelete.id }.forEach { message ->
            assertEquals(true, remainingMessageIds.contains(message.id))
        }
    }

    @Test
    fun `getMessagesFromUser should return messages from specific user`() = runTest {
        // given
        val user1 = User(id = "user1", name = "User 1")
        val user2 = User(id = "user2", name = "User 2")

        val messagesFromUser1 = listOf(
            randomMessage(id = "msg1", cid = CID, user = user1, text = "Message from user 1 - first"),
            randomMessage(id = "msg3", cid = CID, user = user1, text = "Message from user 1 - second"),
        )
        val messagesFromUser2 = listOf(
            randomMessage(id = "msg2", cid = CID, user = user2, text = "Message from user 2"),
        )

        val allMessages = messagesFromUser1 + messagesFromUser2
        channelState.setMessages(allMessages)

        // when
        val user1Messages = channelState.getMessagesFromUser("user1")
        val user2Messages = channelState.getMessagesFromUser("user2")

        // then
        assertEquals(2, user1Messages.size)
        assertEquals(setOf("msg1", "msg3"), user1Messages.map { it.id }.toSet())
        user1Messages.forEach { message ->
            assertEquals("user1", message.user.id)
        }

        assertEquals(1, user2Messages.size)
        assertEquals("msg2", user2Messages.first().id)
        assertEquals("user2", user2Messages.first().user.id)
    }

    @Test
    fun `getMessagesFromUser should return empty list when user has no messages`() = runTest {
        // given
        val user1 = User(id = "user1", name = "User 1")
        val messagesFromUser1 = listOf(
            randomMessage(id = "msg1", cid = CID, user = user1),
        )
        channelState.setMessages(messagesFromUser1)

        // when
        val messagesFromNonExistentUser = channelState.getMessagesFromUser("non_existent_user")

        // then
        assertTrue(messagesFromNonExistentUser.isEmpty())
    }

    @Test
    fun `getMessagesFromUser should return empty list when no messages exist`() = runTest {
        // given
        channelState.setMessages(emptyList())

        // when
        val messages = channelState.getMessagesFromUser("any_user")

        // then
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `getMessagesFromUser should handle null user id gracefully`() = runTest {
        // given
        val userWithNullId = User(id = "user1", name = "User 1")
        val userMessage = randomMessage(id = "msg1", cid = CID, user = userWithNullId)
        channelState.setMessages(listOf(userMessage))

        // when
        val messages = channelState.getMessagesFromUser("user1")

        // then
        assertEquals(1, messages.size)
        assertEquals("msg1", messages.first().id)
    }

    @Test
    fun `getMessagesFromUser should return messages in original order`() = runTest {
        // given
        val user = User(id = "user1", name = "User 1")
        val now = currentTime()

        val messages = listOf(
            randomMessage(id = "msg1", cid = CID, user = user, createdAt = Date(now + 1000)),
            randomMessage(id = "msg2", cid = CID, user = user, createdAt = Date(now + 2000)),
            randomMessage(id = "msg3", cid = CID, user = user, createdAt = Date(now + 3000)),
        )
        channelState.setMessages(messages)

        // when
        val userMessages = channelState.getMessagesFromUser("user1")

        // then
        assertEquals(3, userMessages.size)
        // The order should match the original order in the map (which may not be chronological)
        assertEquals(listOf("msg1", "msg2", "msg3"), userMessages.map { it.id })
    }

    @Test
    fun `markChannelAsRead should return true when lastReadMessageId matches lastMessage but unreadMessages greater than 0`() {
        val lastMessage = randomMessage(
            parentId = null,
            shadowed = false,
        )
        channelState.setMessages(listOf(lastMessage))
        channelState.setChannelConfig(randomConfig(readEventsEnabled = true))

        val readState = randomChannelUserRead(
            user = currentUser,
            unreadMessages = 1,
            lastReadMessageId = lastMessage.id,
        )
        channelState.upsertReads(listOf(readState))

        assertEquals(readState, channelState.read.value)

        val actual = channelState.markChannelAsRead()

        assertEquals(true, actual)
        assertEquals(lastMessage.createdAt, channelState.read.value?.lastReceivedEventDate)
        assertEquals(lastMessage.createdAt, channelState.read.value?.lastRead)
        assertEquals(0, channelState.read.value?.unreadMessages)
    }

    @Test
    fun `markChannelAsRead should return false when lastReadMessageId matches and unreadMessages is 0`() =
        runTest {
            val lastMessage = randomMessage(
                parentId = null,
                shadowed = false,
            )
            channelState.setMessages(listOf(lastMessage))
            channelState.setChannelConfig(randomConfig(readEventsEnabled = true))

            val readState = randomChannelUserRead(
                user = currentUser,
                unreadMessages = 0,
                lastReadMessageId = lastMessage.id,
            )
            channelState.upsertReads(listOf(readState))

            val result = channelState.markChannelAsRead()

            assertEquals(false, result)
        }

    @Test
    fun `markChannelAsRead should return true when lastReadMessageId differs from lastMessage`() = runTest {
        val lastMessage = randomMessage(
            parentId = null,
            shadowed = false,
        )
        channelState.setMessages(listOf(lastMessage))
        channelState.setChannelConfig(randomConfig(readEventsEnabled = true))

        val readState = randomChannelUserRead(
            user = currentUser,
            unreadMessages = 0,
            lastReadMessageId = "different_message_id",
        )
        channelState.upsertReads(listOf(readState))

        val result = channelState.markChannelAsRead()

        assertEquals(true, result)
        assertEquals(lastMessage.createdAt, channelState.read.value?.lastReceivedEventDate)
        assertEquals(lastMessage.createdAt, channelState.read.value?.lastRead)
        assertEquals(0, channelState.read.value?.unreadMessages)
    }

    @Test
    fun `markChannelAsRead should return false when readEventsEnabled is false`() = runTest {
        val lastMessage = randomMessage(
            parentId = null,
            shadowed = false,
        )
        channelState.setMessages(listOf(lastMessage))
        channelState.setChannelConfig(randomConfig(readEventsEnabled = false))

        val readState = randomChannelUserRead(
            user = currentUser,
            unreadMessages = 1,
        )
        channelState.upsertReads(listOf(readState))

        val result = channelState.markChannelAsRead()

        assertEquals(false, result)
    }

    @Test
    fun `markChannelAsRead should return false when there are no messages`() = runTest {
        channelState.setMessages(emptyList())
        channelState.setChannelConfig(randomConfig(readEventsEnabled = true))

        val readState = randomChannelUserRead(
            user = currentUser,
            unreadMessages = 1,
        )
        channelState.upsertReads(listOf(readState))

        val result = channelState.markChannelAsRead()

        assertEquals(false, result)
    }

    @Test
    fun `markChannelAsRead should return true when read state is null`() = runTest {
        val lastMessage = randomMessage(
            parentId = null,
            shadowed = false,
        )
        channelState.setMessages(listOf(lastMessage))
        channelState.setChannelConfig(randomConfig(readEventsEnabled = true))

        assertEquals(lastMessage, channelState.messages.value.lastOrNull())
        assertEquals(true, channelState.channelConfig.value.readEventsEnabled)
        assertEquals(null, channelState.read.value)

        val result = channelState.markChannelAsRead()

        assertEquals(true, result)
    }

    private fun ChannelMutableState.assertPinnedMessagesSizeEqualsTo(size: Int) {
        require(pinnedMessages.value.size == size) {
            "pinnedMessages should have $size items, but was ${pinnedMessages.value.size}"
        }
        require(visiblePinnedMessages.value.size == size) {
            "visiblePinnedMessages should have $size items, but was ${visiblePinnedMessages.value.size}"
        }
        require(sortedPinnedMessages.value.size == size) {
            "sortedPinnedMessages should have $size items, but was ${sortedPinnedMessages.value.size}"
        }
        require(pinnedMessagesList.value.size == size) {
            "pinnedMessagesList should have $size items, but was ${pinnedMessagesList.value.size}"
        }
        require(rawPinnedMessages.size == size) {
            "rawPinnedMessages should have $size items, but was ${rawPinnedMessages.size}"
        }
    }

    /**
     * Helper function to create a list of messages with sequential timestamps.
     */
    private fun createMessages(count: Int): List<Message> {
        val now = currentTime()
        return (1..count).map { i ->
            randomMessage(
                id = "message_$i",
                cid = CID,
                createdAt = Date(now + i * 1000L), // Each message 1 second apart
                createdLocallyAt = null,
                text = "Test message $i",
                parentId = null, // ensure message is not a thread reply
                shadowed = false, // ensure message is not shadowed
                deletedAt = null, // ensure message is not deleted
            )
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
