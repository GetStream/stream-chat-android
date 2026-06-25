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

import io.getstream.chat.android.models.ChannelUserRead
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

internal object ChannelUserReadTestData {

    @Language("JSON")
    val jsonAllFields =
        """{"user":{"id":"user-1","role":"user","created_at":"2025-04-01T10:00:00.000Z","updated_at":"2025-04-07T15:30:00.000Z","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},"last_read":"2025-04-07T16:00:00.000Z","unread_messages":5,"last_read_message_id":"msg-123","last_delivered_at":"2025-04-07T17:00:00.000Z","last_delivered_message_id":"msg-124"}"""

    @Language("JSON")
    val jsonOptionalFieldsMissing =
        """{"user":{"id":"user-1","role":"user","created_at":"2025-04-01T10:00:00.000Z","updated_at":"2025-04-07T15:30:00.000Z","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},"last_read":"2025-04-07T16:00:00.000Z","unread_messages":5}"""

    @Language("JSON")
    val jsonMissingUser =
        """{"last_read":"2025-04-07T16:00:00.000Z","unread_messages":5,"last_read_message_id":"msg-123","last_delivered_at":"2025-04-07T17:00:00.000Z","last_delivered_message_id":"msg-124"}"""

    @Language("JSON")
    val jsonMissingLastRead =
        """{"user":{"id":"user-1","role":"user","created_at":"2025-04-01T10:00:00.000Z","updated_at":"2025-04-07T15:30:00.000Z","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},"unread_messages":5,"last_read_message_id":"msg-123","last_delivered_at":"2025-04-07T17:00:00.000Z","last_delivered_message_id":"msg-124"}"""

    @Language("JSON")
    val jsonMissingUnreadMessages =
        """{"user":{"id":"user-1","role":"user","created_at":"2025-04-01T10:00:00.000Z","updated_at":"2025-04-07T15:30:00.000Z","banned":false,"online": true, "language": "", "created_at": "2020-01-01T00:00:00.000Z", "updated_at": "2020-01-01T00:00:00.000Z"},"last_read":"2025-04-07T16:00:00.000Z","last_read_message_id":"msg-123","last_delivered_at":"2025-04-07T17:00:00.000Z","last_delivered_message_id":"msg-124"}"""

    val lastReceivedEventDate = Date(1744200000000L)

    val expectedAllFields = ChannelUserRead(
        user = User(
            id = "user-1",
            role = "user",
            createdAt = Date(1743501600000L),
            updatedAt = Date(1744039800000L),
            banned = false,
            online = true,
            invisible = false,
        ),
        lastReceivedEventDate = lastReceivedEventDate,
        lastRead = Date(1744041600000L),
        unreadMessages = 5,
        lastReadMessageId = "msg-123",
        lastDeliveredAt = Date(1744045200000L),
        lastDeliveredMessageId = "msg-124",
    )

    val expectedOptionalFieldsMissing = ChannelUserRead(
        user = User(
            id = "user-1",
            role = "user",
            createdAt = Date(1743501600000L),
            updatedAt = Date(1744039800000L),
            banned = false,
            online = true,
            invisible = false,
        ),
        lastReceivedEventDate = lastReceivedEventDate,
        lastRead = Date(1744041600000L),
        unreadMessages = 5,
        lastReadMessageId = null,
        lastDeliveredAt = null,
        lastDeliveredMessageId = null,
    )
}
