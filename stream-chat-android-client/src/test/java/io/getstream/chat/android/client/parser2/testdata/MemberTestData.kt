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

import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import org.intellij.lang.annotations.Language
import java.util.Date

internal object MemberTestData {

    @Language("JSON")
    val jsonAllFields = """{
        "user": {"id": "user1", "role": "user", "banned": false, "online": true},
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z",
        "invited": true,
        "invite_accepted_at": "2020-01-03T00:00:00.000Z",
        "invite_rejected_at": "2020-01-04T00:00:00.000Z",
        "shadow_banned": true,
        "banned": true,
        "channel_role": "channel_moderator",
        "notifications_muted": true,
        "status": "active",
        "ban_expires": "2020-01-05T00:00:00.000Z",
        "pinned_at": "2020-01-06T00:00:00.000Z",
        "archived_at": "2020-01-07T00:00:00.000Z",
        "custom_field": "custom_value"
    }"""

    @Language("JSON")
    val jsonOptionalFieldsMissing = """{
        "user": {"id": "user1", "role": "user", "banned": false, "online": true}
    }"""

    @Language("JSON")
    val jsonMissingUser = """{
        "created_at": "2020-01-01T00:00:00.000Z",
        "updated_at": "2020-01-02T00:00:00.000Z"
    }"""

    val expectedAllFields = Member(
        user = User(id = "user1", role = "user", banned = false, online = true, invisible = false),
        createdAt = Date(1577836800000),
        updatedAt = Date(1577923200000),
        isInvited = true,
        inviteAcceptedAt = Date(1578009600000),
        inviteRejectedAt = Date(1578096000000),
        shadowBanned = true,
        banned = true,
        channelRole = "channel_moderator",
        notificationsMuted = true,
        status = "active",
        banExpires = Date(1578182400000),
        pinnedAt = Date(1578268800000),
        archivedAt = Date(1578355200000),
        extraData = mapOf("custom_field" to "custom_value"),
    )

    val expectedOptionalFieldsMissing = Member(
        user = User(id = "user1", role = "user", banned = false, online = true, invisible = false),
        createdAt = null,
        updatedAt = null,
        isInvited = null,
        inviteAcceptedAt = null,
        inviteRejectedAt = null,
        shadowBanned = false,
        banned = false,
        channelRole = null,
        notificationsMuted = null,
        status = null,
        banExpires = null,
        pinnedAt = null,
        archivedAt = null,
        extraData = emptyMap(),
    )
}
