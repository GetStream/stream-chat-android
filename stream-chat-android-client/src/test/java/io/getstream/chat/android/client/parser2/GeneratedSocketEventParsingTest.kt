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
import io.getstream.chat.android.client.events.ConnectedEvent
import io.getstream.chat.android.client.events.HealthEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/** The socket hello and keep-alive, both `health.check` on the v1 socket, parsed through their generated models. */
internal class GeneratedSocketEventParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `The hello maps to a ConnectedEvent with the connection id and the current user`() {
        val event = parser.fromJson(HELLO, ChatEvent::class.java).shouldBeInstanceOf<ConnectedEvent>()

        event.type shouldBeEqualTo "health.check"
        event.connectionId shouldBeEqualTo "hello-connection-id"
        event.rawCreatedAt shouldBeEqualTo "2026-10-05T10:33:20.347926022Z"
        event.createdAt.time shouldBeEqualTo 1791196400347L
        event.me.id shouldBeEqualTo "jaewoong"
        event.me.totalUnreadCount shouldBeEqualTo 5
        event.me.unreadChannels shouldBeEqualTo 1
        event.me.privacySettings?.readReceipts?.enabled shouldBeEqualTo true
    }

    @Test
    fun `A keep-alive maps to a HealthEvent with the connection id`() {
        val event = parser.fromJson(KEEP_ALIVE, ChatEvent::class.java).shouldBeInstanceOf<HealthEvent>()

        event.connectionId shouldBeEqualTo "hello-connection-id"
        event.rawCreatedAt shouldBeEqualTo "2026-10-05T10:33:20.347926022Z"
    }

    @ParameterizedTest
    @ValueSource(strings = ["connection_id", "created_at"])
    fun `A hello or keep-alive without a required field is rejected`(field: String) {
        assertThrows<JsonDataException> { parser.fromJson(HELLO.without(field), ChatEvent::class.java) }
        assertThrows<JsonDataException> { parser.fromJson(KEEP_ALIVE.without(field), ChatEvent::class.java) }
    }

    @Test
    fun `A hello whose user misses a required field is rejected`() {
        val hello = parser.fromJson(HELLO, Map::class.java)

        @Suppress("UNCHECKED_CAST")
        val me = hello["me"] as Map<String, Any?>
        val json = parser.toJson(hello + ("me" to me.minus("total_unread_count")))

        assertThrows<JsonDataException> { parser.fromJson(json, ChatEvent::class.java) }
    }

    private fun String.without(field: String): String =
        parser.toJson(parser.fromJson(this, Map::class.java).minus(field))

    private companion object {
        /** The first frame of a v1 socket, recorded from the backend (devices dropped). */
        private const val HELLO = """
            {
              "type": "health.check", "cid": "*", "connection_id": "hello-connection-id",
              "created_at": "2026-10-05T10:33:20.347926022Z",
              "me": {
                "id": "jaewoong", "name": "C-3PO", "language": "ko", "role": "user", "teams": [],
                "created_at": "2021-10-21T21:58:10.425705Z", "updated_at": "2026-10-02T07:59:43.539826Z",
                "banned": false, "online": true, "last_active": "2026-10-05T10:33:20.332616722Z",
                "avg_response_time": 168808, "invisible": false, "devices": [], "mutes": [], "channel_mutes": [],
                "privacy_settings": {"typing_indicators": {"enabled": true}, "read_receipts": {"enabled": true}},
                "total_unread_count": 5, "unread_count": 5, "unread_channels": 1, "unread_threads": 0
              }
            }
        """

        /** A keep-alive recorded from the same socket. */
        private const val KEEP_ALIVE = """
            {
              "type": "health.check", "cid": "*", "connection_id": "hello-connection-id",
              "created_at": "2026-10-05T10:33:20.347926022Z"
            }
        """
    }
}
