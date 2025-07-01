/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should contain same`
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsStateLogicTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private val type = randomString()
    private val id = randomString()
    private val testCid = (type to id).toCid()

    private val queryChannelsSpec =
        QueryChannelsSpec(Filters.neutral(), QuerySortByField.descByName(""))
            .apply {
                cids = setOf(testCid)
            }

    private val mutableState: QueryChannelsMutableState = mock {
        on(it.rawChannels) doReturn emptyMap()
        on(it.queryChannelsSpec) doReturn queryChannelsSpec
    }
    private val mutableGlobalState: MutableGlobalState = mock()
    private val stateRegistry: StateRegistry = mock()
    private val logicRegistry: LogicRegistry = mock {
        on(it.channelState(any(), any())) doReturn mock()
    }

    private val queryChannelsStateLogic =
        QueryChannelsStateLogic(mutableState, mutableGlobalState, stateRegistry, logicRegistry, testCoroutines.scope)

    @Test
    fun `when a channel is inside the query spec and it is refreshed, it should be added`() {
        val channel = randomChannel(type = type, id = id)
        val channelState: ChannelState = mock {
            on(it.toChannel()) doReturn channel
        }

        val (channelType, channelId) = testCid.cidToTypeAndId()

        whenever(stateRegistry.isActiveChannel(channelType, channelId)) doReturn true
        whenever(stateRegistry.channel(channelType, channelId)) doReturn channelState

        queryChannelsStateLogic.refreshChannels(listOf(testCid))

        verify(mutableState).setChannels(mapOf(testCid to channel))
    }

    @Test
    fun `when a channel is NOT inside the query spec and it is refreshed, it should NOT be added`() {
        val channel = randomChannel(type = type, id = id)
        val channelState: ChannelState = mock {
            on(it.toChannel()) doReturn channel
        }

        val (channelType, channelId) = testCid.cidToTypeAndId()
        val cidOutsideSpecs = randomCID()

        whenever(stateRegistry.isActiveChannel(channel.type, channel.id)) doReturn true
        whenever(stateRegistry.channel(channelType, channelId)) doReturn channelState

        queryChannelsStateLogic.refreshChannels(listOf(cidOutsideSpecs))

        verify(mutableState, never()).setChannels(mapOf(testCid to channel))
    }

    @Test
    fun `when a channel is inside the query spec and it is refreshed, but is not active, it should NOT be added`() {
        val channel = randomChannel(type = type, id = id)
        val channelState: ChannelState = mock {
            on(it.toChannel()) doReturn channel
        }

        val cidOutsideSpecs = randomCID()
        val (channelType, channelId) = testCid.cidToTypeAndId()

        whenever(stateRegistry.isActiveChannel(channel.type, channel.id)) doReturn false
        whenever(stateRegistry.channel(channelType, channelId)) doReturn channelState

        queryChannelsStateLogic.refreshChannels(listOf(cidOutsideSpecs))

        verify(mutableState, never()).setChannels(mapOf(testCid to channel))
    }

    @Test
    fun `when adding channel state both specs and channels should be added`() = runTest {
        val channel1 = randomChannel()
        val channel2 = randomChannel()
        val channels = listOf(channel1, channel2)

        whenever(mutableState.queryChannelsSpec) doReturn queryChannelsSpec

        queryChannelsStateLogic.addChannelsState(channels)

        queryChannelsSpec.cids `should contain same` setOf(testCid, channel1.cid, channel2.cid)
        verify(mutableState).setChannels(channels.associateBy { it.cid })
    }
}
