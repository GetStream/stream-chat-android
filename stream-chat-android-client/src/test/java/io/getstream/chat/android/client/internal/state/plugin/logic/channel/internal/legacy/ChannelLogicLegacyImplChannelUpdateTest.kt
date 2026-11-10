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

package io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.legacy

import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.SearchLogic
import io.getstream.chat.android.client.internal.state.plugin.state.channel.internal.ChannelStateLegacyImpl
import io.getstream.chat.android.client.internal.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.client.test.randomMemberAddedEvent
import io.getstream.chat.android.models.ChannelData
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import java.util.Date

internal class ChannelLogicLegacyImplChannelUpdateTest {

    private lateinit var state: ChannelStateLegacyImpl
    private lateinit var sut: ChannelLogicLegacyImpl

    @BeforeEach
    fun setUp() {
        val userFlow = MutableStateFlow<User?>(User(id = CURRENT_USER_ID))
        state = ChannelStateLegacyImpl(
            channelType = CHANNEL_TYPE,
            channelId = CHANNEL_ID,
            userFlow = userFlow,
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
            baseMessageLimit = null,
            now = { System.currentTimeMillis() },
        )
        val clientState = mock<ClientState> { on(it.user) doReturn userFlow }
        val stateLogic = ChannelStateLogic(
            clientState = clientState,
            mutableState = state,
            globalMutableState = MutableGlobalState(CURRENT_USER_ID),
            searchLogic = SearchLogic(state),
            coroutineScope = testCoroutines.scope,
        )
        sut = ChannelLogicLegacyImpl(
            repos = mock(),
            userPresence = false,
            stateLogic = stateLogic,
            coroutineScope = testCoroutines.scope,
            getCurrentUserId = { CURRENT_USER_ID },
        )
    }

    @Test
    fun `member added event after the add response does not count the member twice`() {
        val john = randomMember(user = randomUser(id = "john"))
        val jane = randomMember(user = randomUser(id = "jane"))
        state.setMembers(listOf(john), membersCount = 1)

        sut.applyChannelUpdate(channel(members = listOf(john, jane), memberCount = 2), withMembers = true)
        sut.handleEvent(randomMemberAddedEvent(cid = CID, channelType = CHANNEL_TYPE, channelId = CHANNEL_ID, member = jane))

        assertEquals(2, state.membersCount.value)
        assertEquals(setOf("john", "jane"), state.members.value.map { it.getUserId() }.toSet())
    }

    @Test
    fun `an update response keeps the members and the later last message date`() {
        val john = randomMember(user = randomUser(id = "john"))
        state.setMembers(listOf(john), membersCount = 1)
        state.setChannelData(ChannelData(id = CHANNEL_ID, type = CHANNEL_TYPE, lastMessageAt = Date(5_000)))

        sut.applyChannelUpdate(
            channel(name = "new", members = emptyList(), memberCount = 0).copy(lastMessageAt = Date(4_000)),
            withMembers = false,
        )

        assertEquals("new", state.channelData.value.name)
        assertEquals(Date(5_000), state.channelData.value.lastMessageAt)
        assertEquals(1, state.membersCount.value)
        assertEquals(listOf("john"), state.members.value.map { it.getUserId() })
    }

    private fun channel(name: String = "name", members: List<Member>, memberCount: Int) = randomChannel(
        type = CHANNEL_TYPE,
        id = CHANNEL_ID,
        name = name,
        members = members,
        memberCount = memberCount,
        truncatedAt = null,
        deletedAt = null,
    )

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "$CHANNEL_TYPE:$CHANNEL_ID"
        private const val CURRENT_USER_ID = "currentUser"
    }
}
