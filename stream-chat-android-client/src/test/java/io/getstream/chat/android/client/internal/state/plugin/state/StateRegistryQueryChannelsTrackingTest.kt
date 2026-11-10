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
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

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

    private fun trackedQueryChannelsFlow(name: String) = TrackedQueryChannelsStateFlow(
        identifier = QueryChannelsIdentifier.Standard(Filters.eq("name", name), QuerySortByField.descByName("name")),
    ) { MutableStateFlow(null) }
}
