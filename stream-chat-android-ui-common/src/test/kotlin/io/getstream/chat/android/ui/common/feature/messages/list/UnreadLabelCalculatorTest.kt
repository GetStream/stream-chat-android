/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.common.feature.messages.list

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import java.util.Date

internal class UnreadLabelCalculatorTest {

    private val calculator = UnreadLabelCalculator()
    private val currentUser = randomUser(id = "current-user")
    private val otherUser = randomUser(id = "other-user")

    /**
     * Test Case 1: Standard Unread Messages
     * When messages from other users arrive after the last read position.
     */
    @Test
    fun `should calculate unread label for standard unread messages from other users`() {
        // Given: Messages where the last read message is followed by unread messages from other users
        val lastReadMessage = createMessage(id = "msg-3", user = currentUser, createdAt = Date(1000))
        val unreadMessage1 = createMessage(id = "msg-4", user = otherUser, createdAt = Date(2000))
        val unreadMessage2 = createMessage(id = "msg-5", user = otherUser, createdAt = Date(3000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            createMessage(id = "msg-2", user = otherUser, createdAt = Date(200)),
            lastReadMessage,
            unreadMessage1,
            unreadMessage2,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-3",
            unreadMessages = 2,
            lastRead = Date(1000),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should return unread label with correct data
        result shouldBeEqualTo MessageListController.UnreadLabel(
            unreadCount = 2,
            lastReadMessageId = "msg-3",
            buttonVisibility = true,
        )
    }

    /**
     * Test Case 2: Own Message Marked as Unread
     * When a user explicitly marks their own message as unread.
     */
    @Test
    fun `should calculate unread label when own message is marked as unread`() {
        // Given: User's own message is marked as unread
        val lastReadMessage = createMessage(id = "msg-2", user = otherUser, createdAt = Date(1000))
        val ownMessageMarkedUnread = createMessage(id = "msg-3", user = currentUser, createdAt = Date(3000))
        val followUpOwnMessage = createMessage(id = "msg-4", user = currentUser, createdAt = Date(4000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            ownMessageMarkedUnread,
            followUpOwnMessage,
        )

        // Key: lastRead timestamp equals the lastReadMessage createdAt, but the unread message was created after
        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 2,
            lastRead = Date(1000), // Same as lastReadMessage.createdAt
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should return unread label (own message marked as unread is treated like standard unread)
        result shouldBeEqualTo MessageListController.UnreadLabel(
            unreadCount = 2,
            lastReadMessageId = "msg-2",
            buttonVisibility = true,
        )
    }

    /**
     * Test Case 3: Multiple Own Messages Before Other User's Messages
     * When the user sends multiple messages, then receives messages from others.
     * The unread indicator should appear before the other users' messages, not before the user's own messages.
     */
    @Test
    fun `should calculate unread label with multiple own messages before other users messages`() {
        // Given: User sends messages, then receives messages from others
        val lastReadMessage = createMessage(id = "msg-2", user = otherUser, createdAt = Date(1000))
        val ownMessage1 = createMessage(id = "msg-3", user = currentUser, createdAt = Date(2000))
        val ownMessage2 = createMessage(id = "msg-4", user = currentUser, createdAt = Date(3000))
        val otherMessage1 = createMessage(id = "msg-5", user = otherUser, createdAt = Date(4000))
        val otherMessage2 = createMessage(id = "msg-6", user = otherUser, createdAt = Date(5000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            ownMessage1,
            ownMessage2,
            otherMessage1,
            otherMessage2,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 4,
            lastRead = Date(1500), // Between lastReadMessage and first own message
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should use the last own message before other users' messages as the anchor
        result shouldBeEqualTo MessageListController.UnreadLabel(
            unreadCount = 4,
            lastReadMessageId = "msg-4", // Last own message before others
            buttonVisibility = true,
        )
    }

    /**
     * Test Case 4: Button Visibility with Deleted Messages
     * Button should be hidden if all unread messages are deleted.
     */
    @Test
    fun `should hide button when all unread messages are deleted`() {
        // Given: Unread messages are all deleted
        val lastReadMessage = createMessage(id = "msg-2", user = currentUser, createdAt = Date(1000))
        val deletedMessage1 = createMessage(
            id = "msg-3",
            user = otherUser,
            createdAt = Date(2000),
            deletedAt = Date(2500),
        )
        val deletedMessage2 = createMessage(
            id = "msg-4",
            user = otherUser,
            createdAt = Date(3000),
            deletedAt = Date(3500),
        )

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            deletedMessage1,
            deletedMessage2,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 2,
            lastRead = Date(1000),
        )

        // When: Calculate unread label with shouldShowButton = true
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Button should be hidden because all unread messages are deleted
        result?.buttonVisibility shouldBe false
    }

    /**
     * Test Case 5: Button Visibility with Some Deleted Messages
     * Button should be visible if at least one unread message is not deleted.
     */
    @Test
    fun `should show button when some unread messages are not deleted`() {
        // Given: Mix of deleted and non-deleted unread messages
        val lastReadMessage = createMessage(id = "msg-2", user = currentUser, createdAt = Date(1000))
        val deletedMessage = createMessage(
            id = "msg-3",
            user = otherUser,
            createdAt = Date(2000),
            deletedAt = Date(2500),
        )
        val activeMessage = createMessage(id = "msg-4", user = otherUser, createdAt = Date(3000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            deletedMessage,
            activeMessage,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 2,
            lastRead = Date(1000),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Button should be visible
        result?.buttonVisibility shouldBe true
    }

    /**
     * Test Case 6: shouldShowButton Parameter
     * Button visibility should respect the shouldShowButton parameter.
     */
    @Test
    fun `should hide button when shouldShowButton is false even with unread messages`() {
        // Given: Unread messages exist
        val lastReadMessage = createMessage(id = "msg-2", user = currentUser, createdAt = Date(1000))
        val unreadMessage = createMessage(id = "msg-3", user = otherUser, createdAt = Date(2000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            unreadMessage,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 1,
            lastRead = Date(1000),
        )

        // When: Calculate unread label with shouldShowButton = false
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = false,
        )

        // Then: Button should be hidden
        result?.buttonVisibility shouldBe false
    }

    /**
     * Test Case 7: No Unread Messages
     * Should return null when there are no unread messages.
     */
    @Test
    fun `should return null when there are no unread messages`() {
        // Given: All messages are read (no messages after lastReadMessage)
        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            createMessage(id = "msg-2", user = otherUser, createdAt = Date(200)),
            createMessage(id = "msg-3", user = currentUser, createdAt = Date(300)),
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-3",
            unreadMessages = 0,
            lastRead = Date(300),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should return null (no unread label needed)
        result shouldBe null
    }

    /**
     * Test Case 8: Last Message is Last Read Message
     * Should return null when the last message in the list is the last read message.
     */
    @Test
    fun `should return null when last message is the last read message`() {
        // Given: Last message in the list is the last read message
        val lastMessage = createMessage(id = "msg-3", user = otherUser, createdAt = Date(300))
        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            createMessage(id = "msg-2", user = otherUser, createdAt = Date(200)),
            lastMessage,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-3",
            unreadMessages = 0,
            lastRead = Date(300),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should return null
        result shouldBe null
    }

    /**
     * Test Case 9: Empty Messages List
     * Should return null when the messages list is empty.
     */
    @Test
    fun `should return null when messages list is empty`() {
        // Given: Empty messages list
        val messages = emptyList<Message>()
        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-1",
            unreadMessages = 0,
            lastRead = Date(1000),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should return null
        result shouldBe null
    }

    /**
     * Test Case 10: All Unread Messages Are From Current User (No Other Users)
     * Should return null when unread messages are only from the current user and not marked as unread.
     */
    @Test
    fun `should return null when all unread messages are from current user and unreadCount is zero`() {
        // Given: Unread messages are all from current user
        val lastReadMessage = createMessage(id = "msg-2", user = otherUser, createdAt = Date(1000))
        val ownMessage1 = createMessage(id = "msg-3", user = currentUser, createdAt = Date(2000))
        val ownMessage2 = createMessage(id = "msg-4", user = currentUser, createdAt = Date(3000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            ownMessage1,
            ownMessage2,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 0, // Server says no unread
            lastRead = Date(1500),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should return null (no messages from others)
        result shouldBe null
    }

    /**
     * Test Case 11: Pending/Offline Messages Scenario
     * When user sends messages offline (pending), then receives messages from others.
     */
    @Test
    fun `should handle pending messages scenario correctly`() {
        // Given: User sends pending messages, then receives messages from others
        val lastReadMessage = createMessage(id = "msg-2", user = otherUser, createdAt = Date(1000))
        val pendingMessage = createMessage(id = "msg-3", user = currentUser, createdAt = Date(2000))
        val otherMessage = createMessage(id = "msg-4", user = otherUser, createdAt = Date(3000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            pendingMessage,
            otherMessage,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 2,
            lastRead = Date(1500),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should place unread indicator after the pending message, before other user's message
        result shouldBeEqualTo MessageListController.UnreadLabel(
            unreadCount = 2,
            lastReadMessageId = "msg-3", // Pending message
            buttonVisibility = true,
        )
    }

    /**
     * Test Case 12: Last Read Message Not Found in List
     * When lastReadMessageId is not in the messages list, should skip ownership checks
     * and create a standard unread label using the provided lastReadMessageId.
     * This handles cases where messages are loaded around a specific message or filtered.
     */
    @Test
    fun `should handle when last read message is not in the messages list`() {
        // Given: lastReadMessageId doesn't exist in the messages list
        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            createMessage(id = "msg-2", user = otherUser, createdAt = Date(200)),
            createMessage(id = "msg-3", user = otherUser, createdAt = Date(300)),
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-nonexistent",
            unreadMessages = 3,
            lastRead = Date(50), // Before all messages
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should use the provided lastReadMessageId without complex ownership checks
        result shouldBeEqualTo MessageListController.UnreadLabel(
            unreadCount = 3,
            lastReadMessageId = "msg-nonexistent",
            buttonVisibility = true,
        )
    }

    /**
     * Test Case 13: Complex Scenario with Mixed Message Ownership
     * Multiple messages with alternating ownership.
     */
    @Test
    fun `should handle complex scenario with alternating message ownership`() {
        // Given: Complex message pattern
        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            createMessage(id = "msg-2", user = otherUser, createdAt = Date(200)), // Last read
            createMessage(id = "msg-3", user = currentUser, createdAt = Date(300)),
            createMessage(id = "msg-4", user = currentUser, createdAt = Date(400)),
            createMessage(id = "msg-5", user = otherUser, createdAt = Date(500)),
            createMessage(id = "msg-6", user = currentUser, createdAt = Date(600)),
            createMessage(id = "msg-7", user = otherUser, createdAt = Date(700)),
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 5,
            lastRead = Date(250),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should use msg-4 (last own message before first other user's message msg-5)
        result shouldBeEqualTo MessageListController.UnreadLabel(
            unreadCount = 5,
            lastReadMessageId = "msg-4",
            buttonVisibility = true,
        )
    }

    /**
     * Test Case 14: Single Own Message After Last Read
     * Only one own message after last read, followed by other users' messages.
     */
    @Test
    fun `should handle single own message before others messages`() {
        // Given: Single own message before others
        val lastReadMessage = createMessage(id = "msg-2", user = otherUser, createdAt = Date(1000))
        val ownMessage = createMessage(id = "msg-3", user = currentUser, createdAt = Date(2000))
        val otherMessage = createMessage(id = "msg-4", user = otherUser, createdAt = Date(3000))

        val messages = listOf(
            createMessage(id = "msg-1", user = currentUser, createdAt = Date(100)),
            lastReadMessage,
            ownMessage,
            otherMessage,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-2",
            unreadMessages = 2,
            lastRead = Date(1500),
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should use the single own message as anchor
        result shouldBeEqualTo MessageListController.UnreadLabel(
            unreadCount = 2,
            lastReadMessageId = "msg-3",
            buttonVisibility = true,
        )
    }

    /**
     * Test Case 15: Logout/Login Scenario - All Unread Messages Are From Current User
     * When lastReadMessageId is not in the messages list (after logout/login, read state might be out of sync)
     * AND all unread messages are from the current user, should return null.
     * This simulates the scenario where:
     * 1. User sends message offline
     * 2. User logs out (clears history)
     * 3. User logs back in
     * 4. User reopens chat - lastReadMessageId from server doesn't exist in loaded messages
     * 5. All messages are from the current user (their own synced message)
     */
    @Test
    fun `should return null when last read message not found and all unread messages are from current user`() {
        // Given: lastReadMessageId doesn't exist in the messages list (read state out of sync after logout/login)
        // and all messages are from the current user (their own message that was sent offline and synced)
        val ownMessage1 = createMessage(id = "msg-1", user = currentUser, createdAt = Date(1000))
        val ownMessage2 = createMessage(id = "msg-2", user = currentUser, createdAt = Date(2000))

        val messages = listOf(
            ownMessage1,
            ownMessage2,
        )

        val channelUserRead = createChannelUserRead(
            lastReadMessageId = "msg-nonexistent", // Doesn't exist in messages (read state out of sync)
            unreadMessages = 2, // Server might have incorrect count
            lastRead = Date(500), // Before all messages
        )

        // When: Calculate unread label
        val result = calculator.calculateUnreadLabel(
            channelUserRead = channelUserRead,
            messages = messages,
            currentUserId = currentUser.id,
            shouldShowButton = true,
        )

        // Then: Should return null - user has already seen their own messages
        result shouldBe null
    }

    // Helper functions

    private fun createMessage(
        id: String,
        user: User,
        createdAt: Date,
        deletedAt: Date? = null,
    ): Message {
        return randomMessage(
            id = id,
            user = user,
            createdAt = createdAt,
            deletedAt = deletedAt,
            deletedForMe = false,
        )
    }

    private fun createChannelUserRead(
        lastReadMessageId: String?,
        unreadMessages: Int,
        lastRead: Date,
    ): ChannelUserRead {
        return randomChannelUserRead(
            lastReadMessageId = lastReadMessageId,
            unreadMessages = unreadMessages,
            lastRead = lastRead,
        )
    }
}
