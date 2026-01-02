/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.internal.offline.repository.database.database.converter.internal.ExtraDataConverter
import io.getstream.chat.android.client.test.utils.TestDataHelper
import io.getstream.chat.android.test.TestLoggingHelper
import io.getstream.chat.android.test.multiMapOf
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test

internal class ExtraDataConverterTest {

    val data = TestDataHelper()

    @Before
    fun setUp() {
        TestLoggingHelper.initialize()
    }

    @Test
    fun testNullEncoding() {
        val converter = ExtraDataConverter()
        val output = converter.mapToString(null)
        val converted = converter.stringToMap(output)
        converted shouldBeEqualTo mutableMapOf()
    }

    @Test
    fun testSortEncoding() {
        val converter = ExtraDataConverter()
        val output = converter.mapToString(data.extraData1)
        val converted = converter.stringToMap(output)
        converted shouldBeEqualTo data.extraData1
    }

    @Test
    fun testDeserializeDuplicateEntries() {
        val converter = ExtraDataConverter()
        val json = """
            {
                "name": "John",
                "name": "John"
            }
        """.trimIndent()
        val converted = converter.stringToMap(json)
        converted shouldBeEqualTo mapOf("name" to "John")
    }

    @Test
    fun testSerializeDuplicateEntries() {
        val converter = ExtraDataConverter()
        val map = multiMapOf(
            "name" to "John",
            "name" to "John",
        )
        map.size shouldBeEqualTo 2
        val converted = converter.mapToString(map)
        converted shouldBeEqualTo """
            {"name":"John"}
        """.trimIndent()
    }
}
