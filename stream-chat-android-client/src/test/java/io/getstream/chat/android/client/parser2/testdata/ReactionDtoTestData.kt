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
          "extraData": {
            "key1": true
          },
          "customKey1": "customVal1"
        }
        """.withoutWhitespace()
    val downstreamReaction = DownstreamReactionDto(
        message_id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 5,
        user = UserDtoTestData.downstreamUser,
        user_id = "userId",
        created_at = Date(1591787071000),
        updated_at = Date(1591787071588),
        extraData = mapOf(
            "extraData" to mapOf(
                "key1" to true,
            ),
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
          "user_id": ""
        }""".withoutWhitespace()
    val downstreamReactionWithoutExtraData = DownstreamReactionDto(
        message_id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 0,
        user = UserDtoTestData.downstreamUser,
        user_id = "",
        created_at = null,
        updated_at = null,
        extraData = emptyMap(),
    )

    @Language("JSON")
    val upstreamJsonWithoutExtraData =
        """{
          "message_id": "messageId",
          "score": 0,
          "type": "love",
          "user_id": "userId"
        }""".withoutWhitespace()
    val upstreamReactionWithoutExtraData = UpstreamReactionDto(
        created_at = null,
        message_id = "messageId",
        score = 0,
        type = "love",
        updated_at = null,
        user = null,
        user_id = "userId",
        extraData = emptyMap(),
    )

    @Language("JSON")
    val upstreamJson =
        """{
          "created_at": "2020-06-10T11:04:31.000Z",  
          "message_id": "messageId",
          "score": 4,
          "type": "love",
          "updated_at": "2020-06-10T11:04:31.588Z",
          "user": ${UserDtoTestData.upstreamJson},
          "user_id": "userId",
          "other_score": 42
        }""".withoutWhitespace()
    val upstreamReaction = UpstreamReactionDto(
        created_at = Date(1591787071000),
        message_id = "messageId",
        score = 4,
        type = "love",
        updated_at = Date(1591787071588),
        user = UserDtoTestData.upstreamUser,
        user_id = "userId",
        extraData = mapOf("other_score" to 42),
    )
}
