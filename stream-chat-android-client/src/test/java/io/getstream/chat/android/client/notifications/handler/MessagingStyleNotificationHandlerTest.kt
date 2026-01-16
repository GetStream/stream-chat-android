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
import io.getstream.android.push.permissions.NotificationPermissionHandler
import io.getstream.android.push.permissions.NotificationPermissionStatus
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.randomPushMessage
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class MessagingStyleNotificationHandlerTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val mockNotificationManager: NotificationManager = mock()
    private val mockSharedPreferences: SharedPreferences = mock()
    private val mockSharedPreferencesEditor: SharedPreferences.Editor = mock()
    private val mockNotificationChannel: NotificationChannel = mock()
    private val mockUserIconBuilder: UserIconBuilder = mock()
    private val mockPermissionHandler: NotificationPermissionHandler = mock()
    private val mockOnPushMessage: (PushMessage) -> Boolean = mock()
    private val mockChatClient: ChatClient = mock()

    private val newMessageIntent: (Message, Channel) -> Intent = { _, _ -> mock() }
    private val notificationChannel: () -> NotificationChannel = { mockNotificationChannel }
    private val notificationTextFormatter: (User?, Message) -> CharSequence = { _, message -> message.text }
    private val actionsProvider: (Int, Channel, Message) -> List<NotificationCompat.Action> = { _, _, _ -> emptyList() }
    private val notificationBuilderTransformer:
        (NotificationCompat.Builder, ChatNotification) -> NotificationCompat.Builder = { builder, _ -> builder }

    private lateinit var handler: MessagingStyleNotificationHandler

    @Before
    fun setUp() {
        whenever(mockSharedPreferences.edit()) doReturn mockSharedPreferencesEditor
        whenever(mockSharedPreferencesEditor.putStringSet(any(), anyOrNull())) doReturn
            mockSharedPreferencesEditor
        whenever(mockSharedPreferences.getStringSet(any(), anyOrNull())) doReturn
            emptySet()
        whenever(mockNotificationChannel.id) doReturn randomString()

        whenever(mockChatClient.launch(any())) doAnswer { invocation ->
            val block = invocation.getArgument<suspend CoroutineScope.() -> Unit>(0)
            runBlocking { block() }
            Job()
        }

        val contextSpy = spy(context) {
            on { getSharedPreferences(any(), any()) } doReturn mockSharedPreferences
            on { getSystemService(Context.NOTIFICATION_SERVICE) } doReturn mockNotificationManager
        }

        object : ChatClient.ChatClientBuilder() {
            override fun internalBuild(): ChatClient = mockChatClient
        }.build()

        handler = MessagingStyleNotificationHandler(
            context = contextSpy,
            newMessageIntent = newMessageIntent,
            notificationChannel = notificationChannel,
            userIconBuilder = mockUserIconBuilder,
            permissionHandler = mockPermissionHandler,
            notificationTextFormatter = notificationTextFormatter,
            actionsProvider = actionsProvider,
            notificationBuilderTransformer = notificationBuilderTransformer,
            onNewPushMessage = mockOnPushMessage,
            notificationIdFactory = null,
        )
    }

    @Test
    fun `onNotificationPermissionStatus should call permissionHandler`() {
        handler.onNotificationPermissionStatus(NotificationPermissionStatus.GRANTED)
        verify(mockPermissionHandler).onPermissionGranted()
    }

    @Test
    fun `showNotification should create and show notification`() = runTest {
        val channel = randomChannel()
        val message = randomMessage()
        val notification = ChatNotification.MessageNew(channel, message)
        whenever(mockNotificationManager.activeNotifications) doReturn emptyArray()
        whenever(mockChatClient.getCurrentUser()) doReturn randomUser()
        whenever(mockChatClient.getStoredUser()) doReturn null

        handler.showNotification(notification)

        verify(mockNotificationManager).notify(any<Int>(), any<Notification>())
    }

    @Test
    fun `dismissChannelNotifications should dismiss all tracked channel notifications and clear storage`() {
        val channelType = randomString()
        val channelId = randomString()
        val channelKey = "KEY_CHANNEL_NOTIFICATIONS_$channelType:$channelId"
        val notificationId1 = 100
        val notificationId2 = 200
        whenever(mockSharedPreferences.getStringSet(channelKey, null)) doReturn
            setOf(notificationId1.toString(), notificationId2.toString())
        whenever(mockSharedPreferencesEditor.remove(any())) doReturn mockSharedPreferencesEditor

        handler.dismissChannelNotifications(channelType, channelId)

        // Verify all channel notifications are dismissed
        verify(mockNotificationManager).cancel(notificationId1)
        verify(mockNotificationManager).cancel(notificationId2)
        // Verify the channel notification storage is cleared
        verify(mockSharedPreferencesEditor).remove(channelKey)
    }

    @Test
    fun `dismissAllNotifications should dismiss global and channel notifications and clear all storage`() {
        val globalKey = "KEY_NOTIFICATIONS_SHOWN"
        val channelKey1 = "KEY_CHANNEL_NOTIFICATIONS_messaging:channel1"
        val channelKey2 = "KEY_CHANNEL_NOTIFICATIONS_livestream:channel2"

        // Mock global notifications
        whenever(mockSharedPreferences.getStringSet(globalKey, null)) doReturn setOf("1")
        // Mock channel notifications
        whenever(mockSharedPreferences.getStringSet(channelKey1, null)) doReturn setOf("10")
        whenever(mockSharedPreferences.getStringSet(channelKey1, emptySet())) doReturn setOf("10")
        whenever(mockSharedPreferences.getStringSet(channelKey2, null)) doReturn setOf("20")
        whenever(mockSharedPreferences.getStringSet(channelKey2, emptySet())) doReturn setOf("20")
        // Mock all keys for iteration
        whenever(mockSharedPreferences.all) doReturn mapOf(
            globalKey to setOf("1"),
            channelKey1 to setOf("10"),
            channelKey2 to setOf("20"),
        )
        whenever(mockSharedPreferencesEditor.remove(any())) doReturn mockSharedPreferencesEditor

        handler.dismissAllNotifications()

        // Verify global notifications are dismissed
        verify(mockNotificationManager).cancel(1)
        // Verify channel notifications are dismissed
        verify(mockNotificationManager).cancel(10)
        verify(mockNotificationManager).cancel(20)
        // Verify global notification storage is cleared (via putStringSet with remaining IDs)
        verify(mockSharedPreferencesEditor).putStringSet(globalKey, emptySet())
        // Verify channel notification storage is cleared
        verify(mockSharedPreferencesEditor).remove(channelKey1)
        verify(mockSharedPreferencesEditor).remove(channelKey2)
    }

    @Test
    fun `onPushMessage should delegate to onNewPushMessage`() {
        val pushMessage = randomPushMessage()
        val handled = randomBoolean()
        whenever(mockOnPushMessage.invoke(pushMessage)) doReturn handled

        val result = handler.onPushMessage(pushMessage)

        assertEquals(handled, result)
        verify(mockOnPushMessage).invoke(pushMessage)
    }
}
