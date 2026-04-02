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
    fun `LastInGroup should not show footer when message is edited but not last in group`() {
        val message = createMessage(user1, messageTextUpdatedAt = Date())
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.LastInGroup.shouldShowMessageFooter(
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
    fun `LastInGroup should not show footer for edited message when not last in group`() {
        val message = createMessage(user1, messageTextUpdatedAt = Date())
        val nextMessage = createMessage(user1)

        val result = MessageFooterVisibility.LastInGroup.shouldShowMessageFooter(
            message = message,
            isLastMessageInGroup = false,
            nextMessage = nextMessage,
        )

        result `should be equal to` false
    }

    // Helper functions

    private fun createMessage(
        user: User,
        createdAt: Date? = null,
        createdLocallyAt: Date? = null,
        deletedAt: Date? = null,
        messageTextUpdatedAt: Date? = null,
    ): Message {
        return randomMessage(
            user = user,
            createdAt = createdAt,
            createdLocallyAt = createdLocallyAt,
            deletedAt = deletedAt,
            messageTextUpdatedAt = messageTextUpdatedAt,
            deletedForMe = false,
        )
    }
}
