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

package io.getstream.chat.android.models

import io.getstream.chat.android.randomBoolean
import io.getstream.chat.android.randomDate
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class MemberTest {

    @Test
    fun `getUserId should return the id of the member user`() {
        val member = randomMember(user = randomUser(id = "user1"))
        assertEquals("user1", member.getUserId())
    }

    @Test
    fun `getComparableField should return the user id for snake_case and camelCase field names`() {
        val member = randomMember(user = randomUser(id = "user1"))
        assertEquals("user1", member.getComparableField("user_id"))
        assertEquals("user1", member.getComparableField("userId"))
    }

    @Test
    fun `getComparableField should return date fields for snake_case and camelCase field names`() {
        val member = randomMember(
            createdAt = randomDate(),
            updatedAt = randomDate(),
            inviteAcceptedAt = randomDate(),
            inviteRejectedAt = randomDate(),
            banExpires = randomDate(),
            pinnedAt = randomDate(),
            archivedAt = randomDate(),
        )
        assertEquals(member.createdAt, member.getComparableField("created_at"))
        assertEquals(member.createdAt, member.getComparableField("createdAt"))
        assertEquals(member.updatedAt, member.getComparableField("updated_at"))
        assertEquals(member.updatedAt, member.getComparableField("updatedAt"))
        assertEquals(member.inviteAcceptedAt, member.getComparableField("invite_accepted_at"))
        assertEquals(member.inviteAcceptedAt, member.getComparableField("inviteAcceptedAt"))
        assertEquals(member.inviteRejectedAt, member.getComparableField("invite_rejected_at"))
        assertEquals(member.inviteRejectedAt, member.getComparableField("inviteRejectedAt"))
        assertEquals(member.banExpires, member.getComparableField("ban_expires"))
        assertEquals(member.banExpires, member.getComparableField("banExpires"))
        assertEquals(member.pinnedAt, member.getComparableField("pinned_at"))
        assertEquals(member.pinnedAt, member.getComparableField("pinnedAt"))
        assertEquals(member.archivedAt, member.getComparableField("archived_at"))
        assertEquals(member.archivedAt, member.getComparableField("archivedAt"))
    }

    @Test
    fun `getComparableField should return boolean fields`() {
        val member = randomMember(
            isInvited = randomBoolean(),
            shadowBanned = randomBoolean(),
            banned = randomBoolean(),
        ).copy(notificationsMuted = randomBoolean())
        assertEquals(member.isInvited, member.getComparableField("is_invited"))
        assertEquals(member.isInvited, member.getComparableField("isInvited"))
        assertEquals(member.shadowBanned, member.getComparableField("shadow_banned"))
        assertEquals(member.shadowBanned, member.getComparableField("shadowBanned"))
        assertEquals(member.banned, member.getComparableField("banned"))
        assertEquals(member.notificationsMuted, member.getComparableField("notifications_muted"))
        assertEquals(member.notificationsMuted, member.getComparableField("notificationsMuted"))
    }

    @Test
    fun `getComparableField should return string fields`() {
        val member = randomMember(channelRole = randomString()).copy(status = randomString())
        assertEquals(member.channelRole, member.getComparableField("channel_role"))
        assertEquals(member.channelRole, member.getComparableField("channelRole"))
        assertEquals(member.status, member.getComparableField("status"))
    }

    @Test
    fun `getComparableField should return extraData value for custom field`() {
        val member = randomMember().copy(extraData = mapOf("customField" to "customValue"))
        assertEquals("customValue", member.getComparableField("customField"))
    }

    @Test
    fun `getComparableField should return null for unknown field`() {
        val member = randomMember()
        assertNull(member.getComparableField("unknownField"))
    }
}
