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
import io.getstream.chat.android.client.parser2.testdata.DeviceTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.CreateDeviceRequest
import io.getstream.chat.android.network.models.DeviceResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class DeviceParsingTest {

    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private inline fun <reified T> toJson(value: T): String = Serializer.moshi.adapter(T::class.java).toJson(value)

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    // region DTO path (JSON → DeviceResponse → Device)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parse<DeviceResponse>(DeviceTestData.jsonAllFields)
        val device = with(domainMapping) { dto.toDomain() }
        assertEquals(DeviceTestData.expectedDeviceAllFields, device)
    }

    @Test
    fun `DTO path - deserializes with optional field missing`() {
        val dto = parse<DeviceResponse>(DeviceTestData.jsonOptionalFieldMissing)
        val device = with(domainMapping) { dto.toDomain() }
        assertEquals(DeviceTestData.expectedDeviceOptionalMissing, device)
    }

    // endregion

    // region Explicit null values

    @Test
    fun `DTO path - deserializes with explicit null values`() {
        val dto = parse<DeviceResponse>(DeviceTestData.jsonWithExplicitNulls)
        val device = with(domainMapping) { dto.toDomain() }
        assertEquals(DeviceTestData.expectedWithExplicitNulls, device)
    }

    // endregion

    // region Required fields

    @Test
    fun `DTO path - throws on missing id`() {
        assertThrows<JsonDataException> {
            parse<DeviceResponse>(DeviceTestData.jsonMissingId)
        }
    }

    @Test
    fun `DTO path - throws on missing push_provider`() {
        assertThrows<JsonDataException> {
            parse<DeviceResponse>(DeviceTestData.jsonMissingPushProvider)
        }
    }

    // endregion

    // region Request path (CreateDeviceRequest / PushProvider enum)

    @ParameterizedTest
    @MethodSource("pushProviders")
    fun `Request path - serializes each PushProvider to its wire string`(
        wire: String,
        provider: CreateDeviceRequest.PushProvider,
    ) {
        val json = toJson(CreateDeviceRequest(id = "token1", pushProvider = provider))
        assertEquals("""{"id":"token1","push_provider":"$wire"}""", json)
    }

    @ParameterizedTest
    @MethodSource("pushProviders")
    fun `Request path - deserializes each push_provider to the typed PushProvider`(
        wire: String,
        provider: CreateDeviceRequest.PushProvider,
    ) {
        val request = parse<CreateDeviceRequest>(
            """{"id":"token1","push_provider":"$wire"}""",
        )
        assertEquals(provider, request.pushProvider)
    }

    // endregion

    companion object {
        @JvmStatic
        fun pushProviders() = listOf(
            Arguments.of("firebase", CreateDeviceRequest.PushProvider.Firebase),
            Arguments.of("apn", CreateDeviceRequest.PushProvider.Apn),
            Arguments.of("huawei", CreateDeviceRequest.PushProvider.Huawei),
            Arguments.of("xiaomi", CreateDeviceRequest.PushProvider.Xiaomi),
            Arguments.of("newfangled", CreateDeviceRequest.PushProvider.Unknown("newfangled")),
        )
    }
}
