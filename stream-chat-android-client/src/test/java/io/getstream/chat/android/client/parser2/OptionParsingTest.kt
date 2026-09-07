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
import io.getstream.chat.android.client.parser2.direct.OptionAdapter
import io.getstream.chat.android.client.parser2.testdata.OptionTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.models.PollOptionResponseData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class OptionParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val adapter = OptionAdapter()

    // region Response-model path (JSON → PollOptionResponseData → Option)

    @Test
    fun `Response path - deserializes all fields`() {
        val dto = parser.fromJson(OptionTestData.jsonAllFields, PollOptionResponseData::class.java)
        val domain = with(domainMapping) { dto.toOption() }
        assertEquals(OptionTestData.expectedAllFields, domain)
    }

    @Test
    fun `Response path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(OptionTestData.jsonOptionalFieldsMissing, PollOptionResponseData::class.java)
        val domain = with(domainMapping) { dto.toOption() }
        assertEquals(OptionTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → Option via OptionAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(OptionTestData.jsonAllFields)
        assertEquals(OptionTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(OptionTestData.jsonOptionalFieldsMissing)
        assertEquals(OptionTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `Response path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(OptionTestData.jsonMissingId, PollOptionResponseData::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(OptionTestData.jsonMissingId)
        }
    }

    @Test
    fun `Response path - throws on missing text`() {
        assertThrows<JsonDataException> {
            parser.fromJson(OptionTestData.jsonMissingText, PollOptionResponseData::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing text`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(OptionTestData.jsonMissingText)
        }
    }

    // endregion
}
