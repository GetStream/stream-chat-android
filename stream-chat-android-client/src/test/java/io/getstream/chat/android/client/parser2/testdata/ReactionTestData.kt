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

import io.getstream.chat.android.models.Reaction
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ReactionTestData {

    // The wire always sends these: every field on payload.ReactionResponse is a plain, non-omitempty
    // tag, and the nested user is the full commonpayloads.UserResponse. Only custom data is optional.
    @Language("JSON")
    private const val USER_JSON = """{
        "id": "user1",
        "role": "user",
        "banned": false,
        "online": true,
        "language": "en",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z"
    }"""

    private val USER = User(
        id = "user1",
        role = "user",
        banned = false,
        online = true,
        invisible = null,
        language = "en",
        createdAt = Date(1577836800000),
        updatedAt = Date(1577923200000),
    )

    @Language("JSON")
    val jsonAllFields = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON,
        "emoji_code": "\uD83D\uDC4D",
        "custom_field": "custom_value"
    }"""

    @Language("JSON")
    val jsonOptionalFieldsMissing = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON
    }"""

    @Language("JSON")
    val jsonMissingMessageId = """{
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON
    }"""

    @Language("JSON")
    val jsonMissingType = """{
        "message_id": "msg1",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON
    }"""

    @Language("JSON")
    val jsonMissingScore = """{
        "message_id": "msg1",
        "type": "like",
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON
    }"""

    @Language("JSON")
    val jsonMissingUserId = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON
    }"""

    @Language("JSON")
    val jsonMissingCreatedAt = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON
    }"""

    @Language("JSON")
    val jsonMissingUpdatedAt = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "user": $USER_JSON
    }"""

    @Language("JSON")
    val jsonMissingUser = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z"
    }"""

    @Language("JSON")
    val jsonWithExplicitNulls = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": $USER_JSON,
        "emoji_code": null
    }"""

    val expectedAllFields = Reaction(
        messageId = "msg1",
        type = "like",
        score = 1,
        userId = "user1",
        createdAt = Date(1577836800000),
        updatedAt = Date(1577923200000),
        user = USER,
        emojiCode = "\uD83D\uDC4D",
        extraData = mapOf("custom_field" to "custom_value"),
    )

    val expectedOptionalFieldsMissing = Reaction(
        messageId = "msg1",
        type = "like",
        score = 1,
        userId = "user1",
        createdAt = Date(1577836800000),
        updatedAt = Date(1577923200000),
        user = USER,
        emojiCode = null,
        extraData = emptyMap(),
    )

    val expectedWithExplicitNulls = Reaction(
        messageId = "msg1",
        type = "like",
        score = 1,
        userId = "user1",
        createdAt = Date(1577836800000),
        updatedAt = Date(1577923200000),
        user = USER,
        emojiCode = null,
        extraData = emptyMap(),
    )
}
