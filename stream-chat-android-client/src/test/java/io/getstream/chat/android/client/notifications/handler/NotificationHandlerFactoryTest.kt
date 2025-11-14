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

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class NotificationHandlerFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val notificationConfig = NotificationConfig()

    @Test
    @Config(sdk = [21])
    fun `createNotificationHandler should return ChatNotificationHandler`() {
        val handler = NotificationHandlerFactory.createNotificationHandler(context, notificationConfig)

        assertInstanceOf<ChatNotificationHandler>(handler)
    }

    @Test
    fun `createNotificationHandler should return MessagingStyleNotificationHandler since API 23`() {
        val handler = NotificationHandlerFactory.createNotificationHandler(context, notificationConfig)

        assertInstanceOf<MessagingStyleNotificationHandler>(handler)
    }
}
