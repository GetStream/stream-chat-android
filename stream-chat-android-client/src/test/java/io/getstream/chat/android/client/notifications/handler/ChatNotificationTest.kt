/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.notifications.handler

import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class ChatNotificationTest {

    private val testChannel: Channel = randomChannel()
    private val testMessage: Message = randomMessage()

    @Test
    fun `create should return MessageNew notification for message_new type`() {
        // Given
        val payload = emptyMap<String, Any?>()

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_MESSAGE_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertInstanceOf(ChatNotification.MessageNew::class.java, result)
        val messageNew = result as ChatNotification.MessageNew
        Assertions.assertEquals(testChannel, messageNew.channel)
        Assertions.assertEquals(testMessage, messageNew.message)
    }

    @Test
    fun `create should return MessageUpdated notification for message_updated type`() {
        // Given
        val payload = emptyMap<String, Any?>()

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_MESSAGE_UPDATED, payload, testChannel, testMessage)

        // Then
        Assertions.assertInstanceOf(ChatNotification.MessageUpdated::class.java, result)
        val messageUpdated = result as ChatNotification.MessageUpdated
        Assertions.assertEquals(testChannel, messageUpdated.channel)
        Assertions.assertEquals(testMessage, messageUpdated.message)
    }

    @Test
    fun `create should return NotificationReminderDue notification for notification_reminder_due type`() {
        // Given
        val payload = emptyMap<String, Any?>()

        // When
        val result =
            ChatNotification.create(ChatNotification.TYPE_NOTIFICATION_REMINDER_DUE, payload, testChannel, testMessage)

        // Then
        Assertions.assertInstanceOf(ChatNotification.NotificationReminderDue::class.java, result)
        val reminderDue = result as ChatNotification.NotificationReminderDue
        Assertions.assertEquals(testChannel, reminderDue.channel)
        Assertions.assertEquals(testMessage, reminderDue.message)
    }

    @Test
    fun `create should return ReactionNew notification for reaction_new type with all required fields`() {
        // Given
        val title = "New reaction"
        val body = "Someone reacted to your message"
        val reactionType = "like"
        val reactionUserId = "user123"
        val reactionUserImageUrl = "https://example.com/avatar.jpg"
        val payload = mapOf(
            "title" to title,
            "body" to body,
            "reaction_type" to reactionType,
            "reaction_user_id" to reactionUserId,
            "reaction_user_image" to reactionUserImageUrl,
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertInstanceOf(ChatNotification.ReactionNew::class.java, result)
        val reactionNew = result as ChatNotification.ReactionNew
        Assertions.assertEquals(title, reactionNew.title)
        Assertions.assertEquals(body, reactionNew.body)
        Assertions.assertEquals(reactionType, reactionNew.type)
        Assertions.assertEquals(reactionUserId, reactionNew.reactionUserId)
        Assertions.assertEquals(reactionUserImageUrl, reactionNew.reactionUserImageUrl)
        Assertions.assertEquals(testChannel, reactionNew.channel)
        Assertions.assertEquals(testMessage, reactionNew.message)
    }

    @Test
    fun `create should return ReactionNew notification for reaction_new type with null user image`() {
        // Given
        val title = "New reaction"
        val body = "Someone reacted to your message"
        val reactionType = "love"
        val reactionUserId = "user456"
        val payload = mapOf(
            "title" to title,
            "body" to body,
            "reaction_type" to reactionType,
            "reaction_user_id" to reactionUserId,
            "reaction_user_image" to null,
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertInstanceOf(ChatNotification.ReactionNew::class.java, result)
        val reactionNew = result as ChatNotification.ReactionNew
        Assertions.assertEquals(title, reactionNew.title)
        Assertions.assertEquals(body, reactionNew.body)
        Assertions.assertEquals(reactionType, reactionNew.type)
        Assertions.assertEquals(reactionUserId, reactionNew.reactionUserId)
        Assertions.assertNull(reactionNew.reactionUserImageUrl)
        Assertions.assertEquals(testChannel, reactionNew.channel)
        Assertions.assertEquals(testMessage, reactionNew.message)
    }

    @Test
    fun `create should return ReactionNew notification for reaction_new type without user image field`() {
        // Given
        val title = "New reaction"
        val body = "Someone reacted to your message"
        val reactionType = "wow"
        val reactionUserId = "user789"
        val payload = mapOf(
            "title" to title,
            "body" to body,
            "reaction_type" to reactionType,
            "reaction_user_id" to reactionUserId,
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertInstanceOf(ChatNotification.ReactionNew::class.java, result)
        val reactionNew = result as ChatNotification.ReactionNew
        Assertions.assertEquals(title, reactionNew.title)
        Assertions.assertEquals(body, reactionNew.body)
        Assertions.assertEquals(reactionType, reactionNew.type)
        Assertions.assertEquals(reactionUserId, reactionNew.reactionUserId)
        Assertions.assertNull(reactionNew.reactionUserImageUrl)
        Assertions.assertEquals(testChannel, reactionNew.channel)
        Assertions.assertEquals(testMessage, reactionNew.message)
    }

    @Test
    fun `create should return null for unknown notification type`() {
        // Given
        val payload = emptyMap<String, Any?>()

        // When
        val result = ChatNotification.create("unknown.type", payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when title is missing`() {
        // Given
        val payload = mapOf(
            "body" to "Someone reacted to your message",
            "reaction_type" to "like",
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when title is null`() {
        // Given
        val payload = mapOf(
            "title" to null,
            "body" to "Someone reacted to your message",
            "reaction_type" to "like",
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when title is not a string`() {
        // Given
        val payload = mapOf(
            "title" to 123,
            "body" to "Someone reacted to your message",
            "reaction_type" to "like",
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when body is missing`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "reaction_type" to "like",
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when body is null`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to null,
            "reaction_type" to "like",
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when body is not a string`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to 456,
            "reaction_type" to "like",
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when reaction_type is missing`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to "Someone reacted to your message",
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when reaction_type is null`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to "Someone reacted to your message",
            "reaction_type" to null,
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when reaction_type is not a string`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to "Someone reacted to your message",
            "reaction_type" to 789,
            "reaction_user_id" to "user123",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when reaction_user_id is missing`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to "Someone reacted to your message",
            "reaction_type" to "like",
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when reaction_user_id is null`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to "Someone reacted to your message",
            "reaction_type" to "like",
            "reaction_user_id" to null,
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }

    @Test
    fun `create should return null for reaction_new type when reaction_user_id is not a string`() {
        // Given
        val payload = mapOf(
            "title" to "New reaction",
            "body" to "Someone reacted to your message",
            "reaction_type" to "like",
            "reaction_user_id" to 999,
        )

        // When
        val result = ChatNotification.create(ChatNotification.TYPE_REACTION_NEW, payload, testChannel, testMessage)

        // Then
        Assertions.assertNull(result)
    }
}
