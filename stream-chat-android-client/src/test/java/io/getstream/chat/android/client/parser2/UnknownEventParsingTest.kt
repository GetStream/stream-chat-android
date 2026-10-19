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

import com.squareup.moshi.JsonDataException
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.UnknownEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Events of a type the SDK doesn't know, such as an app's custom events, and the user they carry. */
internal class UnknownEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private fun parse(json: String) = parser.fromJson(json, ChatEvent::class.java) as UnknownEvent

    @Test
    fun `A custom event keeps its payload and maps its user like other event users`() {
        val event = parse(customEvent(USER))

        event.type shouldBeEqualTo "friendship_request"
        event.rawCreatedAt shouldBeEqualTo "2026-10-13T07:49:27.355584826Z"
        event.rawData["text"] shouldBeEqualTo "hi"
        event.rawData["score"] shouldBeEqualTo 3.0
        val user = event.user!!
        user.id shouldBeEqualTo "filip"
        user.name shouldBeEqualTo "Chewbacca"
        user.role shouldBeEqualTo "user"
        user.online shouldBeEqualTo false
        user.extraData shouldBeEqualTo mapOf("nickname" to "Chewie")
    }

    @Test
    fun `A custom event whose user doesn't read as the generated user is rejected, as other events are`() {
        assertThrows<JsonDataException> { parse(customEvent("""{"id":"filip"}""")) }
    }

    @Test
    fun `A custom event without a user has none`() {
        parse("""{"type":"friendship_request","created_at":"2026-10-13T07:49:27.355584826Z"}""").user.shouldBeNull()
    }

    private companion object {
        /** A user as the backend sends it on a custom event, with one custom field. */
        private const val USER = """{"id":"filip","name":"Chewbacca","image":"https://example.com/chewie.png",
            "language":"","role":"user","teams":[],"created_at":"2021-10-21T21:58:10.403855Z",
            "updated_at":"2026-10-12T13:08:19.684936Z","banned":false,"online":false,
            "last_active":"2026-10-13T07:49:19.429513146Z","blocked_user_ids":[],"custom":{"nickname":"Chewie"}}"""

        private fun customEvent(user: String) = """{"type":"friendship_request","cid":"messaging:general",
            "channel_id":"general","channel_type":"messaging","user":$user,
            "created_at":"2026-10-13T07:49:27.355584826Z","custom":{"text":"hi","score":3}}"""
    }
}
