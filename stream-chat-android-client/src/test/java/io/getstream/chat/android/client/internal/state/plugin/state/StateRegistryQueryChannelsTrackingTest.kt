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

package io.getstream.chat.android.client.internal.state.plugin.state

import io.getstream.chat.android.client.api.MessageLimitConfig
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.internal.state.plugin.state.internal.TrackedQueryChannelsStateFlow
import io.getstream.chat.android.client.utils.awaitCollected
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.lang.ref.WeakReference

internal class StateRegistryQueryChannelsTrackingTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var stateRegistry: StateRegistry

    @BeforeEach
    fun setUp() {
        stateRegistry = StateRegistry(
            userStateFlow = MutableStateFlow(null),
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
            job = Job(),
            now = { System.currentTimeMillis() },
            scope = testCoroutines.scope,
            messageLimitConfig = MessageLimitConfig(),
        )
    }

    @Test
    fun `getTrackedQueryChannels should return empty list when nothing tracked`() {
        assertTrue(stateRegistry.getTrackedQueryChannels().isEmpty())
    }

    @Test
    fun `getTrackedQueryChannels should return the most recently tracked lists first`() {
        val first = trackedQueryChannelsFlow("first")
        val second = trackedQueryChannelsFlow("second")
        stateRegistry.trackQueryChannels(first)
        stateRegistry.trackQueryChannels(second)

        assertEquals(listOf(second.identifier, first.identifier), stateRegistry.getTrackedQueryChannels())
    }

    @Test
    fun `trackQueryChannels again should move the list to the front`() {
        val first = trackedQueryChannelsFlow("first")
        val second = trackedQueryChannelsFlow("second")
        stateRegistry.trackQueryChannels(first)
        stateRegistry.trackQueryChannels(second)
        stateRegistry.trackQueryChannels(first)

        assertEquals(listOf(first.identifier, second.identifier), stateRegistry.getTrackedQueryChannels())
    }

    @Test
    fun `getTrackedQueryChannels should deduplicate identifiers from multiple flows`() {
        val older = trackedQueryChannelsFlow("list")
        val other = trackedQueryChannelsFlow("other")
        val newer = trackedQueryChannelsFlow("list")
        stateRegistry.trackQueryChannels(older)
        stateRegistry.trackQueryChannels(other)
        stateRegistry.trackQueryChannels(newer)

        assertEquals(listOf(newer.identifier, other.identifier), stateRegistry.getTrackedQueryChannels())
    }

    @Test
    fun `clear should remove all tracked query channels`() {
        stateRegistry.trackQueryChannels(trackedQueryChannelsFlow("list"))

        stateRegistry.clear()

        assertTrue(stateRegistry.getTrackedQueryChannels().isEmpty())
    }

    @Test
    fun `getTrackedQueryChannels should drop a list once its flow is released`() {
        val kept = trackedQueryChannelsFlow("kept")
        stateRegistry.trackQueryChannels(kept)
        val releasedRef = trackAndRelease("released")

        awaitCollected(releasedRef)

        assertNull(releasedRef.get())
        assertEquals(listOf(kept.identifier), stateRegistry.getTrackedQueryChannels())
    }

    /** Tracks a new flow and keeps only a weak reference to it, so nothing else holds it. */
    private fun trackAndRelease(name: String): WeakReference<TrackedQueryChannelsStateFlow> {
        val flow = trackedQueryChannelsFlow(name)
        stateRegistry.trackQueryChannels(flow)
        return WeakReference(flow)
    }

    @Test
    fun `getTrackedQueryChannels should skip a list whose scope is no longer active`() {
        val openJob = Job()
        val replacedJob = Job()
        val open = trackedQueryChannelsFlow("open", openJob)
        val replaced = trackedQueryChannelsFlow("replaced", replacedJob)
        stateRegistry.trackQueryChannels(open)
        stateRegistry.trackQueryChannels(replaced)

        replacedJob.cancel()

        assertEquals(listOf(open.identifier), stateRegistry.getTrackedQueryChannels())
    }

    @Test
    fun `getTrackedQueryChannels should keep a list created without a scope job`() {
        val flow = trackedQueryChannelsFlow("list", scopeJob = null)
        stateRegistry.trackQueryChannels(flow)

        assertEquals(listOf(flow.identifier), stateRegistry.getTrackedQueryChannels())
    }

    private fun trackedQueryChannelsFlow(name: String, scopeJob: Job? = null) = TrackedQueryChannelsStateFlow(
        identifier = QueryChannelsIdentifier.Standard(Filters.eq("name", name), QuerySortByField.descByName("name")),
        scopeJob = scopeJob,
    ) { MutableStateFlow(null) }
}
