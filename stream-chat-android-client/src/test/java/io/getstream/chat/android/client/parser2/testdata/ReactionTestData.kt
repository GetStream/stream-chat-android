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

    @Language("JSON")
    val jsonAllFields = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "user": {"id": "user1", "role": "user", "banned": false, "online": true},
        "emoji_code": "👍",
        "custom_field": "custom_value"
    }"""

    @Language("JSON")
    val jsonOptionalFieldsMissing = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1"
    }"""

    @Language("JSON")
    val jsonMissingMessageId = """{
        "type": "like",
        "score": 1,
        "user_id": "user1"
    }"""

    @Language("JSON")
    val jsonMissingType = """{
        "message_id": "msg1",
        "score": 1,
        "user_id": "user1"
    }"""

    @Language("JSON")
    val jsonMissingScore = """{
        "message_id": "msg1",
        "type": "like",
        "user_id": "user1"
    }"""

    @Language("JSON")
    val jsonMissingUserId = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1
    }"""

    val expectedAllFields = Reaction(
        messageId = "msg1",
        type = "like",
        score = 1,
        userId = "user1",
        createdAt = Date(1577836800000),
        updatedAt = Date(1577923200000),
        user = User(id = "user1", role = "user", banned = false, online = true, invisible = false),
        emojiCode = "👍",
        extraData = mapOf("custom_field" to "custom_value"),
    )

    val expectedOptionalFieldsMissing = Reaction(
        messageId = "msg1",
        type = "like",
        score = 1,
        userId = "user1",
        createdAt = null,
        updatedAt = null,
        user = null,
        emojiCode = null,
        extraData = emptyMap(),
    )

    @Language("JSON")
    val jsonWithExplicitNulls = """{
        "message_id": "msg1",
        "type": "like",
        "score": 1,
        "user_id": "user1",
        "emoji_code": null
    }"""

    val expectedWithExplicitNulls = Reaction(
        messageId = "msg1",
        type = "like",
        score = 1,
        userId = "user1",
        createdAt = null,
        updatedAt = null,
        user = null,
        emojiCode = null,
        extraData = emptyMap(),
    )
}
