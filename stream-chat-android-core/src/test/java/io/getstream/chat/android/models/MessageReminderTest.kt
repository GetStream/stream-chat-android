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

package io.getstream.chat.android.models

import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomMessageReminder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class MessageReminderTest {

    @Test
    fun `builder should set every field`() {
        val channel = randomChannel()
        val message = randomMessage()
        val expected = randomMessageReminder(channel = channel, message = message)

        val built = MessageReminder.Builder()
            .withRemindAt(expected.remindAt)
            .withCid(expected.cid)
            .withChannel(channel)
            .withMessageId(expected.messageId)
            .withMessage(message)
            .withCreatedAt(expected.createdAt)
            .withUpdatedAt(expected.updatedAt)
            .build()

        assertEquals(expected, built)
    }

    @Test
    fun `builder copy constructor should copy every field`() {
        val reminder = randomMessageReminder()

        val built = MessageReminder.Builder(reminder).build()

        assertEquals(reminder, built)
    }

    @Test
    fun `getComparableField should return date fields for snake_case and camelCase field names`() {
        val reminder = randomMessageReminder(remindAt = randomDate())
        assertEquals(reminder.remindAt, reminder.getComparableField("remind_at"))
        assertEquals(reminder.remindAt, reminder.getComparableField("remindAt"))
        assertEquals(reminder.createdAt, reminder.getComparableField("created_at"))
        assertEquals(reminder.createdAt, reminder.getComparableField("createdAt"))
        assertEquals(reminder.updatedAt, reminder.getComparableField("updated_at"))
        assertEquals(reminder.updatedAt, reminder.getComparableField("updatedAt"))
    }

    @Test
    fun `getComparableField should return null for unknown field`() {
        val reminder = randomMessageReminder()
        assertNull(reminder.getComparableField("unknownField"))
    }
}
