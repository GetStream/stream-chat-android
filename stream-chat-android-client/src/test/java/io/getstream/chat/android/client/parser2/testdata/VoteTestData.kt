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

import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.Vote
import org.intellij.lang.annotations.Language
import java.util.Date

internal object VoteTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"id":"vote1","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z","user":{"id":"user1","role":"user","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},"user_id":"user1","is_answer":false,"answer_text":null}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{"id":"vote1","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingId =
        """{"poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingPollId =
        """{"id":"vote1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingOptionId =
        """{"id":"vote1","poll_id":"poll1","created_at":"2024-01-01T10:00:00.000Z","updated_at":"2024-01-01T11:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingCreatedAt =
        """{"id":"vote1","poll_id":"poll1","option_id":"option1","updated_at":"2024-01-01T11:00:00.000Z"}"""

    @Language("JSON")
    val jsonMissingUpdatedAt =
        """{"id":"vote1","poll_id":"poll1","option_id":"option1","created_at":"2024-01-01T10:00:00.000Z"}"""

    val expectedAllFields = Vote(
        id = "vote1",
        pollId = "poll1",
        optionId = "option1",
        createdAt = Date(1704103200000L),
        updatedAt = Date(1704106800000L),
        user = User(
            id = "user1",
            role = "user",
            invisible = false,
            banned = false,
            online = true,
        ),
    )

    val expectedOptionalFieldsMissing = Vote(
        id = "vote1",
        pollId = "poll1",
        optionId = "option1",
        createdAt = Date(1704103200000L),
        updatedAt = Date(1704106800000L),
        user = null,
    )
}
