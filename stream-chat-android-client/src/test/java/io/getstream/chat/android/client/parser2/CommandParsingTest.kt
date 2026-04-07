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
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.CommandDto
import io.getstream.chat.android.client.parser2.event.CommandAdapter
import io.getstream.chat.android.client.parser2.testdata.CommandTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class CommandParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val commandAdapter = CommandAdapter()

    // region DTO path (JSON → CommandDto → Command)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(CommandTestData.jsonAllFields, CommandDto::class.java)
        val command = with(domainMapping) { dto.toDomain() }
        assertEquals(CommandTestData.expectedAllFields, command)
    }

    // endregion

    // region Direct path (JSON → Command via CommandAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val command = commandAdapter.fromJson(CommandTestData.jsonAllFields)
        assertEquals(CommandTestData.expectedAllFields, command)
    }

    // endregion

    // region Error message parity (both paths must throw identical errors)

    @Test
    fun `Both paths - same error message on missing name`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(CommandTestData.jsonMissingName, CommandDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            commandAdapter.fromJson(CommandTestData.jsonMissingName)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing description`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(CommandTestData.jsonMissingDescription, CommandDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            commandAdapter.fromJson(CommandTestData.jsonMissingDescription)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing args`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(CommandTestData.jsonMissingArgs, CommandDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            commandAdapter.fromJson(CommandTestData.jsonMissingArgs)
        }
        assertEquals(dtoException.message, directException.message)
    }

    @Test
    fun `Both paths - same error message on missing set`() {
        val dtoException = assertThrows<JsonDataException> {
            parser.fromJson(CommandTestData.jsonMissingSet, CommandDto::class.java)
        }
        val directException = assertThrows<JsonDataException> {
            commandAdapter.fromJson(CommandTestData.jsonMissingSet)
        }
        assertEquals(dtoException.message, directException.message)
    }

    // endregion
}
