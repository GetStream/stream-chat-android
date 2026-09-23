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

import io.getstream.chat.android.network.models.DraftPayloadResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class DraftPayloadResponseAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Parse a draft payload collecting root custom fields`() {
        val json = """
            {
              "id": "draft-id",
              "text": "hello",
              "silent": false,
              "show_in_channel": true,
              "command": "giphy",
              "args": "cat",
              "flair": "gold"
            }
        """.trimIndent()

        val payload = parser.fromJson(json, DraftPayloadResponse::class.java)

        payload.id shouldBeEqualTo "draft-id"
        payload.text shouldBeEqualTo "hello"
        payload.showInChannel shouldBeEqualTo true
        payload.custom shouldBeEqualTo mapOf("command" to "giphy", "args" to "cat", "flair" to "gold")
    }

    @Test
    fun `Parse a draft payload without custom fields`() {
        val json = """{"id":"draft-id","text":"hello"}"""

        val payload = parser.fromJson(json, DraftPayloadResponse::class.java)

        payload.custom shouldBeEqualTo emptyMap()
    }
}
