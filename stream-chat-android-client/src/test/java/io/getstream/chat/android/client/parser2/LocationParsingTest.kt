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
import io.getstream.chat.android.client.parser2.testdata.LocationTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.SharedLocationResponseData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class LocationParsingTest {

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    // region DTO path (JSON → SharedLocationResponseData → Location)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parse<SharedLocationResponseData>(LocationTestData.jsonAllFields)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(LocationTestData.expectedAllFields, domain)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parse<SharedLocationResponseData>(LocationTestData.jsonOptionalFieldsMissing)
        val domain = with(domainMapping) { dto.toDomain() }
        assertEquals(LocationTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Errors

    @Test
    fun `DTO path - throws on missing channel_cid`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingChannelCid)
        }
    }

    @Test
    fun `DTO path - throws on missing message_id`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingMessageId)
        }
    }

    @Test
    fun `DTO path - throws on missing user_id`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingUserId)
        }
    }

    @Test
    fun `DTO path - throws on missing latitude`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingLatitude)
        }
    }

    @Test
    fun `DTO path - throws on missing longitude`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingLongitude)
        }
    }

    @Test
    fun `DTO path - throws on missing created_by_device_id`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingCreatedByDeviceId)
        }
    }

    @Test
    fun `DTO path - throws on missing created_at`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingCreatedAt)
        }
    }

    @Test
    fun `DTO path - throws on missing updated_at`() {
        assertThrows<JsonDataException> {
            parse<SharedLocationResponseData>(LocationTestData.jsonMissingUpdatedAt)
        }
    }

    // endregion
}
