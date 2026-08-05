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
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.network.models.GetBlockedUsersResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

internal class BlockedUsersParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    // Each block carries two full UserResponse objects; this locks that the generated UserResponse
    // (with its required non-null `language`) parses from the real wire shape.
    @Language("JSON")
    private val json =
        """{
          "duration": "1ms",
          "blocks": [
            {
              "user_id": "blocker-1",
              "blocked_user_id": "blocked-1",
              "created_at": "1970-01-01T00:00:01.000Z",
              "user": {
                "id": "blocker-1", "role": "user", "language": "en", "banned": false,
                "online": true, "created_at": "2020-01-01T00:00:00.000Z",
                "updated_at": "2020-01-01T00:00:00.000Z", "birthland": "Polis Massa"
              },
              "blocked_user": {
                "id": "blocked-1", "role": "user", "language": "", "banned": true,
                "online": false, "created_at": "2020-01-01T00:00:00.000Z",
                "updated_at": "2020-01-01T00:00:00.000Z"
              }
            }
          ]
        }"""

    @Test
    fun `deserializes the blocked-users wire shape and maps it to UserBlock`() {
        val dto = parser.fromJson(json, GetBlockedUsersResponse::class.java)

        assertEquals("1ms", dto.duration)
        val block = dto.blocks.single()
        assertEquals("blocker-1", block.user.id)
        assertEquals("blocked-1", block.blockedUser.id)
        // The adapter collects root-level custom data, and `language` is required non-null but arrives empty.
        assertEquals(mapOf("birthland" to "Polis Massa"), block.user.custom)
        assertEquals("en", block.user.language)
        assertEquals("", block.blockedUser.language)

        val blocks = with(domainMapping) { dto.blocks.toDomain() }

        assertEquals(
            listOf(UserBlock(blockedBy = "blocker-1", userId = "blocked-1", blockedAt = Date(1000))),
            blocks,
        )
    }
}
