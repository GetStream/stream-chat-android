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

package io.getstream.chat.android.client.internal.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.extensions.internal.toCid
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.client.query.QueryChannelsSpec
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomCID
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomMember
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomString
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
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
    private val testChannelId = ChannelId.fromCid(testCid)!!

    private val queryChannelsSpec =
        QueryChannelsSpec(Filters.neutral(), QuerySortByField.descByName("")).also { it.cids = setOf(testCid) }

    private val mutableState: QueryChannelsMutableState = mock {
        on(it.rawChannels) doReturn emptyMap()
        on(it.queryChannelsSpec) doReturn queryChannelsSpec
    }
    private val stateRegistry: StateRegistry = mock()
    private val logicRegistry: LogicRegistry = mock {
        on(it.channel(any(), any())) doReturn mock()
        on(it.channel(any<ChannelId>())) doReturn mock()
    }

    private val queryChannelsStateLogic =
        QueryChannelsStateLogic(mutableState, stateRegistry, logicRegistry, testCoroutines.scope)

    @Test
    fun `when a channel is inside the query spec and it is refreshed, it should be added`() {
        val channel = randomChannel(type = type, id = id)
        val channelState: ChannelState = mock {
            on(it.toChannel()) doReturn channel
        }

        whenever(stateRegistry.isActiveChannel(testChannelId)) doReturn true
        whenever(stateRegistry.channel(testChannelId)) doReturn channelState

        queryChannelsStateLogic.refreshChannels(listOf(testCid))

        verify(mutableState).setChannels(mapOf(testCid to channel))
    }

    @Test
    fun `when a channel is NOT inside the query spec and it is refreshed, it should NOT be added`() {
        val channel = randomChannel(type = type, id = id)
        val channelState: ChannelState = mock {
            on(it.toChannel()) doReturn channel
        }

        val cidOutsideSpecs = randomCID()

        whenever(stateRegistry.isActiveChannel(testChannelId)) doReturn true
        whenever(stateRegistry.channel(testChannelId)) doReturn channelState

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

        whenever(stateRegistry.isActiveChannel(testChannelId)) doReturn false
        whenever(stateRegistry.channel(testChannelId)) doReturn channelState

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

        verify(mutableState).setCids(setOf(testCid, channel1.cid, channel2.cid))
        verify(mutableState).setChannels(channels.associateBy { it.cid })
    }

    @Test
    fun `getActiveChannelState should return channel when it is active in state registry`() {
        val channel = randomChannel(type = type, id = id)
        val channelState: ChannelState = mock {
            on(it.toChannel()) doReturn channel
        }

        whenever(stateRegistry.isActiveChannel(testChannelId)) doReturn true
        whenever(stateRegistry.channel(testChannelId)) doReturn channelState

        val result = queryChannelsStateLogic.getActiveChannelState(testCid)

        assertEquals(channel, result)
    }

    @Test
    fun `getActiveChannelState should return null when channel is not active in state registry`() {
        whenever(stateRegistry.isActiveChannel(testChannelId)) doReturn false

        val result = queryChannelsStateLogic.getActiveChannelState(testCid)

        assertNull(result)
    }

    @Test
    fun `getActiveChannelState should return null when cid is malformed`() {
        assertNull(queryChannelsStateLogic.getActiveChannelState("not-a-cid"))
    }

    @Test
    fun `addChannelsState drops channels with malformed cid`() = runTest {
        val valid = randomChannel(type = "messaging", id = "valid-${randomString()}")
        val malformed = randomChannel(type = "", id = "x")
        whenever(mutableState.queryChannelsSpec) doReturn queryChannelsSpec

        queryChannelsStateLogic.addChannelsState(listOf(valid, malformed))

        verify(mutableState).setCids(setOf(testCid, valid.cid))
        verify(mutableState).setChannels(mapOf(valid.cid to valid))
        verify(logicRegistry).channel(ChannelId.fromCid(valid.cid)!!)
    }

    @Test
    fun `refreshChannels skips malformed cids in the spec intersection`() {
        val malformedCid = "no-colon-cid"
        val specWithMalformed = QueryChannelsSpec(Filters.neutral(), QuerySortByField.descByName(""))
            .also { it.cids = setOf(testCid, malformedCid) }
        whenever(mutableState.queryChannelsSpec) doReturn specWithMalformed
        whenever(mutableState.rawChannels) doReturn emptyMap()
        whenever(stateRegistry.isActiveChannel(testChannelId)) doReturn false

        queryChannelsStateLogic.refreshChannels(listOf(testCid, malformedCid))

        verify(mutableState).setChannels(emptyMap())
    }

    // region Delegation

    @Test
    fun `setLoadingMore delegates to mutableState`() {
        queryChannelsStateLogic.setLoadingMore(true)
        verify(mutableState).setLoadingMore(true)
    }

    @Test
    fun `setLoadingFirstPage delegates to mutableState`() {
        queryChannelsStateLogic.setLoadingFirstPage(true)
        verify(mutableState).setLoadingFirstPage(true)
    }

    @Test
    fun `setCurrentRequest delegates to mutableState`() {
        val request = QueryChannelsRequest(filter = Filters.neutral(), limit = 30)
        queryChannelsStateLogic.setCurrentRequest(request)
        verify(mutableState).setCurrentRequest(request)
    }

    @Test
    fun `setEndOfChannels delegates to mutableState`() {
        queryChannelsStateLogic.setEndOfChannels(true)
        verify(mutableState).setEndOfChannels(true)
    }

    @Test
    fun `setRecoveryNeeded delegates to mutableState`() {
        queryChannelsStateLogic.setRecoveryNeeded(true)
        verify(mutableState).setRecoveryNeeded(true)
    }

    // endregion

    // region removeChannels

    @Test
    fun `removeChannels removes cids from spec and channels from state`() {
        val chA = randomChannel(type = "messaging", id = "a")
        val chB = randomChannel(type = "messaging", id = "b")
        val chC = randomChannel(type = "messaging", id = "c")
        val channels = mapOf(chA.cid to chA, chB.cid to chB, chC.cid to chC)
        val spec = QueryChannelsSpec(
            filter = Filters.neutral(),
            querySort = QuerySortByField.descByName(""),
        ).also { it.cids = setOf(chA.cid, chB.cid, chC.cid) }

        whenever(mutableState.rawChannels) doReturn channels
        whenever(mutableState.queryChannelsSpec) doReturn spec

        val logic = QueryChannelsStateLogic(mutableState, stateRegistry, logicRegistry, testCoroutines.scope)
        logic.removeChannels(setOf(chA.cid, chC.cid))

        verify(mutableState).setCids(setOf(chB.cid))
        verify(mutableState).setChannels(mapOf(chB.cid to chB))
    }

    @Test
    fun `removeChannels is no-op when rawChannels is null`() {
        whenever(mutableState.rawChannels) doReturn null

        queryChannelsStateLogic.removeChannels(setOf("messaging:x"))

        verify(mutableState, never()).setChannels(any())
    }

    // endregion

    // region initializeChannelsIfNeeded

    @Test
    fun `initializeChannelsIfNeeded sets empty map when rawChannels is null`() {
        whenever(mutableState.rawChannels) doReturn null

        queryChannelsStateLogic.initializeChannelsIfNeeded()

        verify(mutableState).setChannels(emptyMap())
    }

    @Test
    fun `initializeChannelsIfNeeded does not overwrite when rawChannels is already set`() {
        val existing = mapOf("messaging:ch" to randomChannel())
        whenever(mutableState.rawChannels) doReturn existing

        queryChannelsStateLogic.initializeChannelsIfNeeded()

        verify(mutableState, never()).setChannels(any())
    }

    // endregion

    // region incrementChannelsOffset

    @Test
    fun `incrementChannelsOffset adds size to current offset`() {
        whenever(mutableState.channelsOffset) doReturn MutableStateFlow(10)

        queryChannelsStateLogic.incrementChannelsOffset(5)

        verify(mutableState).setChannelsOffset(15)
    }

    // endregion

    // region addChannelsState edge cases

    @Test
    fun `addChannelsState merges messages from existing channels`() = runTest {
        val msg1 = randomMessage(id = "m1")
        val msg2 = randomMessage(id = "m2")
        val channelType = "messaging"
        val channelId = "ch1"
        val cid = "$channelType:$channelId"
        val existingChannel = randomChannel(type = channelType, id = channelId).copy(messages = listOf(msg1))
        val newChannel = existingChannel.copy(messages = listOf(msg2))
        whenever(mutableState.rawChannels) doReturn mapOf(cid to existingChannel)

        queryChannelsStateLogic.addChannelsState(listOf(newChannel))

        val captor = argumentCaptor<Map<String, Channel>>()
        verify(mutableState).setChannels(captor.capture())
        val merged = captor.firstValue[cid]!!
        val messageIds = merged.messages.map { it.id }.toSet()
        assertTrue(messageIds.contains("m1"))
        assertTrue(messageIds.contains("m2"))
    }

    @Test
    fun `addChannelsState deduplicates messages by id`() = runTest {
        val sharedMsg = randomMessage(id = "shared")
        val channelType = "messaging"
        val channelId = "ch1"
        val cid = "$channelType:$channelId"
        val existingChannel = randomChannel(type = channelType, id = channelId).copy(messages = listOf(sharedMsg))
        val newChannel = existingChannel.copy(messages = listOf(sharedMsg.copy(text = "updated")))
        whenever(mutableState.rawChannels) doReturn mapOf(cid to existingChannel)

        queryChannelsStateLogic.addChannelsState(listOf(newChannel))

        val captor = argumentCaptor<Map<String, Channel>>()
        verify(mutableState).setChannels(captor.capture())
        val merged = captor.firstValue[cid]!!
        assertEquals(1, merged.messages.count { it.id == "shared" })
    }

    @Test
    fun `addChannelsState merges members when total does not exceed memberCount`() = runTest {
        val userA = randomUser(id = "userA")
        val userB = randomUser(id = "userB")
        val memberA = randomMember(user = userA)
        val memberB = randomMember(user = userB)
        val channelType = "messaging"
        val channelId = "ch1"
        val cid = "$channelType:$channelId"
        val existingChannel = randomChannel(type = channelType, id = channelId).copy(
            members = listOf(memberA),
            memberCount = 10,
        )
        val newChannel = existingChannel.copy(
            members = listOf(memberB),
            memberCount = 10,
        )
        whenever(mutableState.rawChannels) doReturn mapOf(cid to existingChannel)

        queryChannelsStateLogic.addChannelsState(listOf(newChannel))

        val captor = argumentCaptor<Map<String, Channel>>()
        verify(mutableState).setChannels(captor.capture())
        val merged = captor.firstValue[cid]!!
        val memberUserIds = merged.members.map { it.getUserId() }.toSet()
        assertTrue(memberUserIds.contains("userA"))
        assertTrue(memberUserIds.contains("userB"))
    }

    // endregion
}
