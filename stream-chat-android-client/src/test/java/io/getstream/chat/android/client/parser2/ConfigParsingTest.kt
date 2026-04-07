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
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.ConfigDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.CommandAdapter
import io.getstream.chat.android.client.parser2.event.ConfigAdapter
import io.getstream.chat.android.client.parser2.testdata.ConfigTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class ConfigParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val moshi = Moshi.Builder().add(DateAdapter()).build()
    private val configAdapter = ConfigAdapter(
        dateAdapter = moshi.adapter(Date::class.java),
        commandAdapter = CommandAdapter(),
    )

    // region DTO path (JSON → ConfigDto → Config)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ConfigTestData.jsonAllFields, ConfigDto::class.java)
        val config = with(domainMapping) { dto.toDomain() }
        assertEquals(ConfigTestData.expectedAllFields, config)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(ConfigTestData.jsonOptionalFieldsMissing, ConfigDto::class.java)
        val config = with(domainMapping) { dto.toDomain() }
        assertEquals(ConfigTestData.expectedOptionalFieldsMissing, config)
    }

    // endregion

    // region Direct path (JSON → Config via ConfigAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val config = configAdapter.fromJson(ConfigTestData.jsonAllFields)
        assertEquals(ConfigTestData.expectedAllFields, config)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val config = configAdapter.fromJson(ConfigTestData.jsonOptionalFieldsMissing)
        assertEquals(ConfigTestData.expectedOptionalFieldsMissing, config)
    }

    // endregion

    // region Error message parity — representative required fields (Boolean, String, Int, List)

    @Test
    fun `Both paths - same error message on missing typing_events`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ConfigTestData.jsonMissingTypingEvents, ConfigDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            configAdapter.fromJson(ConfigTestData.jsonMissingTypingEvents)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing message_retention`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ConfigTestData.jsonMissingMessageRetention, ConfigDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            configAdapter.fromJson(ConfigTestData.jsonMissingMessageRetention)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing max_message_length`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ConfigTestData.jsonMissingMaxMessageLength, ConfigDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            configAdapter.fromJson(ConfigTestData.jsonMissingMaxMessageLength)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing commands`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(ConfigTestData.jsonMissingCommands, ConfigDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            configAdapter.fromJson(ConfigTestData.jsonMissingCommands)
        }
        assertEquals(dtoException.message, directException.message)
    }

    // endregion
}
