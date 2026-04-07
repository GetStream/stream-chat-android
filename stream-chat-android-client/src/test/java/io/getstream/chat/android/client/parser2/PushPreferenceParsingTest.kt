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

import com.squareup.moshi.Moshi
import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.api2.model.dto.DownstreamPushPreferenceDto
import io.getstream.chat.android.client.parser2.adapters.DateAdapter
import io.getstream.chat.android.client.parser2.event.PushPreferenceAdapter
import io.getstream.chat.android.client.parser2.testdata.PushPreferenceTestData
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

internal class PushPreferenceParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private val moshi = Moshi.Builder().add(DateAdapter()).build()
    private val pushPreferenceAdapter = PushPreferenceAdapter(
        dateAdapter = moshi.adapter(Date::class.java),
    )

    // region DTO path (JSON → DownstreamPushPreferenceDto → PushPreference)

    @Test
    fun `DTO path - deserializes all fields`() {
        val dto = parser.fromJson(PushPreferenceTestData.jsonAllFields, DownstreamPushPreferenceDto::class.java)
        val pushPreference = with(domainMapping) { dto.toDomain() }
        assertEquals(PushPreferenceTestData.expectedAllFields, pushPreference)
    }

    @Test
    fun `DTO path - deserializes with optional fields missing`() {
        val dto = parser.fromJson(PushPreferenceTestData.jsonOptionalFieldsMissing, DownstreamPushPreferenceDto::class.java)
        val pushPreference = with(domainMapping) { dto.toDomain() }
        assertEquals(PushPreferenceTestData.expectedOptionalFieldsMissing, pushPreference)
    }

    // endregion

    // region Direct path (JSON → PushPreference via PushPreferenceAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val pushPreference = pushPreferenceAdapter.fromJson(PushPreferenceTestData.jsonAllFields)
        assertEquals(PushPreferenceTestData.expectedAllFields, pushPreference)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val pushPreference = pushPreferenceAdapter.fromJson(PushPreferenceTestData.jsonOptionalFieldsMissing)
        assertEquals(PushPreferenceTestData.expectedOptionalFieldsMissing, pushPreference)
    }

    // endregion
}
