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
import io.getstream.chat.android.network.models.GetPinnedMessagesResponse
import io.getstream.chat.android.network.models.GetRepliesResponse
import io.getstream.chat.android.network.models.SendReactionResponse
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** The replies, pinned messages and send reaction responses parsed through their generated models. */
internal class MessageListResponseParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    @Test
    fun `Replies parse their messages`() {
        val response = parser.fromJson("""{ "duration": "1ms", "messages": [$MESSAGE] }""", GetRepliesResponse::class.java)

        response.messages.map { it.id } shouldBeEqualTo listOf("message-id")
    }

    @Test
    fun `Pinned messages parse their messages`() {
        val response = parser.fromJson(
            """{ "duration": "1ms", "messages": [$MESSAGE] }""",
            GetPinnedMessagesResponse::class.java,
        )

        response.messages.map { it.id } shouldBeEqualTo listOf("message-id")
    }

    @Test
    fun `A send reaction response parses its reaction`() {
        val response = parser.fromJson(
            """{ "duration": "1ms", "message": $MESSAGE, "reaction": $REACTION }""",
            SendReactionResponse::class.java,
        )

        response.reaction.type shouldBeEqualTo "like"
        response.reaction.messageId shouldBeEqualTo "message-id"
    }

    @Test
    fun `A response without a duration is rejected`() {
        assertThrows<JsonDataException> {
            parser.fromJson("""{ "messages": [$MESSAGE] }""", GetRepliesResponse::class.java)
        }
        assertThrows<JsonDataException> {
            parser.fromJson("""{ "messages": [$MESSAGE] }""", GetPinnedMessagesResponse::class.java)
        }
    }

    @Test
    fun `A send reaction response without its message is rejected`() {
        assertThrows<JsonDataException> {
            parser.fromJson("""{ "duration": "1ms", "reaction": $REACTION }""", SendReactionResponse::class.java)
        }
    }

    private companion object {
        private const val USER = """
            {
              "id": "jaewoong", "role": "user", "language": "en", "banned": false, "online": false,
              "created_at": "2026-09-22T10:00:00.000Z", "updated_at": "2026-09-22T10:00:00.000Z"
            }
        """

        private const val MESSAGE = """
            {
              "id": "message-id", "cid": "messaging:general", "text": "hello", "html": "<p>hello</p>",
              "type": "regular", "created_at": "2026-09-22T10:00:00.000Z", "updated_at": "2026-09-22T10:00:00.000Z",
              "mentioned_channel": false, "mentioned_here": false, "pinned": false, "shadowed": false,
              "silent": false, "reply_count": 0, "deleted_reply_count": 0, "user": $USER
            }
        """

        private const val REACTION = """
            {
              "type": "like", "message_id": "message-id", "user_id": "jaewoong", "score": 1,
              "created_at": "2026-09-22T10:00:00.000Z", "updated_at": "2026-09-22T10:00:00.000Z", "user": $USER
            }
        """
    }
}
