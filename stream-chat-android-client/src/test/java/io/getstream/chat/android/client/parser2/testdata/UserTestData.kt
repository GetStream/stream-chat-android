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
            "invisible": true,
            "privacy_settings": {
                "typing_indicators": {"enabled": true},
                "read_receipts": {"enabled": false},
                "delivery_receipts": {"enabled": true}
            },
            "language": "en",
            "banned": false,
            "devices": [
                {
                    "id": "device1",
                    "push_provider": "firebase",
                    "push_provider_name": "Firebase"
                }
            ],
            "online": true,
            "created_at": "2020-01-01T00:00:00.000Z",
            "deactivated_at": null,
            "updated_at": "2020-01-02T00:00:00.000Z",
            "last_active": "2020-01-03T00:00:00.000Z",
            "teams": ["team1", "team2"],
            "teams_role": {"team1": "admin", "team2": "member"},
            "total_unread_count": 5,
            "unread_channels": 3,
            "unread_threads": 2,
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
            "banned": true,
            "online": false
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
        invisible = true,
        privacySettings = PrivacySettings(
            typingIndicators = TypingIndicators(enabled = true),
            readReceipts = ReadReceipts(enabled = false),
            deliveryReceipts = DeliveryReceipts(enabled = true),
        ),
        language = "en",
        banned = false,
        devices = listOf(
            Device(
                token = "device1",
                pushProvider = PushProvider.FIREBASE,
                providerName = "Firebase",
            ),
        ),
        online = true,
        createdAt = Date(1577836800000L),
        deactivatedAt = null,
        updatedAt = Date(1577923200000L),
        lastActive = Date(1578009600000L),
        totalUnreadCount = 5,
        unreadChannels = 3,
        unreadThreads = 2,
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
        privacySettings = null,
        language = "",
        banned = true,
        devices = emptyList(),
        online = false,
        createdAt = null,
        deactivatedAt = null,
        updatedAt = null,
        lastActive = null,
        totalUnreadCount = 0,
        unreadChannels = 0,
        unreadThreads = 0,
        mutes = emptyList(),
        teams = emptyList(),
        teamsRole = emptyMap(),
        channelMutes = emptyList(),
        blockedUserIds = emptyList(),
        avgResponseTime = null,
        pushPreference = null,
        extraData = emptyMap(),
    )
}
