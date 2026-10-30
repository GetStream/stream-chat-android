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

import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.CreateGuestResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test

internal class CreateGuestResponseParsingTest {
    @Test
    fun `Deserialize guest response and map the embedded user with custom fields`() {
        val response = Serializer.moshi.adapter(CreateGuestResponse::class.java).fromJson(GUEST_JSON)!!

        response.accessToken shouldBeEqualTo GUEST_TOKEN
        response.duration shouldBeEqualTo "8.13ms"
        response.user.id shouldBeEqualTo "guest-99752477-4c60-4c6f-84fd-a27a869561f1-probe"
        response.user.name shouldBeEqualTo "Probe Guest"
        response.user.role shouldBeEqualTo "guest"
        response.user.language shouldBeEqualTo ""
        response.user.custom shouldBeEqualTo mapOf("nickname" to "Ghost")
    }

    companion object {
        /** Shaped like a JWT, since the real one is a credential and the parse only cares about the string. */
        private const val GUEST_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.redacted-payload.redacted-signature"

        /**
         * Captured from `POST /api/v2/guest`, with the access token replaced and the guest id shortened. Note the
         * server sends an empty `language`, `name` at the root where the model declares it, and `nickname` under
         * `custom`.
         */
        private const val GUEST_JSON =
            """{
                "user": {
                    "id": "guest-99752477-4c60-4c6f-84fd-a27a869561f1-probe",
                    "name": "Probe Guest",
                    "custom": {
                        "nickname": "Ghost"
                    },
                    "language": "",
                    "role": "guest",
                    "teams": [],
                    "created_at": 1793188343331121000,
                    "updated_at": 1793188343331121000,
                    "banned": false,
                    "online": false,
                    "blocked_user_ids": []
                },
                "access_token": "$GUEST_TOKEN",
                "duration": "8.13ms"
            }"""
    }
}
