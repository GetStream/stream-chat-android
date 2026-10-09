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
import io.getstream.chat.android.client.parser2.direct.DeviceAdapter
import io.getstream.chat.android.client.parser2.direct.UserAdapter
import io.getstream.chat.android.client.parser2.testdata.UserTestData
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserTransformer
import io.getstream.chat.android.network.infrastructure.IsoDateAdapter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Date

internal class UserParsingTest {

    private val moshi = Moshi.Builder().add(IsoDateAdapter()).build()
    private val dateAdapter = moshi.adapter(Date::class.java)
    private val deviceAdapter = DeviceAdapter()
    private val userAdapter = UserAdapter(
        deviceAdapter = deviceAdapter,
        dateAdapter = dateAdapter,
        userTransformer = NoOpUserTransformer,
    )

    // region Direct path (JSON → User via UserAdapter)

    @Test
    fun `Direct path - deserializes all fields`() {
        val domain = userAdapter.fromJson(UserTestData.jsonAllFields)
        assertEquals(UserTestData.expectedAllFields, domain)
    }

    @Test
    fun `Direct path - deserializes with optional fields missing`() {
        val domain = userAdapter.fromJson(UserTestData.jsonOptionalFieldsMissing)
        assertEquals(UserTestData.expectedOptionalFieldsMissing, domain)
    }

    // endregion

    // region Explicit nulls (JSON with explicit null values)

    @Test
    fun `Direct path - deserializes with explicit nulls`() {
        val domain = userAdapter.fromJson(UserTestData.jsonWithExplicitNulls)
        assertEquals(UserTestData.expectedWithExplicitNulls, domain)
    }

    // endregion

    // region Required fields

    @Test
    fun `Direct path - throws on missing id`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingId)
        }
    }

    @Test
    fun `Direct path - throws on missing role`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingRole)
        }
    }

    @Test
    fun `Direct path - throws on missing banned`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingBanned)
        }
    }

    @Test
    fun `Direct path - throws on missing online`() {
        assertThrows<JsonDataException> {
            userAdapter.fromJson(UserTestData.jsonMissingOnline)
        }
    }

    // endregion

    // region Transformer

    @Test
    fun `Direct path - applies a custom UserTransformer`() {
        val customTransformer = UserTransformer { it.copy(name = it.name + " [transformed]") }
        val transformedUserAdapter = UserAdapter(
            deviceAdapter = deviceAdapter,
            dateAdapter = dateAdapter,
            userTransformer = customTransformer,
        )

        val directResult = transformedUserAdapter.fromJson(UserTestData.jsonAllFields)

        assertEquals(UserTestData.expectedAllFields.copy(name = "John Doe [transformed]"), directResult)
    }

    // endregion
}
