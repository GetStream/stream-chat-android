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
import io.getstream.chat.android.client.api2.model.dto.DeviceDto
import io.getstream.chat.android.client.parser2.event.DeviceAdapter
import io.getstream.chat.android.client.parser2.testdata.DeviceTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DeviceParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val deviceAdapter = DeviceAdapter()

    // region DTO path (JSON → DeviceDto → Device)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(DeviceTestData.jsonAllFields, DeviceDto::class.java)
        val device = with(domainMapping) { dto.toDomain() }
        assertEquals(DeviceTestData.expectedDeviceAllFields, device)
    }

    @Test
    fun `DTO path - deserializes with optional field missing`() {
        val dto = parser.fromJson(DeviceTestData.jsonOptionalFieldMissing, DeviceDto::class.java)
        val device = with(domainMapping) { dto.toDomain() }
        assertEquals(DeviceTestData.expectedDeviceOptionalMissing, device)
    }

    // endregion

    // region Direct path (JSON → Device via DeviceAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val device = deviceAdapter.fromJson(DeviceTestData.jsonAllFields)
        assertEquals(DeviceTestData.expectedDeviceAllFields, device)
    }

    @Test
    fun `Direct path - deserializes with optional field missing`() {
        val device = deviceAdapter.fromJson(DeviceTestData.jsonOptionalFieldMissing)
        assertEquals(DeviceTestData.expectedDeviceOptionalMissing, device)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(DeviceTestData.jsonMissingId, DeviceDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            deviceAdapter.fromJson(DeviceTestData.jsonMissingId)
        }
    }

    @Test
    fun `DTO path - throws on missing push_provider`() {
        assertThrows<JsonDataException> {
            parser.fromJson(DeviceTestData.jsonMissingPushProvider, DeviceDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing push_provider`() {
        assertThrows<JsonDataException> {
            deviceAdapter.fromJson(DeviceTestData.jsonMissingPushProvider)
        }
    }

    // endregion
}
