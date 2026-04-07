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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamModerationDetailsDto
import io.getstream.chat.android.client.parser2.event.MessageModerationDetailsAdapter
import io.getstream.chat.android.client.parser2.testdata.MessageModerationDetailsTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MessageModerationDetailsParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val adapter = MessageModerationDetailsAdapter()

    // region DTO path (JSON → DownstreamModerationDetailsDto → MessageModerationDetails)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(MessageModerationDetailsTestData.jsonAllFields, DownstreamModerationDetailsDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(MessageModerationDetailsTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(MessageModerationDetailsTestData.jsonOptionalFieldsMissing, DownstreamModerationDetailsDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(MessageModerationDetailsTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → MessageModerationDetails via MessageModerationDetailsAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(MessageModerationDetailsTestData.jsonAllFields)
        assertEquals(MessageModerationDetailsTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(MessageModerationDetailsTestData.jsonOptionalFieldsMissing)
        assertEquals(MessageModerationDetailsTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion
}
