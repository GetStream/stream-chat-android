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
import io.getstream.chat.android.client.api2.model.dto.DownstreamLocationDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.LocationAdapter
import io.getstream.chat.android.client.parser2.testdata.LocationTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class LocationParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val moshi = Moshi.Builder().add(DateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)
    private val adapter = LocationAdapter(dateAdapter)

    // region DTO path (JSON → DownstreamLocationDto → Location)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(LocationTestData.jsonAllFields, DownstreamLocationDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(LocationTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(LocationTestData.jsonOptionalFieldsMissing, DownstreamLocationDto::class.java)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(LocationTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Direct path (JSON → Location via LocationAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = adapter.fromJson(LocationTestData.jsonAllFields)
        assertEquals(LocationTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = adapter.fromJson(LocationTestData.jsonOptionalFieldsMissing)
        assertEquals(LocationTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Error message parity

    @Test
    fun `DTO path - throws on missing channel_cid`() {
        assertThrows<JsonDataException> {
            parser.fromJson(LocationTestData.jsonMissingChannelCid, DownstreamLocationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing channel_cid`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(LocationTestData.jsonMissingChannelCid)
        }
    }

    @Test
    fun `DTO path - throws on missing message_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(LocationTestData.jsonMissingMessageId, DownstreamLocationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing message_id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(LocationTestData.jsonMissingMessageId)
        }
    }

    @Test
    fun `DTO path - throws on missing user_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(LocationTestData.jsonMissingUserId, DownstreamLocationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing user_id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(LocationTestData.jsonMissingUserId)
        }
    }

    @Test
    fun `DTO path - throws on missing latitude`() {
        assertThrows<JsonDataException> {
            parser.fromJson(LocationTestData.jsonMissingLatitude, DownstreamLocationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing latitude`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(LocationTestData.jsonMissingLatitude)
        }
    }

    @Test
    fun `DTO path - throws on missing longitude`() {
        assertThrows<JsonDataException> {
            parser.fromJson(LocationTestData.jsonMissingLongitude, DownstreamLocationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing longitude`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(LocationTestData.jsonMissingLongitude)
        }
    }

    @Test
    fun `DTO path - throws on missing created_by_device_id`() {
        assertThrows<JsonDataException> {
            parser.fromJson(LocationTestData.jsonMissingCreatedByDeviceId, DownstreamLocationDto::class.java)
        }
    }

    @Test
    fun `Direct path - throws on missing created_by_device_id`() {
        assertThrows<JsonDataException> {
            adapter.fromJson(LocationTestData.jsonMissingCreatedByDeviceId)
        }
    }

    // endregion
}
