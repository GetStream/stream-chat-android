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

import android.app.PendingIntent
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.R
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomInt
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class NotificationActionsFactoryTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `createMarkReadAction should return action with default values`() {
        val notificationId = randomInt()
        val channel = randomChannel()
        val message = randomMessage()

        val action = NotificationActionsFactory.createMarkReadAction(
            context = context,
            notificationId = notificationId,
            channel = channel,
            message = message,
        )

        assertNotNull(action)
        assertEquals(android.R.drawable.ic_menu_view, action.iconCompat?.resId)
        assertEquals(context.getString(R.string.stream_chat_notification_read), action.title.toString())
        assertNotNull(action.actionIntent)
    }

    @Test
    fun `createMarkReadAction should return action with custom values`() {
        val notificationId = randomInt()
        val channel = randomChannel()
        val message = randomMessage()
        val customIcon = android.R.drawable.ic_menu_close_clear_cancel
        val customTitle = randomString()
        val customPendingIntent: PendingIntent = mock()

        val action = NotificationActionsFactory.createMarkReadAction(
            context = context,
            notificationId = notificationId,
            channel = channel,
            message = message,
            icon = customIcon,
            title = customTitle,
            pendingIntent = customPendingIntent,
        )

        assertNotNull(action)
        assertEquals(customIcon, action.iconCompat?.resId)
        assertEquals(customTitle, action.title.toString())
        assertEquals(customPendingIntent, action.actionIntent)
    }

    @Test
    fun `createReplyAction should return action with default values`() {
        val notificationId = randomInt()
        val channel = randomChannel()

        val action = NotificationActionsFactory.createReplyAction(
            context = context,
            notificationId = notificationId,
            channel = channel,
        )

        assertNotNull(action)
        assertEquals(android.R.drawable.ic_menu_send, action.iconCompat?.resId)
        assertEquals(context.getString(R.string.stream_chat_notification_reply), action.title.toString())
        assertNotNull(action.actionIntent)
        assertNotNull(action.remoteInputs)
        assertTrue(action.remoteInputs!!.isNotEmpty())
        assertTrue(action.allowGeneratedReplies)
    }

    @Test
    fun `createReplyAction should return action with custom values`() {
        val notificationId = randomInt()
        val channel = randomChannel()
        val customIcon = android.R.drawable.ic_menu_edit
        val customTitle = randomString()
        val customPendingIntent: PendingIntent = mock()

        val action = NotificationActionsFactory.createReplyAction(
            context = context,
            notificationId = notificationId,
            channel = channel,
            icon = customIcon,
            title = customTitle,
            pendingIntent = customPendingIntent,
        )

        assertNotNull(action)
        assertEquals(customIcon, action.iconCompat?.resId)
        assertEquals(customTitle, action.title.toString())
        assertEquals(customPendingIntent, action.actionIntent)
    }
}
