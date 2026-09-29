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

import io.getstream.chat.android.network.models.MessageResponse
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.junit.jupiter.api.Test

internal class MessageResponseParsingTest {
    private val parser = ParserFactory.createMoshiChatParser()

    private fun message(extra: String) = parser.fromJson(
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
        MessageResponse::class.java,
    )

    @Test
    fun `Keys the hand-written DTO did not declare stay in custom as well as being typed`() {
        val parsed = message(
            """,
              "mml": "<mml/>",
              "poll_id": "poll-1",
              "restricted_visibility": ["jaewoong"],
              "mentioned_group_ids": ["group-1"],
              "image_labels": {"cat": ["animal"]}
            """.trimIndent(),
        )

        parsed.mml shouldBeEqualTo "<mml/>"
        parsed.pollId shouldBeEqualTo "poll-1"
        parsed.restrictedVisibility shouldBeEqualTo listOf("jaewoong")

        // Declaring these keys would otherwise drop them from Message.extraData.
        parsed.custom.keys shouldContain "mml"
        parsed.custom.keys shouldContain "poll_id"
        parsed.custom.keys shouldContain "restricted_visibility"
        parsed.custom.keys shouldContain "mentioned_group_ids"
        parsed.custom.keys shouldContain "image_labels"
    }

    @Test
    fun `Undeclared root keys are collected into custom`() {
        val parsed = message(""", "moderation_details": {"action": "MESSAGE_RESPONSE_ACTION_BOUNCE"}, "flair": "gold"""")

        parsed.custom["flair"] shouldBeEqualTo "gold"
        parsed.custom.keys shouldContain "moderation_details"
    }
}
