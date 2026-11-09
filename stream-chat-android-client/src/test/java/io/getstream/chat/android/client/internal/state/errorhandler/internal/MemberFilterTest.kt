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

package io.getstream.chat.android.client.internal.state.errorhandler.internal

import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date

internal class MemberFilterTest {

    private val cid = "messaging:general"
    private val joinedAt = Date(1_800_000_000_000)
    private val member = randomMember(
        user = randomUser(
            id = "anna",
            name = "Anna-Maria Smith",
            lastActive = joinedAt,
            extraData = mutableMapOf("email" to "anna@example.com"),
        ),
        createdAt = joinedAt,
        isInvited = false,
        banned = false,
        channelRole = "channel_member",
    ).copy(notificationsMuted = null, extraData = mapOf("team" to "red", "level" to 3.0, "tags" to listOf("a", "b")))

    @Test
    fun `neutral filter matches`() {
        assertTrue(Filters.neutral().matches(member))
    }

    @Test
    fun `user id and name conditions compare against the user`() {
        assertTrue(Filters.eq("id", "anna").matches(member))
        assertTrue(Filters.eq("user_id", "anna").matches(member))
        assertFalse(Filters.eq("id", "bob").matches(member))
        assertTrue(Filters.`in`("user_id", listOf("bob", "anna")).matches(member))
        assertFalse(Filters.nin("user_id", listOf("bob", "anna")).matches(member))
        assertTrue(Filters.eq("name", "Anna-Maria Smith").matches(member))
        assertTrue(Filters.ne("name", "Bob").matches(member))
        assertTrue(Filters.eq("user.email", "anna@example.com").matches(member))
    }

    @Test
    fun `name autocomplete matches word starts ignoring case`() {
        assertTrue(Filters.autocomplete("name", "an").matches(member))
        assertTrue(Filters.autocomplete("name", "MARIA").matches(member))
        assertTrue(Filters.autocomplete("name", "smi an").matches(member))
        assertFalse(Filters.autocomplete("name", "mith").matches(member))
        assertFalse(Filters.autocomplete("name", "smith bob").matches(member))
        assertFalse(Filters.autocomplete("name", " ").matches(member))
    }

    @Test
    fun `name autocomplete ignores accents`() {
        val accented = member.copy(user = member.user.copy(name = "José Müller"))

        assertTrue(Filters.autocomplete("name", "jose").matches(accented))
        assertTrue(Filters.autocomplete("name", "MULL").matches(accented))
        assertTrue(Filters.autocomplete("name", "jos").matches(member.copy(user = member.user.copy(name = "Jos"))))
        assertTrue(Filters.autocomplete("name", "josé").matches(member.copy(user = member.user.copy(name = "Jose"))))
    }

    @Test
    fun `membership conditions compare against the member`() {
        assertTrue(Filters.eq("channel_role", "channel_member").matches(member))
        assertFalse(Filters.eq("banned", true).matches(member))
        assertTrue(Filters.eq("notifications_muted", false).matches(member))
        assertTrue(Filters.eq("joined", true).matches(member))
        assertFalse(Filters.eq("invite", "pending").matches(member))
        assertTrue(Filters.eq("cid", cid).matches(member))
    }

    @Test
    fun `invite and joined follow the invite dates`() {
        val pending = member.copy(isInvited = true, inviteAcceptedAt = null, inviteRejectedAt = null)
        val accepted = member.copy(isInvited = true, inviteAcceptedAt = joinedAt, inviteRejectedAt = null)
        val rejected = member.copy(isInvited = true, inviteAcceptedAt = null, inviteRejectedAt = joinedAt)

        assertTrue(Filters.eq("invite", "pending").matches(pending))
        assertFalse(Filters.eq("joined", true).matches(pending))
        assertTrue(Filters.eq("invite", "accepted").matches(accepted))
        assertTrue(Filters.eq("joined", true).matches(accepted))
        assertTrue(Filters.eq("invite", "rejected").matches(rejected))
        assertTrue(Filters.eq("joined", false).matches(rejected))
    }

    @Test
    fun `date conditions accept dates and date strings`() {
        assertTrue(Filters.greaterThanEquals("created_at", joinedAt).matches(member))
        assertFalse(Filters.greaterThan("created_at", joinedAt).matches(member))
        assertTrue(Filters.lessThan("created_at", "2030-01-01T00:00:00.000Z").matches(member))
        assertTrue(Filters.lessThanEquals("last_active", joinedAt).matches(member))
        assertFalse(Filters.greaterThan("updated_at", joinedAt).matches(member.copy(updatedAt = null)))
    }

    @Test
    fun `custom fields compare against the member custom data`() {
        assertTrue(Filters.eq("team", "red").matches(member))
        assertTrue(Filters.greaterThan("level", 2).matches(member))
        assertTrue(Filters.contains("tags", "b").matches(member))
        assertTrue(Filters.exists("team").matches(member))
        assertTrue(Filters.notExists("missing").matches(member))
    }

    @Test
    fun `logical conditions combine the nested conditions`() {
        assertTrue(Filters.and(Filters.eq("id", "anna"), Filters.eq("team", "red")).matches(member))
        assertFalse(Filters.and(Filters.eq("id", "anna"), Filters.eq("team", "blue")).matches(member))
        assertTrue(Filters.or(Filters.eq("id", "bob"), Filters.eq("team", "red")).matches(member))
        assertFalse(Filters.nor(Filters.eq("id", "bob"), Filters.eq("team", "red")).matches(member))
    }

    @Test
    fun `conditions that cannot be evaluated do not match`() {
        assertFalse(Filters.eq("is_moderator", true).matches(member))
        assertFalse(Filters.ne("is_moderator", true).matches(member))
        assertFalse(Filters.nin("missing", listOf("a")).matches(member))
        assertFalse(Filters.eq("created_at", 1).matches(member))
        assertFalse(Filters.distinct(listOf("anna", "bob")).matches(member))
    }

    @Test
    fun `name sort compares user names`() {
        val bob = randomMember(user = randomUser(name = "Bob"))
        val anna = randomMember(user = randomUser(name = "Anna"))

        assertEquals(
            listOf(anna, bob),
            listOf(bob, anna).sortedWith(QuerySortByField.ascByName<Member>("name").memberComparator),
        )
    }

    @Test
    fun `name sort uses the collation order where case and accents only break ties`() {
        val names = listOf("bob", "Émile", "anna", "Alice", "Eva", "emil")
        val members = names.map { randomMember(user = randomUser(name = it)) }

        val sorted = members.sortedWith(QuerySortByField.ascByName<Member>("name").memberComparator)

        assertEquals(listOf("Alice", "anna", "bob", "emil", "Émile", "Eva"), sorted.map { it.user.name })
    }

    @Test
    fun `sort without name keeps the member field order`() {
        val older = randomMember(createdAt = Date(1))
        val newer = randomMember(createdAt = Date(2))

        assertEquals(
            listOf(newer, older),
            listOf(older, newer).sortedWith(QuerySortByField.descByName<Member>("created_at").memberComparator),
        )
    }

    private fun FilterObject.matches(member: Member): Boolean = matchesMember(member, cid)
}
