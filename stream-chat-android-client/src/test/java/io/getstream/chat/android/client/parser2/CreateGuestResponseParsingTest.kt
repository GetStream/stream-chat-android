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

        response.accessToken shouldBeEqualTo GUEST_TOKEN
        response.duration shouldBeEqualTo "12.65ms"
        response.user.id shouldBeEqualTo "guest-a9466182-3293-43f2-8d80-b3b9892e98d3-probe"
        response.user.name shouldBeEqualTo "Probe Guest"
        response.user.role shouldBeEqualTo "guest"
        response.user.language shouldBeEqualTo ""
        response.user.custom shouldBeEqualTo mapOf("nickname" to "Ghost")
    }

    companion object {
        /** Shaped like a JWT, since the real one is a credential and the parse only cares about the string. */
        private const val GUEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.redacted-payload.redacted-signature"

        /**
         * Captured from `POST /guest`, with the access token replaced and the guest id shortened. Note the
         * server sends an empty `language`, sends `name` at the root where the model declares it, and leaves
         * `nickname` next to it for the custom sweep to collect.
         */
        private const val GUEST_JSON =
            """{
                "user": {
                    "id": "guest-a9466182-3293-43f2-8d80-b3b9892e98d3-probe",
                    "name": "Probe Guest",
                    "language": "",
                    "role": "guest",
                    "teams": [],
                    "created_at": "2026-08-18T13:44:19.149154Z",
                    "updated_at": "2026-08-18T13:44:19.149154Z",
                    "banned": false,
                    "online": false,
                    "blocked_user_ids": [],
                    "nickname": "Ghost"
                },
                "access_token": "$GUEST_TOKEN",
                "duration": "12.65ms"
            }"""
    }
}
