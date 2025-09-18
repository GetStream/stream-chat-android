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
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
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
        channelState.watcherCount.value shouldBeEqualTo watchers.size
        channelState.watchers.value.size shouldBeEqualTo watchers.size
        channelState.watchers.value shouldBeEqualTo watchers
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
        channelState.watcherCount.value shouldBeEqualTo newWatcherCount
        channelState.watchers.value.size shouldBeEqualTo newWatcherCount
        channelState.watchers.value shouldBeEqualTo listOf(currentUser)
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
        channelStateWithoutLimit.loadingOlderMessages.value shouldBeEqualTo true
        // messageLimit should remain null - we can verify this by checking that all messages are retained
        // even after setting a large number of messages
        val manyMessages = createMessages(200)
        channelStateWithoutLimit.setMessages(manyMessages)
        channelStateWithoutLimit.messages.value.size shouldBeEqualTo 200
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
            channelStateWithLimit.loadingOlderMessages.value shouldBeEqualTo false
            // messageLimit should remain unchanged (neutral multiplier = 1.0)
            // We can verify this by checking that the limit is still effectively 100
            val messagesAtLimit = createMessages(150) // 150 messages
            channelStateWithLimit.upsertMessages(messagesAtLimit)
            // Should trim to baseLimit (100) + TRIM_BUFFER (30) = 130, then take last 100
            channelStateWithLimit.messages.value.size shouldBeEqualTo baseLimit
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
            channelStateWithLimit.loadingOlderMessages.value shouldBeEqualTo false
            // messageLimit should be increased by LIMIT_MULTIPLIER (1.5)
            // We can verify this by checking that more messages are retained
            val manyMessages = createMessages(200) // More than original limit but within new limit
            channelStateWithLimit.upsertMessages(manyMessages)
            // New limit should be 100 * 1.5 = 150
            channelStateWithLimit.messages.value.size shouldBeEqualTo 150
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
        channelStateWithLimit.loadingOlderMessages.value shouldBeEqualTo false
        // messageLimit should not be changed even though message count >= limit + buffer
        val manyMessages = createMessages(200)
        channelStateWithLimit.upsertMessages(manyMessages)
        // Should still use original limit of 100
        channelStateWithLimit.messages.value.size shouldBeEqualTo baseLimit
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
        channelStateWithoutLimit.messages.value.size shouldBeEqualTo 200
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
        channelStateWithLimit.messages.value.size shouldBeEqualTo 100
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
            channelStateWithLimit.messages.value.size shouldBeEqualTo 120
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
        channelStateWithLimit.messages.value.size shouldBeEqualTo baseLimit
        channelStateWithLimit.endOfOlderMessages.value shouldBeEqualTo false

        // Verify that the most recent messages are kept
        val sortedOriginalMessages = messages.sortedBy { it.createdAt }
        val expectedKeptMessages = sortedOriginalMessages.takeLast(baseLimit)
        val actualMessages = channelStateWithLimit.messages.value.sortedBy { it.createdAt }

        actualMessages.map { it.id } shouldBeEqualTo expectedKeptMessages.map { it.id }
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
        channelStateWithLimit.messages.value.size shouldBeEqualTo baseLimit
        // Should keep the last 30 messages (71-100)
        val keptMessages = channelStateWithLimit.messages.value.sortedBy { it.createdAt }
        keptMessages.first().text shouldBeEqualTo "Test message 71"
        keptMessages.last().text shouldBeEqualTo "Test message 100"
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
        channelState.messages.value.size shouldBeEqualTo 3
        val remainingMessageIds = channelState.messages.value.map { it.id }.toSet()
        messagesToDelete.forEach { messageToDelete ->
            remainingMessageIds.contains(messageToDelete.id) shouldBeEqualTo false
        }
        // Verify remaining messages are still there
        messages.drop(2).forEach { remainingMessage ->
            remainingMessageIds.contains(remainingMessage.id) shouldBeEqualTo true
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
        channelState.messages.value.size shouldBeEqualTo initialMessageCount
        channelState.messages.value.map { it.id }.toSet() shouldBeEqualTo messages.map { it.id }.toSet()
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
        channelState.messages.value.size shouldBeEqualTo 3
        channelState.messages.value.map { it.id }.toSet() shouldBeEqualTo existingMessages.map { it.id }.toSet()
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
        channelState.messages.value.size shouldBeEqualTo 3
        val remainingMessageIds = channelState.messages.value.map { it.id }.toSet()
        remainingMessageIds.contains(messageToDelete.id) shouldBeEqualTo false
        remainingMessageIds.contains(nonExistentMessage.id) shouldBeEqualTo false
        // Other existing messages should remain
        existingMessages.filterNot { it.id == messageToDelete.id }.forEach { message ->
            remainingMessageIds.contains(message.id) shouldBeEqualTo true
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
        user1Messages.size shouldBeEqualTo 2
        user1Messages.map { it.id }.toSet() shouldBeEqualTo setOf("msg1", "msg3")
        user1Messages.forEach { message ->
            message.user.id shouldBeEqualTo "user1"
        }

        user2Messages.size shouldBeEqualTo 1
        user2Messages.first().id shouldBeEqualTo "msg2"
        user2Messages.first().user.id shouldBeEqualTo "user2"
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
        messagesFromNonExistentUser shouldBeEqualTo emptyList()
    }

    @Test
    fun `getMessagesFromUser should return empty list when no messages exist`() = runTest {
        // given
        channelState.setMessages(emptyList())

        // when
        val messages = channelState.getMessagesFromUser("any_user")

        // then
        messages shouldBeEqualTo emptyList()
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
        messages.size shouldBeEqualTo 1
        messages.first().id shouldBeEqualTo "msg1"
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
        userMessages.size shouldBeEqualTo 3
        // The order should match the original order in the map (which may not be chronological)
        userMessages.map { it.id } shouldBeEqualTo listOf("msg1", "msg2", "msg3")
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
