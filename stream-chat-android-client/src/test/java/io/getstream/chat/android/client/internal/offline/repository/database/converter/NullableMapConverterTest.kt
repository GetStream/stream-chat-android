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

package io.getstream.chat.android.client.internal.offline.repository.database.converter

import io.getstream.chat.android.client.internal.offline.repository.database.converter.internal.NullableMapConverter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class NullableMapConverterTest {

    private val converter = NullableMapConverter()

    @Test
    fun `null round-trips as null`() {
        val encoded = converter.mapToString(null)
        val decoded = converter.stringToMap(encoded)

        assertNull(encoded)
        assertNull(decoded)
    }

    @Test
    fun `empty map round-trips as empty map`() {
        val encoded = converter.mapToString(emptyMap())
        val decoded = converter.stringToMap(encoded)

        assertNotNull(encoded)
        assertEquals(emptyMap<String, Any>(), decoded)
    }

    @Test
    fun `populated map round-trips with values preserved`() {
        val original = mapOf<String, Any>(
            "string" to "value",
            "int" to 42.0,
            "boolean" to true,
        )

        val encoded = converter.mapToString(original)
        val decoded = converter.stringToMap(encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `null and empty map are distinguishable after round-trip`() {
        val nullDecoded = converter.stringToMap(converter.mapToString(null))
        val emptyDecoded = converter.stringToMap(converter.mapToString(emptyMap()))

        assertNull(nullDecoded)
        assertEquals(emptyMap<String, Any>(), emptyDecoded)
    }

    @Test
    fun `empty string decodes to null`() {
        assertNull(converter.stringToMap(""))
    }

    @Test
    fun `literal null string decodes to null`() {
        assertNull(converter.stringToMap("null"))
    }
}
