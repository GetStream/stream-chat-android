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
import io.getstream.chat.android.client.api2.model.response.FlagResponse
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.junit.jupiter.api.Test

/** The flag response as the backend sends it, through the DTO and the domain mapping. */
internal class FlagResponseParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()
    private val mapping = DomainMapping(
        currentUserIdProvider = { "jaewoong" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Test
    fun `A flag keeps its creation date and has no review fields until reviewed`() {
        val flag = with(mapping) { parser.fromJson(FLAG_RESPONSE, FlagResponse::class.java).flag.toDomain() }

        flag.createdAt?.time shouldBeEqualTo CREATED_AT_MILLIS
        flag.approvedAt.shouldBeNull()
        flag.reviewedBy shouldBeEqualTo ""
        flag.targetUser?.id shouldBeEqualTo "guest"
    }

    @Test
    fun `A reviewed flag keeps the reviewer id`() {
        val reviewed = FLAG_RESPONSE.replaceFirst(
            """"created_by_automod": false,""",
            """"created_by_automod": false, "reviewed_by": "moderator", "reviewed_at": "2026-10-06T11:00:00.000Z",""",
        )

        val flag = with(mapping) { parser.fromJson(reviewed, FlagResponse::class.java).flag.toDomain() }

        flag.reviewedBy shouldBeEqualTo "moderator"
        flag.createdAt?.time shouldBeEqualTo CREATED_AT_MILLIS
    }

    private companion object {
        // 2026-10-06T10:40:00.000Z
        private const val CREATED_AT_MILLIS = 1791283200000L

        private const val USER = """
            "id": "%s", "role": "user", "language": "en", "banned": false, "online": false,
            "created_at": "2026-01-01T00:00:00.000Z", "updated_at": "2026-01-01T00:00:00.000Z"
        """

        /** The shape of a `POST /moderation/flag` response for a user flag (recorded 2026-10-06, ids renamed). */
        private val FLAG_RESPONSE = """
            {
              "duration": "1ms",
              "flag": {
                "created_by_automod": false,
                "created_at": "2026-10-06T10:40:00.000Z",
                "updated_at": "2026-10-06T10:40:00.000Z",
                "user": { ${USER.format("jaewoong")} },
                "target_user": { ${USER.format("guest")} }
              }
            }
        """.trimIndent()
    }
}
