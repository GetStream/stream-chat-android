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
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.events.UserUpdatedEvent
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.User
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test

/**
 * Parses whole event payloads, so the event-user adapters, their keep-set and the devices promotion are
 * exercised end to end rather than through models built directly.
 */
internal class EventUserParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    // Keys that only reach the user as flattened custom data on the wire.
    private val customUserKeys = """
        "deleted_at": "2020-06-29T06:14:28.000Z",
        "revoke_tokens_issued_before": "2020-06-29T06:14:28.000Z",
        "flair": "gold",
        "devices": [
          { "id": "token-1", "push_provider": "firebase", "push_provider_name": "chat-android-firebase" }
        ]
    """.trimIndent()

    private fun userJson(extra: String = "") = """
        {
          "id": "bender",
          "role": "user",
          "language": "en",
          "created_at": "2020-06-29T06:14:28.000Z",
          "updated_at": "2020-06-29T06:14:28.000Z",
          "banned": false,
          "online": true,
          $customUserKeys
          $extra
        }
    """.trimIndent()

    @Test
    fun `A user event keeps the legacy keys in extraData and reads devices as typed devices`() {
        val json = """
            {
              "type": "user.watching.start",
              "created_at": "2020-06-29T06:14:28.000Z",
              "cid": "messaging:general",
              "channel_type": "messaging",
              "channel_id": "general",
              "watcher_count": 1,
              "user": ${userJson()}
            }
        """.trimIndent()

        val event = parser.fromJson(json, ChatEvent::class.java)

        event.shouldBeInstanceOf<UserStartWatchingEvent>()
        assertCustomHandling((event as UserStartWatchingEvent).user)
    }

    @Test
    fun `user updated keeps the legacy keys in extraData and reads devices as typed devices`() {
        val json = """
            {
              "type": "user.updated",
              "created_at": "2020-06-29T06:14:28.000Z",
              "user": ${userJson(""", "invisible": true""")}
            }
        """.trimIndent()

        val event = parser.fromJson(json, ChatEvent::class.java)

        event.shouldBeInstanceOf<UserUpdatedEvent>()
        val user = (event as UserUpdatedEvent).user
        assertCustomHandling(user)
        user.invisible shouldBeEqualTo true
    }

    private fun assertCustomHandling(user: User) {
        user.language shouldBeEqualTo "en"
        user.devices shouldBeEqualTo listOf(
            Device(token = "token-1", pushProvider = PushProvider.FIREBASE, providerName = "chat-android-firebase"),
        )
        // Declared on the generated models but kept in extraData, since the domain has no property for them.
        user.extraData.keys shouldBeEqualTo setOf("deleted_at", "revoke_tokens_issued_before", "flair")
        user.extraData["flair"] shouldBeEqualTo "gold"
    }
}
