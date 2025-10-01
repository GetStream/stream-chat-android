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

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MessagingStyleNotificationFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val notificationManager: NotificationManager = mock()
    private val userIconBuilder: UserIconBuilder = mock()

    private val notificationChannelId = "test_channel_id"
    private val currentUser = randomUser()
    private val newMessageIntent: (Message, Channel) -> Intent = { _, _ -> mock() }
    private val notificationTextFormatter: (User?, Message) -> CharSequence = { _, message -> message.text }
    private val actionsProvider: (Int, Channel, Message) -> List<NotificationCompat.Action> = { _, _, _ -> emptyList() }
    private val notificationBuilderTransformer:
        (NotificationCompat.Builder, ChatNotification) -> NotificationCompat.Builder = { builder, _ -> builder }
    private val currentUserProvider: () -> User? = { currentUser }

    private lateinit var factory: MessagingStyleNotificationFactory

    @Before
    fun setUp() {
        factory = MessagingStyleNotificationFactory(
            context = context,
            notificationManager = notificationManager,
            notificationChannelId = notificationChannelId,
            userIconBuilder = userIconBuilder,
            newMessageIntent = newMessageIntent,
            notificationTextFormatter = notificationTextFormatter,
            actionsProvider = actionsProvider,
            notificationBuilderTransformer = notificationBuilderTransformer,
            currentUserProvider = currentUserProvider,
        )
    }

    @Test
    fun `createNotificationId should generate correct ID for MessageNew notification`() {
        // Given
        val channel = randomChannel(type = "messaging", id = "channel123")
        val message = randomMessage()
        val notification = ChatNotification.MessageNew(channel, message)

        // When
        val notificationId = factory.createNotificationId(notification)

        // Then
        val expectedId = "messaging:channel123".hashCode()
        assertEquals(expectedId, notificationId)
    }

    @Test
    fun `createNotificationId should generate correct ID for MessageUpdated notification`() {
        // Given
        val channel = randomChannel(type = "messaging", id = "channel123")
        val message = randomMessage(id = "message456")
        val notification = ChatNotification.MessageUpdated(channel, message)

        // When
        val notificationId = factory.createNotificationId(notification)

        // Then
        val expectedId = "messaging:channel123:message456".hashCode()
        assertEquals(expectedId, notificationId)
    }

    @Test
    fun `createNotificationId should generate correct ID for ReactionNew notification`() {
        // Given
        val channel = randomChannel()
        val message = randomMessage(id = "message456")
        val notification = ChatNotification.ReactionNew(
            title = "New reaction",
            body = "Someone reacted",
            type = "like",
            reactionUserId = "user789",
            reactionUserImageUrl = null,
            channel = channel,
            message = message,
        )

        // When
        val notificationId = factory.createNotificationId(notification)

        // Then
        val expectedId = "message456:user789:like".hashCode()
        assertEquals(expectedId, notificationId)
    }

    @Test
    fun `createNotificationId should generate correct ID for NotificationReminderDue notification`() {
        // Given
        val channel = randomChannel(type = "messaging", id = "channel123")
        val message = randomMessage(id = "message456")
        val notification = ChatNotification.NotificationReminderDue(channel, message)

        // When
        val notificationId = factory.createNotificationId(notification)

        // Then
        val expectedId = "messaging:channel123:message456".hashCode()
        assertEquals(expectedId, notificationId)
    }

    @Test
    fun `createNotification should create notification for MessageNew when current user is available`() = runTest {
        // Given
        val channel = randomChannel()
        val message = randomMessage()
        val notification = ChatNotification.MessageNew(channel, message)

        whenever(notificationManager.activeNotifications) doReturn emptyArray()

        // When
        val result = factory.createNotification(notification)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `createNotification should create notification for MessageUpdated when current user is available`() = runTest {
        // Given
        val channel = randomChannel()
        val message = randomMessage()
        val notification = ChatNotification.MessageUpdated(channel, message)

        whenever(notificationManager.activeNotifications) doReturn emptyArray()

        // When
        val result = factory.createNotification(notification)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `createNotification should create notification for ReactionNew when current user is available`() = runTest {
        // Given
        val channel = randomChannel()
        val message = randomMessage()
        val notification = ChatNotification.ReactionNew(
            title = "New reaction",
            body = "Someone reacted to your message",
            type = "like",
            reactionUserId = "user123",
            reactionUserImageUrl = null,
            channel = channel,
            message = message,
        )

        // When
        val result = factory.createNotification(notification)

        // Then
        assertNotNull(result)
    }

    @Test
    fun `createNotification should create notification for NotificationReminderDue when current user is available`() =
        runTest {
            // Given
            val channel = randomChannel()
            val message = randomMessage()
            val notification = ChatNotification.NotificationReminderDue(channel, message)

            // When
            val result = factory.createNotification(notification)

            // Then
            assertNotNull(result)
        }
}
