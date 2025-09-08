/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api2.model.dto.UnreadDto
import io.getstream.chat.android.client.parser2.testdata.UnreadDtoTestData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class UnreadDtoAdapterTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `deserialize JSON`() {
        val dto = parser.fromJson(UnreadDtoTestData.json, UnreadDto::class.java)
        assertEquals(UnreadDtoTestData.dto, dto)
    }

    @Test
    fun `serialize DTO`() {
        val json = parser.toJson(UnreadDtoTestData.dto)
        assertEquals(UnreadDtoTestData.json, json)
    }
}
