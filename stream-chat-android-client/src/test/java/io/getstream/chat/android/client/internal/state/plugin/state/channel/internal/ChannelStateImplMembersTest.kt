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

package io.getstream.chat.android.client.internal.state.plugin.state.channel.internal

import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.Date

@ExperimentalCoroutinesApi
internal class ChannelStateImplMembersTest : ChannelStateImplTestBase() {

    @Nested
    inner class SetMembers {

        @Test
        fun `setMembers should replace existing members`() = runTest {
            // given
            val initialMembers = createMembers(3)
            channelState.setMembers(initialMembers)
            // when
            val newMembers = createMembers(5, startIndex = 10)
            channelState.setMembers(newMembers)
            // then
            assertEquals(5, channelState.members.value.size)
            assertEquals(newMembers.map { it.getUserId() }.toSet(), channelState.members.value.map { it.getUserId() }.toSet())
        }

        @Test
        fun `setMembers with empty list should clear members`() = runTest {
            // given
            val initialMembers = createMembers(3)
            channelState.setMembers(initialMembers)
            // when
            channelState.setMembers(emptyList())
            // then
            assertTrue(channelState.members.value.isEmpty())
        }
    }

    @Nested
    inner class SetMemberCount {

        @Test
        fun `setMemberCount should set the member count`() = runTest {
            // when
            channelState.setMemberCount(100)
            // then
            assertEquals(100, channelState.membersCount.value)
        }
    }

    @Nested
    inner class AddMember {

        @Test
        fun `addMember should add new member`() = runTest {
            // given
            val member = createMember(1)
            // when
            channelState.addMember(member)
            // then
            assertEquals(1, channelState.members.value.size)
            assertEquals(member.getUserId(), channelState.members.value.first().getUserId())
        }

        @Test
        fun `addMember should not add duplicate member`() = runTest {
            // given
            val member = createMember(1)
            channelState.addMember(member)
            // when
            val duplicateMember = member.copy(user = member.user.copy(name = "Updated Name"))
            channelState.addMember(duplicateMember)
            // then
            assertEquals(1, channelState.members.value.size)
        }

        @Test
        fun `addMember should increment member count`() = runTest {
            // given
            channelState.setMemberCount(5)
            val member = createMember(1)
            // when
            channelState.addMember(member)
            // then
            assertEquals(6, channelState.membersCount.value)
        }

        @Test
        fun `addMember with duplicate should not increment member count`() = runTest {
            // given
            val member = createMember(1)
            channelState.addMember(member)
            val initialCount = channelState.membersCount.value
            // when
            channelState.addMember(member) // Add same member again
            // then - count should not change since member was already present
            assertEquals(initialCount, channelState.membersCount.value)
        }
    }

    @Nested
    inner class UpsertMember {

        @Test
        fun `upsertMember should add new member`() = runTest {
            // given
            val member = createMember(1)
            // when
            channelState.upsertMember(member)
            // then
            assertEquals(1, channelState.members.value.size)
            assertEquals(member.getUserId(), channelState.members.value.first().getUserId())
        }

        @Test
        fun `upsertMember should update existing member`() = runTest {
            // given
            val member = createMember(1)
            channelState.setMembers(listOf(member))
            // when
            val updatedMember = member.copy(user = member.user.copy(name = "Updated Name"))
            channelState.upsertMember(updatedMember)
            // then
            assertEquals(1, channelState.members.value.size)
            assertEquals("Updated Name", channelState.members.value.first().user.name)
        }
    }

    @Nested
    inner class UpsertMembers {

        @Test
        fun `upsertMembers should add multiple new members`() = runTest {
            // given
            val members = createMembers(3)
            // when
            channelState.upsertMembers(members)
            // then
            assertEquals(3, channelState.members.value.size)
        }

        @Test
        fun `upsertMembers should update existing members`() = runTest {
            // given
            val members = createMembers(3)
            channelState.setMembers(members)
            // when
            val updatedMembers = members.map { it.copy(user = it.user.copy(name = "Updated: ${it.user.name}")) }
            channelState.upsertMembers(updatedMembers)
            // then
            assertEquals(3, channelState.members.value.size)
            channelState.members.value.forEach { member ->
                assertTrue(member.user.name.startsWith("Updated:"))
            }
        }

        @Test
        fun `upsertMembers should add and update mixed members`() = runTest {
            // given
            val existingMembers = createMembers(2)
            channelState.setMembers(existingMembers)
            // when
            val newMember = createMember(10)
            val updatedMember = existingMembers[0].copy(user = existingMembers[0].user.copy(name = "Updated"))
            channelState.upsertMembers(listOf(updatedMember, newMember))
            // then
            assertEquals(3, channelState.members.value.size)
            assertEquals("Updated", channelState.members.value.find { it.getUserId() == existingMembers[0].getUserId() }?.user?.name)
        }

        @Test
        fun `upsertMembers with empty list should not change state`() = runTest {
            // given
            val members = createMembers(3)
            channelState.setMembers(members)
            // when
            channelState.upsertMembers(emptyList())
            // then
            assertEquals(3, channelState.members.value.size)
        }
    }

