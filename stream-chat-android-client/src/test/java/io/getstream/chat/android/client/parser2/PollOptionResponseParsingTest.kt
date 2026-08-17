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

import io.getstream.chat.android.network.models.PollOptionResponse
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class PollOptionResponseParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize a poll option response`() {
        val response = parser.fromJson(POLL_OPTION_JSON, PollOptionResponse::class.java)

        response.duration shouldBeEqualTo "12ms"
        response.pollOption.id shouldBeEqualTo "optionId"
        response.pollOption.text shouldBeEqualTo "Option text"
    }

    @Test
    fun `Collect the root-level custom fields of a poll option`() {
        val response = parser.fromJson(POLL_OPTION_JSON, PollOptionResponse::class.java)

        response.pollOption.custom shouldBeEqualTo mapOf("sentiment" to "positive")
    }

    @Test
    fun `Deserialize a poll option without custom fields`() {
        val response = parser.fromJson(POLL_OPTION_WITHOUT_CUSTOM_JSON, PollOptionResponse::class.java)

        response.pollOption.custom.shouldBeEmpty()
    }

    companion object {
        private const val POLL_OPTION_JSON =
            """{
                "duration": "12ms",
                "poll_option": {
                    "id": "optionId",
                    "text": "Option text",
                    "sentiment": "positive"
                }
            }"""

        private const val POLL_OPTION_WITHOUT_CUSTOM_JSON =
            """{
                "duration": "12ms",
                "poll_option": {
                    "id": "optionId",
                    "text": "Option text"
                }
            }"""
    }
}
