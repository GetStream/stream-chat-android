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

package io.getstream.chat.android.client.internal.state.plugin.listener.internal

import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal.QueryChannelsLogic
import io.getstream.chat.android.client.query.CreateChannelParams
import io.getstream.chat.android.models.MemberData
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomUser
import io.getstream.result.Error
import io.getstream.result.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

internal class CreateChannelListenerStateTest {

    private val queryChannelsLogic: QueryChannelsLogic = mock {
        on { it.cids() } doReturn setOf("messaging:!members-a, b")
    }
    private val otherQueryChannelsLogic: QueryChannelsLogic = mock {
        on { it.cids() } doReturn setOf("messaging:general")
    }
    private val logic: LogicRegistry = mock {
        on { it.getActiveQueryChannelsLogic() } doReturn listOf(queryChannelsLogic, otherQueryChannelsLogic)
    }
    private val stateRegistry: StateRegistry = mock()
    private val sut = CreateChannelListenerState(logic, stateRegistry)

    @Test
    fun `creating a channel without an id records its placeholder`() = runTest {
        val params = CreateChannelParams(members = listOf(MemberData("b"), MemberData("a")), extraData = emptyMap())

        sut.onCreateChannelRequest("messaging", "", params, randomUser())

        verify(logic).markPlaceholderChannel("messaging:!members-a, b")
    }

    @Test
    fun `creating a channel with an id records no placeholder`() = runTest {
        val params = CreateChannelParams(members = listOf(MemberData("a")), extraData = emptyMap())

        sut.onCreateChannelRequest("messaging", "general", params, randomUser())

        verify(logic, never()).markPlaceholderChannel(any())
    }

    @Test
    fun `the server creating the channel drops the placeholder from state`() = runTest {
        val serverChannel = randomChannel(type = "messaging", id = "!members-hash", hidden = false)

        sut.onCreateChannelResult("messaging", "", listOf("b", "a"), Result.Success(serverChannel))

        verify(logic).removeChannel("messaging", "!members-a, b")
        verify(stateRegistry).removeChannel("messaging", "!members-a, b")
        verify(queryChannelsLogic).removeChannel("messaging:!members-a, b")
        verify(queryChannelsLogic).addChannelFromEvent(serverChannel)
        verify(otherQueryChannelsLogic, never()).removeChannel(any())
        verify(otherQueryChannelsLogic, never()).addChannelFromEvent(any())
    }

    @Test
    fun `a server channel the user hid is not added in place of the placeholder`() = runTest {
        val serverChannel = randomChannel(type = "messaging", id = "!members-hash", hidden = true)

        sut.onCreateChannelResult("messaging", "", listOf("b", "a"), Result.Success(serverChannel))

        verify(queryChannelsLogic).removeChannel("messaging:!members-a, b")
        verify(queryChannelsLogic, never()).addChannelFromEvent(any())
    }

    @Test
    fun `a channel created with its own id keeps its state`() = runTest {
        val channel = randomChannel(type = "messaging", id = "general")

        sut.onCreateChannelResult("messaging", "general", listOf("a"), Result.Success(channel))

        verify(logic, never()).removeChannel(any(), any())
    }

    @Test
    fun `a failed create keeps the placeholder`() = runTest {
        sut.onCreateChannelResult("messaging", "", listOf("a", "b"), Result.Failure(Error.GenericError("offline")))

        verify(logic, never()).removeChannel(any(), any())
    }
}
