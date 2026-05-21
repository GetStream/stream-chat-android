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
import io.getstream.chat.android.client.api2.model.dto.ChannelInfoDto
import io.getstream.chat.android.client.parser2.direct.ChannelInfoAdapter
import io.getstream.chat.android.client.parser2.testdata.ChannelInfoTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ChannelInfoParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val channelInfoAdapter = ChannelInfoAdapter()

    // region DTO path (JSON → ChannelInfoDto → ChannelInfo)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(ChannelInfoTestData.jsonAllFields, ChannelInfoDto::class.java)
        val channelInfo = with(domainMapping) { dto.toDomain() }
        assertEquals(ChannelInfoTestData.expectedAllFields, channelInfo)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(ChannelInfoTestData.jsonOptionalFieldsMissing, ChannelInfoDto::class.java)
        val channelInfo = with(domainMapping) { dto.toDomain() }
        assertEquals(ChannelInfoTestData.expectedOptionalFieldsMissing, channelInfo)
    }

    // endregion

    // region Direct path (JSON → ChannelInfo via ChannelInfoAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val channelInfo = channelInfoAdapter.fromJson(ChannelInfoTestData.jsonAllFields)
        assertEquals(ChannelInfoTestData.expectedAllFields, channelInfo)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val channelInfo = channelInfoAdapter.fromJson(ChannelInfoTestData.jsonOptionalFieldsMissing)
        assertEquals(ChannelInfoTestData.expectedOptionalFieldsMissing, channelInfo)
    }

    // endregion

    // region Explicit null values

    @Test
    fun `DTO path - deserializes with explicit null values`() {
        val dto = parser.fromJson(ChannelInfoTestData.jsonWithExplicitNulls, ChannelInfoDto::class.java)
        val channelInfo = with(domainMapping) { dto.toDomain() }
        assertEquals(ChannelInfoTestData.expectedWithExplicitNulls, channelInfo)
    }

    @Test
    fun `Direct path - deserializes with explicit null values`() {
        val channelInfo = channelInfoAdapter.fromJson(ChannelInfoTestData.jsonWithExplicitNulls)
        assertEquals(ChannelInfoTestData.expectedWithExplicitNulls, channelInfo)
    }

    // endregion
}
