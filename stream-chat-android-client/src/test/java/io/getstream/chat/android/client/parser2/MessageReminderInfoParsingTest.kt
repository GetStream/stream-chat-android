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

package io.getstream.chat.android.client.parser2

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.parser2.direct.MessageReminderInfoAdapter
import io.getstream.chat.android.client.parser2.testdata.MessageReminderInfoTestData
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class MessageReminderInfoParsingTest {

    private val moshi = Moshi.Builder().add(IsoDateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)
    private val adapter = MessageReminderInfoAdapter(dateAdapter)

    // region Direct path (JSON → MessageReminderInfo via MessageReminderInfoAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(MessageReminderInfoTestData.jsonAllFields)
        assertEquals(MessageReminderInfoTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(MessageReminderInfoTestData.jsonOptionalFieldsMissing)
        assertEquals(MessageReminderInfoTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Errors

    @Test
    fun `Direct path - rejects a reminder without created_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(MessageReminderInfoTestData.jsonMissingCreatedAt)
        }
    }

    @Test
    fun `Direct path - rejects a reminder without updated_at`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(MessageReminderInfoTestData.jsonMissingUpdatedAt)
        }
    }

    // endregion
}
