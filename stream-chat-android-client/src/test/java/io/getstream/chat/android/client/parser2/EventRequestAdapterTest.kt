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

import io.getstream.chat.android.network.models.EventRequest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

internal class EventRequestAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Serialize EventRequest with custom fields flattened to root`() {
        val request = EventRequest(
            type = "typing.start",
            custom = mapOf("customKey" to "customValue"),
        )
        val json = parser.toJson(request)
        Assertions.assertEquals(
            """{"type":"typing.start","customKey":"customValue"}""",
            json,
        )
    }

    @Test
    fun `Serialize EventRequest without custom fields`() {
        val request = EventRequest(type = "typing.start", custom = emptyMap())
        val json = parser.toJson(request)
        Assertions.assertEquals("""{"type":"typing.start"}""", json)
    }
}
