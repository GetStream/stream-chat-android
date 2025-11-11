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

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.randomPushMessage
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomUser
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ChatNotificationHandlerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockNotificationManager: NotificationManager = mock()
    private val mockSharedPreferences: SharedPreferences = mock()
    private val mockSharedPreferencesEditor: SharedPreferences.Editor = mock()
    private val mockNotificationChannel: NotificationChannel = mock()
    private val mockOnNewPushMessage: (PushMessage) -> Boolean = mock()

    private val testChannel = randomChannel(type = "messaging", id = "test_channel")
    private val testMessage = randomMessage(id = "test_message", text = "Test message content")
    private val testUser = randomUser(id = "test_user", name = "Test User")

    private val newMessageIntent: (Message, Channel) -> Intent = { _, _ -> mock() }
    private val notificationChannel: () -> NotificationChannel = { mockNotificationChannel }
    private val notificationTextFormatter: (User?, Message) -> CharSequence = { _, message -> message.text }
    private val actionsProvider: (Int, Channel, Message) -> List<NotificationCompat.Action> = { _, _, _ -> emptyList() }
    private val notificationBuilderTransformer: (
        NotificationCompat.Builder,
        ChatNotification,
    ) -> NotificationCompat.Builder = { builder, _ -> builder }
    private val currentUserProvider: () -> User? = { testUser }

    private lateinit var chatNotificationHandler: ChatNotificationHandler

    @Before
    fun setUp() {
        // Mock SharedPreferences behavior
        whenever(mockSharedPreferences.edit()) doReturn mockSharedPreferencesEditor
        whenever(mockSharedPreferencesEditor.putInt(any(), any())) doReturn mockSharedPreferencesEditor
        whenever(mockSharedPreferencesEditor.putStringSet(any(), anyOrNull())) doReturn mockSharedPreferencesEditor
        whenever(mockSharedPreferencesEditor.remove(any())) doReturn mockSharedPreferencesEditor
        whenever(mockSharedPreferences.getStringSet(any(), anyOrNull())) doReturn emptySet()
        whenever(mockSharedPreferences.getInt(any(), any())) doReturn 0

        // Mock NotificationChannel
        whenever(mockNotificationChannel.id) doReturn "test_channel_id"

        // Create a spy of the context to mock getSharedPreferences and getSystemService
        val contextSpy = spy(context) {
            on { getSharedPreferences(any(), any()) } doReturn mockSharedPreferences
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn mockNotificationManager
        }

        chatNotificationHandler = ChatNotificationHandler(
            context = contextSpy,
            newMessageIntent = newMessageIntent,
            notificationChannel = notificationChannel,
            notificationTextFormatter = notificationTextFormatter,
            actionsProvider = actionsProvider,
            notificationBuilderTransformer = notificationBuilderTransformer,
            currentUserProvider = currentUserProvider,
            onNewPushMessage = mockOnNewPushMessage,
        )
    }

    @Test
    fun `showNotification should handle MessageNew notification correctly`() {
        // Given
        val notification = ChatNotification.MessageNew(testChannel, testMessage)

        // When
        chatNotificationHandler.showNotification(notification)

        // Then
        // Verify that NotificationManager.notify is called twice (individual notification + group summary)
        verify(mockNotificationManager, times(2)).notify(any<Int>(), any<Notification>())

        // Verify SharedPreferences interactions for storing notification IDs
        verify(mockSharedPreferencesEditor, times(2)).putStringSet(any(), any())
        verify(mockSharedPreferencesEditor).putInt(any(), any())
    }

    @Test
    fun `showNotification should handle MessageUpdated notification correctly`() {
        // Given
        val notification = ChatNotification.MessageUpdated(testChannel, testMessage)

        // When
        chatNotificationHandler.showNotification(notification)

        // Then
        // Verify that NotificationManager.notify is called twice (individual notification + group summary)
        verify(mockNotificationManager, times(2)).notify(any<Int>(), any<Notification>())

        // Verify SharedPreferences interactions for storing notification IDs
        verify(mockSharedPreferencesEditor, times(2)).putStringSet(any(), any())
        verify(mockSharedPreferencesEditor).putInt(any(), any())
    }

    @Test
    fun `showNotification should handle ReactionNew notification correctly`() {
        // Given
        val notification = ChatNotification.ReactionNew(
            title = "New Reaction",
            body = "Someone reacted to your message",
            type = "like",
            reactionUserId = "reaction_user_id",
            reactionUserImageUrl = "https://example.com/avatar.jpg",
            channel = testChannel,
            message = testMessage,
        )

        // When
        chatNotificationHandler.showNotification(notification)

        // Then
        // Verify that NotificationManager.notify is called once (no group summary for reactions)
        verify(mockNotificationManager, times(1)).notify(any<Int>(), any<Notification>())

        // Verify SharedPreferences interactions for storing notification ID without summary
        verify(mockSharedPreferencesEditor, times(1)).putStringSet(any(), any())
    }

    @Test
    fun `showNotification should handle NotificationReminderDue notification correctly`() {
        // Given
        val notification = ChatNotification.NotificationReminderDue(testChannel, testMessage)

        // When
        chatNotificationHandler.showNotification(notification)

        // Then
        // Verify that NotificationManager.notify is called once (no group summary for reminders)
        verify(mockNotificationManager, times(1)).notify(any<Int>(), any<Notification>())

        // Verify SharedPreferences interactions for storing notification ID without summary
        verify(mockSharedPreferencesEditor, times(1)).putStringSet(any(), any())
    }

    @Test
    fun `onPushMessage should be handleable`() {
        // Given
        val handled = randomBoolean()
        val pushMessage = randomPushMessage()
        whenever(mockOnNewPushMessage.invoke(pushMessage)) doReturn handled

        // When
        val actual = chatNotificationHandler.onPushMessage(message = pushMessage)

        // Then
        assertEquals(handled, actual)
    }
}
