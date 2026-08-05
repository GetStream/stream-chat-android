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

import io.getstream.chat.android.network.models.CreateGuestResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class CreateGuestResponseParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize guest response and map the embedded user with custom fields`() {
        val response = parser.fromJson(GUEST_JSON, CreateGuestResponse::class.java)

        response.accessToken shouldBeEqualTo "guest-token"
        response.user.id shouldBeEqualTo "guest-123"
        response.user.role shouldBeEqualTo "guest"
        response.user.language shouldBeEqualTo "en"
        response.user.custom shouldBeEqualTo mapOf("nickname" to "Ghost")
    }

    companion object {
        private const val GUEST_JSON =
            """{
                "access_token": "guest-token",
                "duration": "12ms",
                "user": {
                    "id": "guest-123",
                    "role": "guest",
                    "language": "en",
                    "banned": false,
                    "online": false,
                    "created_at": "2020-06-10T11:04:31.000Z",
                    "updated_at": "2020-06-10T11:04:31.000Z",
                    "nickname": "Ghost"
                }
            }"""
    }
}
