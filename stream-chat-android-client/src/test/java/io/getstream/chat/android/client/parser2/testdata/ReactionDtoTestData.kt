package io.getstream.chat.android.client.parser2.testdata

import io.getstream.chat.android.client.api2.model.dto.DownstreamReactionDto
import org.intellij.lang.annotations.Language

internal object ReactionDtoTestData {

    @Language("JSON")
    val downstreamJson =
        """{
          "message_id": "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
          "score": 0,
          "type": "like",
          "user": ${UserDtoTestData.downstreamJson},
          "user_id": "",
          "extraData": {
            "key1": true
          },
          "customKey1": "customVal1"
        }
        """.withoutWhitespace()
    val downstreamReaction = DownstreamReactionDto(
        message_id = "8584452-6d711169-0224-41c2-b9aa-1adbe624521b",
        type = "like",
        score = 0,
        user = UserDtoTestData.downstreamUser,
        user_id = "",
        created_at = null,
        updated_at = null,
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
}
