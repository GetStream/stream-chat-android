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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class NotificationConfigTest {

    @Test
    fun `ignorePushMessageWhenUserOnline should return true for TYPE_MESSAGE_NEW by default`() {
        // Given
        val config = NotificationConfig()
        // When
        val result = config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_MESSAGE_NEW)
        // Then
        Assertions.assertTrue(result, "TYPE_MESSAGE_NEW should be ignored when user is online by default")
    }

    @Test
    fun `ignorePushMessageWhenUserOnline should return true for TYPE_MESSAGE_UPDATED by default`() {
        // Given
        val config = NotificationConfig()
        // When
        val result = config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_MESSAGE_UPDATED)
        // Then
        Assertions.assertTrue(result, "TYPE_MESSAGE_UPDATED should be ignored when user is online by default")
    }

    @Test
    fun `ignorePushMessageWhenUserOnline should return true for TYPE_REACTION_NEW by default`() {
        // Given
        val config = NotificationConfig()
        // When
        val result = config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_REACTION_NEW)
        // Then
        Assertions.assertTrue(result, "TYPE_REACTION_NEW should be ignored when user is online by default")
    }

    @Test
    fun `ignorePushMessageWhenUserOnline should return false for TYPE_NOTIFICATION_REMINDER_DUE by default`() {
        // Given
        val config = NotificationConfig()
        // When
        val result = config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_NOTIFICATION_REMINDER_DUE)
        // Then
        Assertions.assertFalse(result, "TYPE_NOTIFICATION_REMINDER_DUE should NOT be ignored when user is online by default")
    }

    @Test
    fun `ignorePushMessageWhenUserOnline should return true for unknown notification types by default`() {
        // Given
        val config = NotificationConfig()
        // When
        val result = config.ignorePushMessageWhenUserOnline("unknown.type")
        // Then
        Assertions.assertTrue(result, "Unknown notification types should be ignored when user is online by default")
    }

    @Test
    fun `ignorePushMessageWhenUserOnline should always return false for TYPE_NOTIFICATION_REMINDER_DUE`() {
        // Given
        val config = NotificationConfig()
        // When
        val result = config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_NOTIFICATION_REMINDER_DUE)
        // Then
        Assertions.assertFalse(result, "TYPE_NOTIFICATION_REMINDER_DUE should always be false when user is online")
    }

    @Test
    fun `ignorePushMessageWhenUserOnline should return true for unknown notification types`() {
        // Given
        val config = NotificationConfig()
        // When
        val result = config.ignorePushMessageWhenUserOnline("unknown.type")
        // Then
        Assertions.assertTrue(result, "Unknown notification types should be ignored when user is online")
    }

    @Test
    fun `ignorePushMessageWhenUserOnline should handle custom function override`() {
        // Given
        val customFunction: (String) -> Boolean = { type ->
            when (type) {
                ChatNotification.TYPE_MESSAGE_NEW -> false // Override default behavior
                ChatNotification.TYPE_REACTION_NEW -> false // Override default behavior
                else -> true
            }
        }
        val config = NotificationConfig(ignorePushMessageWhenUserOnline = customFunction)
        // When & Then
        Assertions.assertFalse(
            config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_MESSAGE_NEW),
            "Custom function should override default behavior for TYPE_MESSAGE_NEW",
        )
        Assertions.assertFalse(
            config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_REACTION_NEW),
            "Custom function should override default behavior for TYPE_REACTION_NEW",
        )
        Assertions.assertTrue(
            config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_MESSAGE_UPDATED),
            "Custom function should handle TYPE_MESSAGE_UPDATED with else clause",
        )
        Assertions.assertTrue(
            config.ignorePushMessageWhenUserOnline(ChatNotification.TYPE_NOTIFICATION_REMINDER_DUE),
            "Custom function should handle TYPE_NOTIFICATION_REMINDER_DUE with else clause",
        )
    }
}
