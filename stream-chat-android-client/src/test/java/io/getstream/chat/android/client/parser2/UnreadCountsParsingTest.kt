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
import io.getstream.chat.android.models.UnreadChannel
import io.getstream.chat.android.models.UnreadChannelByType
import io.getstream.chat.android.models.UnreadThread
import io.getstream.chat.android.network.models.WrappedUnreadCountsResponse
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

internal class UnreadCountsParsingTest {

    private val parser = ParserFactory.createMoshiChatParser()

    private val domainMapping = DomainMapping(
        currentUserIdProvider = { "" },
        channelTransformer = NoOpChannelTransformer,
        messageTransformer = NoOpMessageTransformer,
        userTransformer = NoOpUserTransformer,
    )

    @Language("JSON")
    private val json =
        """{
          "duration": "1ms",
          "total_unread_count": 7,
          "total_unread_threads_count": 3,
          "total_unread_count_by_team": { "team-1": 4 },
          "channels": [
            { "channel_id": "messaging:c1", "unread_count": 2, "last_read": "1970-01-01T00:00:01.000Z" }
          ],
          "threads": [
            {
              "parent_message_id": "parent-1",
              "unread_count": 1,
              "last_read": "1970-01-01T00:00:01.000Z",
              "last_read_message_id": "msg-1"
            }
          ],
          "channel_type": [
            { "channel_type": "messaging", "channel_count": 5, "unread_count": 6 }
          ]
        }"""

    @Test
    fun `deserializes the unread counts wire shape and maps it to UnreadCounts`() {
        val dto = parser.fromJson(json, WrappedUnreadCountsResponse::class.java)

        val unreadCounts = with(domainMapping) { dto.toDomain() }

        assertEquals(7, unreadCounts.messagesCount)
        assertEquals(3, unreadCounts.threadsCount)
        assertEquals(mapOf("team-1" to 4), unreadCounts.messagesCountByTeam)
        assertEquals(
            listOf(UnreadChannel(cid = "messaging:c1", messagesCount = 2, lastRead = Date(1000))),
            unreadCounts.channels,
        )
        assertEquals(
            listOf(
                UnreadThread(
                    parentMessageId = "parent-1",
                    messagesCount = 1,
                    lastRead = Date(1000),
                    lastReadMessageId = "msg-1",
                ),
            ),
            unreadCounts.threads,
        )
        assertEquals(
            listOf(UnreadChannelByType(channelType = "messaging", channelsCount = 5, messagesCount = 6)),
            unreadCounts.channelsByType,
        )
    }
}
