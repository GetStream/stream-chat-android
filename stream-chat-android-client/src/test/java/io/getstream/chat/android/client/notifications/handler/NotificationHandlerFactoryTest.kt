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

import android.content.Context
import androidx.core.graphics.drawable.IconCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.models.User
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
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

    @Test
    fun `createNotificationHandler should not build the user icon builder until an icon is needed`() {
        val iconContexts = mutableListOf<Context>()
        NotificationDefaults.userIconBuilderFactory = {
            iconContexts += it
            mock()
        }

        NotificationHandlerFactory.createNotificationHandler(context, notificationConfig)

        assertEquals(emptyList<Context>(), iconContexts)
    }

    @Test
    fun `default user icon builder should build the registered builder once and delegate to it`() = runTest {
        val user = User(id = "jc")
        val icon = IconCompat.createWithData(ByteArray(0), 0, 0)
        val registered = mock<UserIconBuilder> { onBlocking { buildIcon(user) } doReturn icon }
        val iconContexts = mutableListOf<Context>()
        NotificationDefaults.userIconBuilderFactory = {
            iconContexts += it
            registered
        }
        val builder = NotificationHandlerFactory.provideDefaultUserIconBuilder(context)

        val first = builder.buildIcon(user)
        val second = builder.buildIcon(user)

        assertEquals(icon, first)
        assertEquals(icon, second)
        assertEquals(listOf(context.applicationContext), iconContexts)
    }

    @After
    fun tearDown() {
        NotificationDefaults.userIconBuilderFactory = null
    }
}
