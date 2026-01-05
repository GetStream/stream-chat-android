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

package io.getstream.chat.android.client.extensions.internal

import io.getstream.chat.android.randomMessageReminder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MessageReminderTest {

    @Test
    fun `toMessageReminderInfo maps properties correctly`() {
        // Given
        val reminder = randomMessageReminder()
        // When
        val result = reminder.toMessageReminderInfo()
        // Then
        assertEquals(reminder.remindAt, result.remindAt)
        assertEquals(reminder.createdAt, result.createdAt)
        assertEquals(reminder.updatedAt, result.updatedAt)
    }

    @Test
    fun `toMessageReminderInfo with null remindAt maps correctly`() {
        // Given
        val reminder = randomMessageReminder(remindAt = null)
        // When
        val result = reminder.toMessageReminderInfo()
        // Then
        assertEquals(null, result.remindAt)
        assertEquals(reminder.createdAt, result.createdAt)
        assertEquals(reminder.updatedAt, result.updatedAt)
    }
}
