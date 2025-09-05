/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

import io.getstream.chat.android.client.api2.model.dto.UnreadChannelByTypeDto
import io.getstream.chat.android.client.api2.model.dto.UnreadChannelDto
import io.getstream.chat.android.client.api2.model.dto.UnreadDto
import io.getstream.chat.android.client.api2.model.dto.UnreadThreadDto
import org.intellij.lang.annotations.Language
import java.time.ZonedDateTime
import java.util.Date

internal object UnreadDtoTestData {

    @Language("JSON")
    val json = """{
        "total_unread_count": 7,
        "total_unread_threads_count": 15,
        "total_unread_count_by_team": {
            "team1": 3,
            "team2": 4
        },
        "channels": [
            {
                "channel_id": "channel1",
                "unread_count": 4,
                "last_read": "$LastReadString"
            },
            {
                "channel_id": "channel2",
                "unread_count": 3,
                "last_read": "$LastReadString"
            }
        ],
        "threads": [
            {
                "parent_message_id": "message1",
                "unread_count": 7,
                "last_read": "$LastReadString",
                "last_read_message_id": "message1"
            },
            {
                "parent_message_id": "message2",
                "unread_count": 8,
                "last_read": "$LastReadString",
                "last_read_message_id": "message2"
            }
        ],
        "channel_type": [
            {
                "channel_type": "messaging",
                "channel_count": 9,
                "unread_count": 10
            },
            {
                "channel_type": "livestream",
                "channel_count": 11,
                "unread_count": 12
            }
        ]
    }""".withoutWhitespace()

    val dto = UnreadDto(
        total_unread_count = 7,
        total_unread_threads_count = 15,
        total_unread_count_by_team = mapOf("team1" to 3, "team2" to 4),
        channels = listOf(
            UnreadChannelDto(
                channel_id = "channel1",
                unread_count = 4,
                last_read = LastReadDate,
            ),
            UnreadChannelDto(
                channel_id = "channel2",
                unread_count = 3,
                last_read = LastReadDate,
            ),
        ),
        threads = listOf(
            UnreadThreadDto(
                parent_message_id = "message1",
                unread_count = 7,
                last_read = LastReadDate,
                last_read_message_id = "message1",
            ),
            UnreadThreadDto(
                parent_message_id = "message2",
                unread_count = 8,
                last_read = LastReadDate,
                last_read_message_id = "message2",
            ),
        ),
        channel_type = listOf(
            UnreadChannelByTypeDto(
                channel_type = "messaging",
                channel_count = 9,
                unread_count = 10,
            ),
            UnreadChannelByTypeDto(
                channel_type = "livestream",
                channel_count = 11,
                unread_count = 12,
            ),
        ),
    )
}

private const val LastReadString = "2025-08-15T07:15:00.000Z"

private val LastReadDate = ZonedDateTime
    .parse(LastReadString)
    .toInstant()
    .let { Date.from(it) }
