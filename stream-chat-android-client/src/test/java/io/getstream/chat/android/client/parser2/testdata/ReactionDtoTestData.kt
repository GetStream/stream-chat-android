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

import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import io.getstream.chat.android.client.api2.model.dto.UpstreamReactionDto
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ReactionDtoTestData {

    @Language("JSON")
    val downstreamJson =
        """{
          "message_id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "score": 5,
          "type": "like",
          "user": ${UserDtoTestData.downstreamJson},
          "user_id": "userId",
          "created_at": "2020-06-10T11:04:31.0Z",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "emoji_code": "👍",
          "customKey1": "customVal1"
        }
        """.withoutWhitespace()
    val downstreamReaction = DownstreamReactionDto(
        messageId = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 5,
        user = UserDtoTestData.downstreamUser,
        userId = "userId",
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        custom = mapOf(
            "emoji_code" to "👍",
            "customKey1" to "customVal1",
        ),
    )

    @Language("JSON")
    val downstreamJsonWithoutExtraData =
        """{
          "message_id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "score": 0,
          "type": "like",
          "user": ${UserDtoTestData.downstreamJson},
          "user_id": "",
          "created_at": "2020-06-10T11:04:31.0Z",
          "updated_at": "2020-06-10T11:04:31.588Z"
        }""".withoutWhitespace()
    val downstreamReactionWithoutExtraData = DownstreamReactionDto(
        messageId = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 0,
        user = UserDtoTestData.downstreamUser,
        userId = "",
        createdAt = Date(1591787071000),
        updatedAt = Date(1591787071588),
        custom = emptyMap(),
    )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
          "score": 0,
          "type": "love"
        }""".withoutWhitespace()
    val upstreamReactionWithoutExtraData = UpstreamReactionDto(
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
    val upstreamReaction = UpstreamReactionDto(
        type = "love",
        createdAt = Date(1591787071000),
        score = 4,
        updatedAt = Date(1591787071588),
        custom = mapOf("other_score" to 42, "emoji_code" to "👍"),
    )
}
