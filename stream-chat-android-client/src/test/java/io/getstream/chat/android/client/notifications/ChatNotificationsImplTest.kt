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

package io.getstream.chat.android.client.notifications

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.randomPushMessage
import io.getstream.chat.android.client.receipts.MessageReceiptManager
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.PushMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.whenever
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ChatNotificationsImplTest {

    @Test
    fun `onPushMessage calls onPushNotificationReceived`() {
        val pushMessage = randomPushMessage()
        val mockListener = mock<PushNotificationReceivedListener>()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onPushMessage(pushMessage, mockListener)

        verify(mockListener).onPushNotificationReceived(
            channelType = pushMessage.channelType,
            channelId = pushMessage.channelId,
        )
    }

    @Test
    fun `onPushMessage marks message as delivered`() {
        val pushMessage = randomPushMessage()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onPushMessage(pushMessage)

        val message = Message(
            id = pushMessage.messageId,
            cid = "${pushMessage.channelType}:${pushMessage.channelId}",
        )
        fixture.verifyMarkMessageAsDeliveredCalled(message)
    }

    @Test
    fun `onPushMessage schedules work when shouldShowNotificationOnPush is true and handler does not handle message`() {
        val pushMessage = randomPushMessage()
        val fixture = Fixture()
        val sut = fixture.get()

        sut.onPushMessage(pushMessage)

        val workInfos = getLoadNotificationDataWorkerInfos()
        assertNotNull(workInfos.firstOrNull())
    }

    @Test
    fun `onPushMessage does not schedule work when shouldShowNotificationOnPush is false`() {
        val pushMessage = randomPushMessage()
        val fixture = Fixture()
            .givenNotificationConfig(config = NotificationConfig(shouldShowNotificationOnPush = { false }))
        val sut = fixture.get()

        sut.onPushMessage(pushMessage)

        val workInfos = getLoadNotificationDataWorkerInfos()
        assertNull(workInfos.firstOrNull())
    }

    @Test
    fun `onPushMessage does not schedule work when handler handles message`() {
        val pushMessage = randomPushMessage()
        val fixture = Fixture()
            .givenOnPushMessageHandled(pushMessage = pushMessage, handled = true)
        val sut = fixture.get()

        sut.onPushMessage(pushMessage)

        val workInfos = getLoadNotificationDataWorkerInfos()
        assertNull(workInfos.firstOrNull())
    }

    private class Fixture {

        private var mockNotificationHandler = mock<NotificationHandler>()
        private var notificationConfig = NotificationConfig()
        private val mockMessageReceiptManager = mock<MessageReceiptManager>()

        private val mockChatClient = mock<ChatClient> {
            on { messageReceiptManager } doReturn mockMessageReceiptManager
        }

        fun givenOnPushMessageHandled(pushMessage: PushMessage, handled: Boolean) = apply {
            whenever(mockNotificationHandler.onPushMessage(pushMessage)) doReturn handled
        }

        fun givenNotificationConfig(config: NotificationConfig) = apply {
            notificationConfig = config
        }

        fun verifyMarkMessageAsDeliveredCalled(message: Message) {
            verifyBlocking(mockMessageReceiptManager) { markMessageAsDelivered(message) }
        }

        fun get(): ChatNotificationsImpl {
            val context = ApplicationProvider.getApplicationContext<Context>()
            WorkManagerTestInitHelper.initializeTestWorkManager(context)

            return ChatNotificationsImpl(
                handler = mockNotificationHandler,
                notificationConfig = notificationConfig,
                context = context,
                scope = CoroutineScope(UnconfinedTestDispatcher()),
                chatClientProvider = { mockChatClient },
            )
        }
    }
}

private fun getLoadNotificationDataWorkerInfos(): List<WorkInfo> {
    val workInfos = WorkManager
        .getInstance(ApplicationProvider.getApplicationContext())
        .getWorkInfosByTag(LoadNotificationDataWorker::class.qualifiedName!!).get()
    return workInfos
}
