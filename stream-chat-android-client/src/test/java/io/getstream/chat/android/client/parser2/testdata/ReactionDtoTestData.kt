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

package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.network.models.ReactionRequest
import io.getstream.chat.android.network.models.ReactionResponse
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ReactionDtoTestData {

    /**
     * The wire always sends both timestamps and a full user for a reaction, so the generated model
     * declares them non-null.
     */
    @Language("JSON")
    val reactionResponseJson =
        """{
          "message_id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "score": 0,
          "type": "like",
          "user": ${UserDtoTestData.userResponseJson},
          "user_id": "",
          "created_at": "2020-06-10T11:04:31.000Z",
          "updated_at": "2020-06-10T11:04:31.588Z"
        }""".withoutWhitespace()

    val reactionResponse = ReactionResponse(
        messageId = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 0,
        user = UserDtoTestData.userResponse,
        userId = "",
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
    )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
          "score": 0,
          "type": "love"
        }""".withoutWhitespace()
    val upstreamReactionWithoutExtraData = ReactionRequest(
        type = "love",
        createdAt = null,
        score = 0,
        updatedAt = null,
        custom = emptyMap(),
    )

    @Language("JSON")
    val upstreamJson =
        """{
          "created_at": "2020-06-10T11:04:31.000Z",
          "score": 4,
          "type": "love",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "emoji_code": "👍",
          "other_score": 42
        }""".withoutWhitespace()
    val upstreamReaction = ReactionRequest(
        type = "love",
        createdAt = Date(1591787071000),
        score = 4,
        updatedAt = Date(1591787071588),
        custom = mapOf("other_score" to 42, "emoji_code" to "👍"),
    )
}
