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

import io.getstream.chat.android.DeliveryReceipts
import io.getstream.chat.android.PrivacySettings
import io.getstream.chat.android.ReadReceipts
import io.getstream.chat.android.TypingIndicators
import io.getstream.chat.android.models.Device
import io.getstream.chat.android.models.PushProvider
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

internal object UserTestData {

    @Language("JSON")
    val jsonAllFields = """
        {
            "id": "user123",
            "name": "John Doe",
            "image": "https://example.com/avatar.jpg",
            "role": "user",
            "language": "en",
            "banned": false,
            "online": true,
            "created_at": "2020-01-01T00:00:00.000Z",
            "deactivated_at": null,
            "updated_at": "2020-01-02T00:00:00.000Z",
            "last_active": "2020-01-03T00:00:00.000Z",
            "teams": ["team1", "team2"],
            "teams_role": {"team1": "admin", "team2": "member"},
            "blocked_user_ids": ["blocked1"],
            "avg_response_time": 3600,
            "custom_field": "custom_value"
        }
    """.trimIndent()

    @Language("JSON")
    val jsonOptionalFieldsMissing = """
        {
            "id": "user456",
            "role": "admin",
            "language": "",
            "banned": true,
            "online": false,
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T00:00:00.000Z"
        }
    """.trimIndent()

    @Language("JSON")
    val jsonMissingId = """
        {
            "name": "Jane Doe",
            "role": "user",
            "banned": false,
            "online": true
        }
    """.trimIndent()

    @Language("JSON")
    val jsonMissingRole = """
        {
            "id": "user789",
            "name": "Jane Doe",
            "banned": false,
            "online": true
        }
    """.trimIndent()

    @Language("JSON")
    val jsonMissingBanned = """
        {
            "id": "user789",
            "name": "Jane Doe",
            "role": "user",
            "online": true
        }
    """.trimIndent()

    @Language("JSON")
    val jsonMissingOnline = """
        {
            "id": "user789",
            "name": "Jane Doe",
            "role": "user",
            "banned": false
        }
    """.trimIndent()

    val expectedAllFields = User(
        id = "user123",
        name = "John Doe",
        image = "https://example.com/avatar.jpg",
        role = "user",
        invisible = false,
        language = "en",
        banned = false,
        online = true,
        createdAt = Date(1577836800000L),
        deactivatedAt = null,
        updatedAt = Date(1577923200000L),
        lastActive = Date(1578009600000L),
        teams = listOf("team1", "team2"),
        teamsRole = mapOf("team1" to "admin", "team2" to "member"),
        blockedUserIds = listOf("blocked1"),
        avgResponseTime = 3600,
        extraData = mapOf("custom_field" to "custom_value"),
    )

    @Language("JSON")
    val jsonUnreadCountsNull = """
        {
            "id": "user123",
            "role": "user",
            "banned": false,
            "online": true,
            "total_unread_count": null,
            "unread_channels": null,
            "unread_threads": null
        }
    """.trimIndent()

    val expectedOptionalFieldsMissing = User(
        id = "user456",
        name = "",
        image = "",
        role = "admin",
        invisible = false,
        language = "",
        banned = true,
        online = false,
        createdAt = Date(1577836800000L),
        updatedAt = Date(1577836800000L),
        extraData = emptyMap(),
    )

    @Language("JSON")
    val jsonWithExplicitNulls = """
        {
            "id": "user123",
            "role": "user",
            "language": "",
            "banned": false,
            "online": true,
            "created_at": "2020-01-01T00:00:00.000Z",
            "updated_at": "2020-01-01T00:00:00.000Z",
            "name": null,
            "image": null,
            "avg_response_time": null,
            "last_active": null,
            "deactivated_at": null
        }
    """.trimIndent()

    val expectedWithExplicitNulls = User(
        id = "user123",
        name = "",
        image = "",
        role = "user",
        invisible = false,
        language = "",
        banned = false,
        online = true,
        createdAt = Date(1577836800000L),
        updatedAt = Date(1577836800000L),
        avgResponseTime = null,
        extraData = emptyMap(),
    )
}
