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

package io.getstream.chat.android.ui.common.utils.extensions

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.ui.common.state.messages.list.MessageFooterVisibility
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.util.Date

internal class MessageFooterVisibilityTest {

    private val user1 = User(id = "user1", name = "User 1")
    private val user2 = User(id = "user2", name = "User 2")

    // Test MessageFooterVisibility.Always

    @Test
    fun `Always should show footer when nextMessage is null`() {
        val message = createMessage(user1)

        val result = MessageFooterVisibility.Always.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = null,
        )

        result `should be equal to` true
    }

    @Test
    fun `Always should show footer when nextMessage is not null`() {
        val message = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.Always.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `Always should show footer when isLastMessageInGroup is true`() {
        val message = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.Always.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = true,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    // Test MessageFooterVisibility.Never

    @Test
    fun `Never should not show footer when nextMessage is null`() {
        val message = createMessage(user1)

        val result = MessageFooterVisibility.Never.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = null,
        )

        result `should be equal to` false
    }

    @Test
    fun `Never should not show footer when nextMessage is not null`() {
        val message = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.Never.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` false
    }

    @Test
    fun `Never should not show footer when isLastMessageInGroup is true`() {
        val message = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.Never.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = true,
            nextMessage = nextMessage,
        )

        result `should be equal to` false
    }

    @Test
    fun `Never should not show footer when message is edited`() {
        val message = createMessage(user1, messageTextUpdatedAt = Date())

        val result = MessageFooterVisibility.Never.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = createMessage(user1),
        )

        result `should be equal to` false
    }

    // Test MessageFooterVisibility.LastInGroup

    @Test
    fun `LastInGroup should show footer when nextMessage is null`() {
        val message = createMessage(user1)

        val result = MessageFooterVisibility.LastInGroup.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = null,
        )

        result `should be equal to` true
    }

    @Test
    fun `LastInGroup should show footer when isLastMessageInGroup is true`() {
        val message = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.LastInGroup.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = true,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `LastInGroup should not show footer when isLastMessageInGroup is false`() {
        val message = createMessage(user1)
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.LastInGroup.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` false
    }

    @Test
    fun `LastInGroup should show footer when message is edited`() {
        val message = createMessage(user1, messageTextUpdatedAt = Date())
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.LastInGroup.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    // Test MessageFooterVisibility.WithTimeDifference

    @Test
    fun `WithTimeDifference should show footer when nextMessage is null`() {
        val message = createMessage(user1)

        val result = MessageFooterVisibility.WithTimeDifference().shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = null,
        )

        result `should be equal to` true
    }

    @Test
    fun `WithTimeDifference should show footer when isLastMessageInGroup is true`() {
        val message = createMessage(user1, createdAt = Date(1000))
        val nextMessage = createMessage(user1, createdAt = Date(2000))

        val result = MessageFooterVisibility.WithTimeDifference().shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = true,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `WithTimeDifference should not show footer when message is deleted`() {
        val message = createMessage(user1, createdAt = Date(1000), deletedAt = Date(1500))
        val nextMessage = createMessage(user1, createdAt = Date(2000))

        val result = MessageFooterVisibility.WithTimeDifference().shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` false
    }

    @Test
    fun `WithTimeDifference should show footer when users are different`() {
        val message = createMessage(user1, createdAt = Date(1000))
        val nextMessage = createMessage(user2, createdAt = Date(2000))

        val result = MessageFooterVisibility.WithTimeDifference().shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `WithTimeDifference should show footer when next message is deleted`() {
        val message = createMessage(user1, createdAt = Date(1000))
        val nextMessage = createMessage(user1, createdAt = Date(2000), deletedAt = Date(2500))

        val result = MessageFooterVisibility.WithTimeDifference().shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `WithTimeDifference should show footer when time difference exceeds threshold`() {
        val timeDiffMillis = 60_000L // 1 minute
        val message = createMessage(user1, createdAt = Date(1000))
        val nextMessage = createMessage(user1, createdAt = Date(62_000)) // More than 1 minute later

        val result = MessageFooterVisibility.WithTimeDifference(timeDiffMillis).shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `WithTimeDifference should not show footer when time difference is within threshold`() {
        val timeDiffMillis = 60_000L // 1 minute
        val message = createMessage(user1, createdAt = Date(1000))
        val nextMessage = createMessage(user1, createdAt = Date(30_000)) // 29 seconds later

        val result = MessageFooterVisibility.WithTimeDifference(timeDiffMillis).shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` false
    }

    @Test
    fun `WithTimeDifference should show footer when time difference exactly equals threshold`() {
        val timeDiffMillis = 60_000L // 1 minute
        val message = createMessage(user1, createdAt = Date(1000))
        val nextMessage = createMessage(user1, createdAt = Date(61_001)) // Exactly 60 seconds + 1ms later

        val result = MessageFooterVisibility.WithTimeDifference(timeDiffMillis).shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `WithTimeDifference should show footer when message is edited`() {
        val message = createMessage(user1, createdAt = Date(1000), messageTextUpdatedAt = Date(1500))
        val nextMessage = createMessage(user1, createdAt = Date(2000))

        val result = MessageFooterVisibility.WithTimeDifference().shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `WithTimeDifference should not show footer when all conditions are favorable to hide`() {
        val timeDiffMillis = 60_000L // 1 minute
        val message = createMessage(user1, createdAt = Date(1000))
        val nextMessage = createMessage(user1, createdAt = Date(10_000)) // 9 seconds later

        val result = MessageFooterVisibility.WithTimeDifference(timeDiffMillis).shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` false
    }

    // Test edited messages

    @Test
    fun `Always should show footer when message is edited`() {
        val message = createMessage(user1, messageTextUpdatedAt = Date())
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.Always.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    @Test
    fun `LastInGroup should show footer when message is edited even if not last in group`() {
        val message = createMessage(user1, messageTextUpdatedAt = Date())
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.LastInGroup.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` true
    }

    // Helper functions

    private fun createMessage(
        user: User,
        createdAt: Date? = null,
        deletedAt: Date? = null,
        messageTextUpdatedAt: Date? = null,
    ): Message {
        return randomMessage(
            user = user,
            createdAt = createdAt,
            deletedAt = deletedAt,
            messageTextUpdatedAt = messageTextUpdatedAt,
            deletedForMe = false,
        )
    }
}
