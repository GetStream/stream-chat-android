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

package io.getstream.chat.android.ui.common.feature.messages.composer.mention

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Role
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.UserGroup
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class MentionLookupHandlerTest {

    @Test
    fun `Empty query emits only channel and here without hitting the network`() = runTest {
        val fixture = Fixture().build()

        val result = fixture.handler.handleMentionLookup(query = "")

        assertEquals(listOf(Mention.Channel, Mention.Here), result)
        verify(fixture.chatClient, never()).searchUserGroups(any(), any(), any(), any(), any())
        verify(fixture.chatClient, never()).searchRoles(any(), any(), any(), any(), any())
    }

    @Test
    fun `Prefix matching here but not channel emits only here`() = runTest {
        val fixture = Fixture().build()

        val result = fixture.handler.handleMentionLookup(query = "he")

        assertEquals(listOf(Mention.Here), result)
    }

    @Test
    fun `Each matching source contributes a mention`() = runTest {
        val group = UserGroup(id = "g1", name = "platform")
        val role = Role(name = "admin")
        val user = User(id = "u1", name = "Alice")
        val fixture = Fixture()
            .withGroupSearchResult(listOf(group))
            .withRoleSearchResult(listOf(role))
            .withUserLookupResult(listOf(user))
            .build()

        // 'c' matches `channel` (prefix) but not `here`.
        val result = fixture.handler.handleMentionLookup(query = "c")

        assertEquals(
            listOf(
                Mention.Channel,
                Mention.Role(role.name),
                Mention.Group(group),
                Mention.User(user),
            ),
            result,
        )
    }

    @Test
    fun `Non-user mentions without the matching capability are skipped`() = runTest {
        val user = User(id = "u1", name = "Alice")
        val fixture = Fixture()
            .withGroupSearchResult(listOf(UserGroup(id = "g1", name = "platform")))
            .withRoleSearchResult(listOf(Role(name = "admin")))
            .withUserLookupResult(listOf(user))
            .withOwnCapabilities(emptySet())
            .build()

        val result = fixture.handler.handleMentionLookup(query = "a")

        assertEquals(listOf(Mention.User(user)), result)
        verify(fixture.chatClient, never()).searchUserGroups(any(), any(), any(), any(), any())
        verify(fixture.chatClient, never()).searchRoles(any(), any(), any(), any(), any())
    }

    @Test
    fun `Group search forwards the channel's team when present`() = runTest {
        val fixture = Fixture().withTeam("ops").build()

        fixture.handler.handleMentionLookup(query = "plat")

        verify(fixture.chatClient).searchUserGroups(eq("plat"), anyOrNull(), eq("ops"), anyOrNull(), anyOrNull())
    }

    private class Fixture {
        private val chatClient: ChatClient = mock()
        private var groupSearchResult: List<UserGroup> = emptyList()
        private var roleSearchResult: List<Role> = emptyList()
        private var userLookupResult: List<User> = emptyList()
        private var ownCapabilities: Set<String> = setOf(
            ChannelCapabilities.CREATE_MENTION,
            ChannelCapabilities.NOTIFY_CHANNEL,
            ChannelCapabilities.NOTIFY_HERE,
            ChannelCapabilities.NOTIFY_ROLE,
            ChannelCapabilities.NOTIFY_GROUP,
        )
        private var team: String = ""

        fun withGroupSearchResult(groups: List<UserGroup>) = apply { groupSearchResult = groups }
        fun withRoleSearchResult(roles: List<Role>) = apply { roleSearchResult = roles }
        fun withUserLookupResult(users: List<User>) = apply { userLookupResult = users }
        fun withOwnCapabilities(capabilities: Set<String>) = apply { ownCapabilities = capabilities }
        fun withTeam(team: String) = apply { this.team = team }

        fun build(): Bundle {
            whenever(chatClient.searchUserGroups(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
                .thenReturn(groupSearchResult.asCall())
            whenever(chatClient.searchRoles(any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()))
                .thenReturn(roleSearchResult.asCall())
            val channelData = ChannelData(
                id = "c1",
                type = "messaging",
                team = team,
                ownCapabilities = ownCapabilities,
            )
            val state: ChannelState = mock()
            whenever(state.channelData).thenReturn(MutableStateFlow(channelData))
            val handler = MentionLookupHandler(
                chatClient = chatClient,
                channelState = MutableStateFlow(state),
                userLookupHandler = { userLookupResult },
            )
            return Bundle(chatClient, handler)
        }
    }

    private data class Bundle(val chatClient: ChatClient, val handler: MentionLookupHandler)
}
