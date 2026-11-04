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

import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.channel.state.ChannelState
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal.QueryChannelsMutableState
import io.getstream.chat.android.client.utils.internal.ChannelId
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

/**
 * Events, query results and message listeners change a list from different threads. A change that starts while
 * another one is between reading the list and writing it back must not be lost.
 */
internal class QueryChannelsStateLogicConcurrentChangesTest {

    private val listed = randomChannel(type = "messaging", id = "listed")
    private val dropped = randomChannel(type = "messaging", id = "dropped")
    private val newChannel = randomChannel(type = "messaging", id = "new")

    private val listState = spy(
        QueryChannelsMutableState(
            identifier = QueryChannelsIdentifier.Grouped("all"),
            scope = TestScope(),
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
        ),
    )
    private val listedState: ChannelState = mock {
        on { toChannel() } doReturn listed.copy(name = "refreshed")
    }
    private val stateRegistry: StateRegistry = mock {
        on { isActiveChannel(any()) } doReturn true
        on { activeChannel(ChannelId.fromCid(listed.cid)!!) } doReturn listedState
    }
    private val stateLogic = QueryChannelsStateLogic(
        listState,
        stateRegistry,
        mock(),
        TestScope(),
        isLocalUnreadCountEnabled = false,
    )

    @Test
    fun `a channel tracked while channels are refreshed stays listed`() {
        stateLogic.trackChannel(listed)

        whileWritingTheList(otherChange = { stateLogic.trackChannel(newChannel) }) {
            stateLogic.refreshChannels(listOf(listed.cid))
        }

        assertEquals(setOf(listed.cid, newChannel.cid), listState.rawChannels.orEmpty().keys)
        assertEquals("refreshed", listState.rawChannels.orEmpty().getValue(listed.cid).name)
    }

    @Test
    fun `a channel tracked while another is removed stays tracked`() {
        stateLogic.trackChannel(listed)
        stateLogic.trackChannel(dropped)

        whileWritingTheCids(otherChange = { stateLogic.trackChannel(newChannel) }) {
            stateLogic.removeChannels(setOf(dropped.cid))
        }

        assertEquals(setOf(listed.cid, newChannel.cid), listState.queryChannelsSpec.cids)
        assertEquals(setOf(listed.cid, newChannel.cid), listState.rawChannels.orEmpty().keys)
    }

    @Test
    fun `a channel tracked while the list is reset to a first page stays listed and tracked`() {
        stateLogic.trackChannel(listed)
        stateLogic.trackChannel(dropped)

        whileWritingTheList(otherChange = { stateLogic.trackChannel(newChannel) }) {
            stateLogic.resetToPage(setOf(dropped.cid))
        }

        assertEquals(setOf(listed.cid, newChannel.cid), listState.rawChannels.orEmpty().keys)
        assertEquals(setOf(listed.cid, newChannel.cid), listState.queryChannelsSpec.cids)
    }

    @Test
    fun `a channel kept by a first-page reset keeps its copy and is still refreshed`() {
        stateLogic.trackChannel(listed)
        stateLogic.trackChannel(dropped)

        stateLogic.resetToPage(setOf(dropped.cid))

        assertEquals(mapOf(listed.cid to listed), listState.rawChannels)
        stateLogic.refreshChannels(listOf(listed.cid))
        assertEquals("refreshed", listState.rawChannels.orEmpty().getValue(listed.cid).name)
    }

    @Test
    fun `members refreshed while a channel is tracked keep the tracked channel`() {
        stateLogic.trackChannel(listed)

        whileWritingTheList(otherChange = { stateLogic.trackChannel(newChannel) }) {
            stateLogic.refreshMembersStateForUser(randomUser())
        }

        assertEquals(setOf(listed.cid, newChannel.cid), listState.rawChannels.orEmpty().keys)
    }

    /** Runs [change], starting [otherChange] on another thread when [change] first writes the channel list. */
    private fun whileWritingTheList(otherChange: () -> Unit, change: () -> Unit) {
        val other = OtherChange(otherChange)
        doAnswer {
            other.start()
            it.callRealMethod()
        }.whenever(listState).setChannels(any())
        change()
        other.finish()
    }

    /** Runs [change], starting [otherChange] on another thread when [change] first writes the cids. */
    private fun whileWritingTheCids(otherChange: () -> Unit, change: () -> Unit) {
        val other = OtherChange(otherChange)
        doAnswer {
            other.start()
            it.callRealMethod()
        }.whenever(listState).setCids(any())
        change()
        other.finish()
    }

    /**
     * A change on another thread. [start] runs it until it either finishes or waits for the change in progress, so
     * the change in progress writes after it in the first case.
     */
    private class OtherChange(private val action: () -> Unit) {
        private val started = AtomicBoolean(false)
        private var thread: Thread? = null
        private var failure: Throwable? = null

        fun start() {
            if (!started.compareAndSet(false, true)) return
            val other = thread { runCatching(action).onFailure { failure = it } }
            thread = other
            val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
            while (other.state != Thread.State.BLOCKED && other.state != Thread.State.TERMINATED) {
                check(System.nanoTime() < deadline) { "The other change neither finished nor waited" }
                Thread.onSpinWait()
            }
        }

        fun finish() {
            val other = checkNotNull(thread) { "The other change never started" }
            other.join(TimeUnit.SECONDS.toMillis(5))
            check(!other.isAlive) { "The other change did not finish" }
            failure?.let { throw it }
        }
    }
}