    @Nested
    inner class DeleteMember {

        @Test
        fun `deleteMember should remove member from state`() = runTest {
            // given
            val members = createMembers(3)
            channelState.setMembers(members)
            // when
            channelState.deleteMember(members[1].getUserId())
            // then
            assertEquals(2, channelState.members.value.size)
            assertFalse(channelState.members.value.any { it.getUserId() == members[1].getUserId() })
        }

        @Test
        fun `deleteMember should decrement member count`() = runTest {
            // given
            val members = createMembers(3)
            channelState.setMembers(members)
            channelState.setMemberCount(3)
            // when
            channelState.deleteMember(members[0].getUserId())
            // then
            assertEquals(2, channelState.membersCount.value)
        }

        @Test
        fun `deleteMember should not decrement count below zero`() = runTest {
            // given
            channelState.setMemberCount(0)
            // when
            channelState.deleteMember("non_existing_id")
            // then
            assertEquals(0, channelState.membersCount.value)
        }

        @Test
        fun `deleteMember should do nothing for non-existing member in list but still decrement count`() = runTest {
            // given - members list may be partial (not all members loaded); removed member might not be in our list
            val members = createMembers(3)
            channelState.setMembers(members)
            channelState.setMemberCount(3)
            // when - removing member not in our list
            channelState.deleteMember("non_existing_id")
            // then - list unchanged, count decremented (we trust the event)
            assertEquals(3, channelState.members.value.size)
            assertEquals(2, channelState.membersCount.value)
        }
    }

    @Nested
    inner class UpdateMemberBan {

        @Test
        fun `updateMemberBan should update ban status`() = runTest {
            // given
            val member = createMember(1)
            channelState.setMembers(listOf(member))
            // when
            channelState.updateMemberBan(
                memberId = member.getUserId(),
                banned = true,
                expiry = null,
                shadow = false,
            )
            // then
            val updatedMember = channelState.members.value.first()
            assertTrue(updatedMember.banned)
            assertNull(updatedMember.banExpires)
            assertFalse(updatedMember.shadowBanned)
        }

        @Test
        fun `updateMemberBan should set shadow ban`() = runTest {
            // given
            val member = createMember(1)
            channelState.setMembers(listOf(member))
            // when
            channelState.updateMemberBan(
                memberId = member.getUserId(),
                banned = true,
                expiry = null,
                shadow = true,
            )
            // then
            val updatedMember = channelState.members.value.first()
            assertTrue(updatedMember.banned)
            assertTrue(updatedMember.shadowBanned)
        }

        @Test
        fun `updateMemberBan should set ban expiry`() = runTest {
            // given
            val member = createMember(1)
            channelState.setMembers(listOf(member))
            val expiryDate = Date(System.currentTimeMillis() + 3600000) // 1 hour from now
            // when
            channelState.updateMemberBan(
                memberId = member.getUserId(),
                banned = true,
                expiry = expiryDate,
                shadow = false,
            )
            // then
            val updatedMember = channelState.members.value.first()
            assertTrue(updatedMember.banned)
            assertEquals(expiryDate, updatedMember.banExpires)
        }

        @Test
        fun `updateMemberBan should unban member`() = runTest {
            // given
            val member = createMember(1).copy(banned = true, shadowBanned = true)
            channelState.setMembers(listOf(member))
            // when
            channelState.updateMemberBan(
                memberId = member.getUserId(),
                banned = false,
                expiry = null,
                shadow = false,
            )
            // then
            val updatedMember = channelState.members.value.first()
            assertFalse(updatedMember.banned)
            assertFalse(updatedMember.shadowBanned)
        }

        @Test
        fun `updateMemberBan should do nothing for non-existing member`() = runTest {
            // given
            val member = createMember(1)
            channelState.setMembers(listOf(member))
            // when
            channelState.updateMemberBan(
                memberId = "non_existing_id",
                banned = true,
                expiry = null,
                shadow = false,
            )
            // then
            val existingMember = channelState.members.value.first()
            assertFalse(existingMember.banned)
        }
    }

    @Nested
    inner class SetMembership {

        @Test
        fun `setMembership should set the membership`() = runTest {
            // given
            channelState.updateChannelData { ChannelData("type", "id") }
            val membership = createMember(1)
            // when
            channelState.setMembership(membership)
            // then
            assertEquals(membership.getUserId(), channelState.channelData.value.membership?.getUserId())
        }

        @Test
        fun `setMembership should update existing membership`() = runTest {
            // given
            channelState.updateChannelData { ChannelData("type", "id") }
            val initialMembership = createMember(1)
            channelState.setMembership(initialMembership)
            // when
            val updatedMembership = initialMembership.copy(
                user = initialMembership.user.copy(name = "Updated Name"),
            )
            channelState.setMembership(updatedMembership)
            // then
            assertEquals("Updated Name", channelState.channelData.value.membership?.user?.name)
        }
    }

    @Nested
    inner class DeleteMembership {

        @Test
        fun `deleteMembership should remove the membership`() = runTest {
            // given
            channelState.updateChannelData { ChannelData("type", "id") }
            val membership = createMember(1)
            channelState.setMembership(membership)
            // when
            channelState.deleteMembership()
            // then
            assertNull(channelState.channelData.value.membership)
        }

        @Test
        fun `deleteMembership should do nothing if no membership exists`() = runTest {
            // given - no membership set
            assertNull(channelState.channelData.value.membership)
            // when
            channelState.deleteMembership()
            // then - should not throw
            assertNull(channelState.channelData.value.membership)
        }
    }

    private fun createMember(index: Int): Member {
        val user = randomUser(id = "user_$index", name = "User $index")
        return randomMember(user = user)
    }

    private fun createMembers(count: Int, startIndex: Int = 1): List<Member> {
        return (startIndex until startIndex + count).map { i ->
            createMember(i)
        }
    }
}
