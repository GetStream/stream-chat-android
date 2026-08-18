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

import io.getstream.chat.android.network.models.UserResponse
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test

internal class UserResponseAdapterTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Deserialize JSON user response and collect unknown root fields into custom`() {
        val user = parser.fromJson(JSON_WITH_CUSTOM, UserResponse::class.java)

        user.id shouldBeEqualTo "jc"
        user.role shouldBeEqualTo "admin"
        user.language shouldBeEqualTo "en"
        user.custom shouldBeEqualTo mapOf("nickname" to "Johnny", "level" to 42.0)
    }

    @Test
    fun `Deserialize JSON user response without custom fields yields empty custom`() {
        val user = parser.fromJson(JSON_WITHOUT_CUSTOM, UserResponse::class.java)

        user.id shouldBeEqualTo "jc"
        user.custom shouldBeEqualTo emptyMap()
    }

    @Test
    fun `Can't serialize user response`() {
        invoking {
            parser.toJson(parser.fromJson(JSON_WITHOUT_CUSTOM, UserResponse::class.java))
        }.shouldThrow(RuntimeException::class)
    }

    companion object {
        private const val JSON_WITHOUT_CUSTOM =
            """{
                "id": "jc",
                "role": "admin",
                "language": "en",
                "banned": false,
                "online": true,
                "created_at": "2020-06-10T11:04:31.000Z",
                "updated_at": "2020-06-10T11:04:31.000Z"
            }"""

        private const val JSON_WITH_CUSTOM =
            """{
                "id": "jc",
                "role": "admin",
                "language": "en",
                "banned": false,
                "online": true,
                "created_at": "2020-06-10T11:04:31.000Z",
                "updated_at": "2020-06-10T11:04:31.000Z",
                "nickname": "Johnny",
                "level": 42
            }"""
    }
}
