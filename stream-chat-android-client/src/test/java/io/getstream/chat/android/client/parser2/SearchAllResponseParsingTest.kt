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
import io.getstream.chat.android.network.models.SearchAllResponse
import io.getstream.chat.android.network.models.SearchAllSectionError
import io.getstream.chat.android.network.models.SearchAllWarning
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

internal class SearchAllResponseParsingTest {

    @Test
    fun `Deserialize a response with a failed section, a rejected cursor and an empty section`() {
        val response = Serializer.moshi.adapter(SearchAllResponse::class.java).fromJson(JSON)!!

        response.messages!!.error shouldBeEqualTo SearchAllSectionError(
            code = SearchAllSectionError.Code.BackendTimeout,
            message = "messages search timed out",
        )
        response.messages!!.results.shouldBeNull()
        response.users!!.warning shouldBeEqualTo SearchAllWarning(SearchAllWarning.Code.CursorRejected)
        response.users!!.users.shouldBeNull()
        response.channels!!.channels.shouldBeNull()
        response.channels!!.error.shouldBeNull()
    }

    @Test
    fun `Deserialize an error code added after this SDK version`() {
        val json = """{"messages":{"error":{"code":"new_code","message":"failed"}},"duration":"1ms"}"""

        val response = Serializer.moshi.adapter(SearchAllResponse::class.java).fromJson(json)!!

        response.messages!!.error!!.code shouldBeEqualTo SearchAllSectionError.Code.Unknown("new_code")
    }

    companion object {
        /**
         * The server leaves out empty fields, so a failed section has only its error and a section without results
         * is an empty object.
         */
        private const val JSON =
            """{
                "messages": {
                    "error": {
                        "code": "backend_timeout",
                        "message": "messages search timed out"
                    }
                },
                "users": {
                    "warning": {
                        "code": "cursor_rejected"
                    }
                },
                "channels": {},
                "duration": "3.21ms"
            }"""
    }
}
