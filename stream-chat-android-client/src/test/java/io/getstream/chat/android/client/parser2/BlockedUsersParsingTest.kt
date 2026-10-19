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
import io.getstream.chat.android.client.api2.model.response.QueryBlockedUsersResponse
import io.getstream.chat.android.models.NoOpChannelTransformer
import io.getstream.chat.android.models.NoOpMessageTransformer
import io.getstream.chat.android.models.NoOpUserTransformer
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
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

    @Language("JSON")
    private val jsonWithBlockedUser =
        """{
          "duration": "1ms",
          "blocks": [
            {
              "user_id": "blocker-1",
              "blocked_user_id": "blocked-1",
              "created_at": "1970-01-01T00:00:01.000Z",
              "blocked_user": {
                "id": "blocked-1", "name": "Blocked One", "image": "https://example.com/blocked-1.png",
                "role": "user", "banned": false, "online": false
              }
            }
          ]
        }"""

    @Language("JSON")
    private val jsonWithoutBlockedUser =
        """{
          "duration": "1ms",
          "blocks": [
            { "user_id": "blocker-1", "blocked_user_id": "blocked-1", "created_at": "1970-01-01T00:00:01.000Z" }
          ]
        }"""

    @Test
    fun `parses the blocked user into UserBlock`() {
        val dto = parser.fromJson(jsonWithBlockedUser, QueryBlockedUsersResponse::class.java)

        val block = with(domainMapping) { dto.blocks.toDomain() }.single()

        assertEquals("blocker-1", block.blockedBy)
        assertEquals("blocked-1", block.userId)
        assertEquals(Date(1000), block.blockedAt)
        assertEquals("blocked-1", block.blockedUser?.id)
        assertEquals("Blocked One", block.blockedUser?.name)
        assertEquals("https://example.com/blocked-1.png", block.blockedUser?.image)
    }

    @Test
    fun `parses a block without a blocked user`() {
        val dto = parser.fromJson(jsonWithoutBlockedUser, QueryBlockedUsersResponse::class.java)

        val block = with(domainMapping) { dto.blocks.toDomain() }.single()

        assertEquals("blocked-1", block.userId)
        assertNull(block.blockedUser)
    }
}
