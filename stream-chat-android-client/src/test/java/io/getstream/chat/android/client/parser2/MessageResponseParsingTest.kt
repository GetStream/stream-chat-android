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

import io.getstream.chat.android.client.api2.mapping.DomainMapping
import io.getstream.chat.android.client.parser2.testdata.MessageFixtures
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.network.infrastructure.Serializer
import io.getstream.chat.android.network.models.MessageResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MessageResponseParsingTest {
    private inline fun <reified T> parse(json: String): T = Serializer.moshi.adapter(T::class.java).fromJson(json)!!

    private val mapping = DomainMapping(
        currentUserIdProvider = { "user-1" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    private fun message(extra: String) = parse<MessageResponse>(
        """
        {
          "id": "message-id",
          "cid": "messaging:general",
          "text": "hello",
          "html": "<p>hello</p>",
          "type": "regular",
          "created_at": "2026-09-22T10:00:00.000Z",
          "updated_at": "2026-09-22T10:00:00.000Z",
          "mentioned_channel": false,
          "mentioned_here": false,
          "pinned": false,
          "shadowed": false,
          "silent": false,
          "reply_count": 0,
          "deleted_reply_count": 0,
          "user": {
            "id": "jaewoong", "role": "user", "language": "en", "banned": false, "online": false,
            "created_at": "2026-09-22T10:00:00.000Z", "updated_at": "2026-09-22T10:00:00.000Z"
          }
          $extra
        }
        """.trimIndent(),
    )

    @Test
    fun `A message by a deleted user parses with the placeholder the backend sends`() {
        val message = with(mapping) { parse<MessageResponse>(MessageFixtures.DELETED_AUTHOR_JSON).toDomain() }

        assertEquals("deleted-user", message.user.id)
        assertEquals("", message.user.language)
        // The exact instant depends on the calendar the date parser uses for year 1; it only has to parse.
        assertTrue((message.user.createdAt?.time ?: 0) < 0) { "createdAt = ${message.user.createdAt}" }
    }
}
