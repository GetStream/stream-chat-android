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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.ReminderInfoConverter
import io.getstream.chat.android.client.internal.offline.repository.domain.message.internal.ReminderInfoEntity
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomDateOrNull
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

/**
 * Runs under Robolectric because the dates are serialised by the API 26+ formatter, which is stricter about the year
 * than the [java.text.SimpleDateFormat] fallback used below it.
 */
@RunWith(AndroidJUnit4::class)
internal class ReminderInfoConverterTest {

    private val converter = ReminderInfoConverter()

    @Test
    fun `a reminder built from the date fixtures survives the round trip`() {
        repeat(ROUNDS) {
            val reminder = ReminderInfoEntity(
                remindAt = randomDateOrNull(),
                createdAt = randomDate(),
                updatedAt = randomDate(),
            )

            converter.stringToReminderInfo(converter.reminderInfoToString(reminder)) shouldBeEqualTo reminder
        }
    }

    @Test
    fun `a reminder at the last serializable instant survives the round trip`() {
        // Guards the upper bound the date fixtures generate up to: past it the year no longer fits the wire format,
        // so a reminder comes back holding a different date, or cannot be read back at all.
        val lastSerializable = Date(LAST_SERIALIZABLE_DATE_MILLIS)
        val reminder = ReminderInfoEntity(
            remindAt = lastSerializable,
            createdAt = lastSerializable,
            updatedAt = lastSerializable,
        )

        converter.stringToReminderInfo(converter.reminderInfoToString(reminder)) shouldBeEqualTo reminder
    }

    private companion object {
        // Enough draws that a fixture generating unserializable dates fails here, rather than intermittently
        // somewhere downstream.
        const val ROUNDS = 5_000
        const val LAST_SERIALIZABLE_DATE_MILLIS = 253_402_300_799_999L // 9999-12-31T23:59:59.999Z
    }
}
