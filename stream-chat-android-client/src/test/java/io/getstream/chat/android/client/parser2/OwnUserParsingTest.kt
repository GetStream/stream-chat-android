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

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.events.ConnectedEvent
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

/**
 * Parses a whole connection event, so the own-user adapter and its keep-set are exercised end to end
 * rather than through a model built directly.
 */
internal class OwnUserParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `The own user keeps the legacy keys in extraData and parses its devices`() {
        val json = """
            {
              "type": "health.check",
              "created_at": "2020-06-29T06:14:28.000Z",
              "connection_id": "connection-1",
              "me": {
                "id": "bender",
                "role": "user",
                "language": "en",
                "created_at": "2020-06-29T06:14:28.000Z",
                "updated_at": "2020-06-29T06:14:28.000Z",
                "banned": false,
                "online": true,
                "invisible": false,
                "total_unread_count": 3,
                "unread_channels": 2,
                "unread_count": 3,
                "unread_threads": 1,
                "devices": [
                  {
                    "id": "token-1",
                    "push_provider": "firebase",
                    "created_at": "2020-06-29T06:14:28.000Z",
                    "user_id": "bender"
                  }
                ],
                "deleted_at": "2020-06-29T06:14:28.000Z",
                "latest_hidden_channels": ["messaging:hidden"],
                "revoke_tokens_issued_before": "2020-06-29T06:14:28.000Z",
                "total_unread_count_by_team": { "red": 3 },
                "flair": "gold"
              }
            }
        """.trimIndent()

        val event = parser.fromJson(json, ChatEvent::class.java)

        event.shouldBeInstanceOf<ConnectedEvent>()
        val me = (event as ConnectedEvent).me
        me.language shouldBeEqualTo "en"
        me.unreadThreads shouldBeEqualTo 1
        me.devices.map { it.token } shouldBeEqualTo listOf("token-1")
        // Declared on OwnUserResponse but kept in extraData, since the domain has no property for them.
        me.extraData.keys shouldBeEqualTo setOf(
            "deleted_at",
            "latest_hidden_channels",
            "revoke_tokens_issued_before",
            "total_unread_count_by_team",
            "flair",
        )
    }
}
