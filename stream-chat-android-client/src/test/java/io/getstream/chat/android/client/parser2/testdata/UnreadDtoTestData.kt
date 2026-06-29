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

import org.intellij.lang.annotations.Language
import java.time.ZonedDateTime
import java.util.Date
import io.getstream.chat.android.network.models.UnreadCountsChannel as UnreadChannelDto
import io.getstream.chat.android.network.models.UnreadCountsChannelType as UnreadChannelByTypeDto
import io.getstream.chat.android.network.models.UnreadCountsThread as UnreadThreadDto
import io.getstream.chat.android.network.models.WrappedUnreadCountsResponse as UnreadDto

internal object UnreadDtoTestData {

    @Language("JSON")
    val json = """{
        "duration": "1.5ms",
        "total_unread_count": 7,
        "total_unread_threads_count": 15,
        "channel_type": [
            {
                "channel_count": 9,
                "channel_type": "messaging",
                "unread_count": 10
            },
            {
                "channel_count": 11,
                "channel_type": "livestream",
                "unread_count": 12
            }
        ],
        "channels": [
            {
                "channel_id": "channel1",
                "last_read": "$LastReadString",
                "unread_count": 4
            },
            {
                "channel_id": "channel2",
                "last_read": "$LastReadString",
                "unread_count": 3
            }
        ],
        "threads": [
            {
                "last_read": "$LastReadString",
                "last_read_message_id": "message1",
                "parent_message_id": "message1",
                "unread_count": 7
            },
            {
                "last_read": "$LastReadString",
                "last_read_message_id": "message2",
                "parent_message_id": "message2",
                "unread_count": 8
            }
        ],
        "total_unread_count_by_team": {
            "team1": 3,
            "team2": 4
        }
    }""".withoutWhitespace()

    val dto = UnreadDto(
        duration = "1.5ms",
        totalUnreadCount = 7,
        totalUnreadThreadsCount = 15,
        totalUnreadCountByTeam = mapOf("team1" to 3, "team2" to 4),
        channels = listOf(
            UnreadChannelDto(
                channelId = "channel1",
                unreadCount = 4,
                lastRead = LastReadDate,
            ),
            UnreadChannelDto(
                channelId = "channel2",
                unreadCount = 3,
                lastRead = LastReadDate,
            ),
        ),
        threads = listOf(
            UnreadThreadDto(
                parentMessageId = "message1",
                unreadCount = 7,
                lastRead = LastReadDate,
                lastReadMessageId = "message1",
            ),
            UnreadThreadDto(
                parentMessageId = "message2",
                unreadCount = 8,
                lastRead = LastReadDate,
                lastReadMessageId = "message2",
            ),
        ),
        channelType = listOf(
            UnreadChannelByTypeDto(
                channelType = "messaging",
                channelCount = 9,
                unreadCount = 10,
            ),
            UnreadChannelByTypeDto(
                channelType = "livestream",
                channelCount = 11,
                unreadCount = 12,
            ),
        ),
    )
}

private const val LastReadString = "2025-08-15T07:15:00.000Z"

private val LastReadDate = ZonedDateTime
    .parse(LastReadString)
    .toInstant()
    .let { Date.from(it) }
