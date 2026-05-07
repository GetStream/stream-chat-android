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

package io.getstream.chat.android.client.internal.state.plugin.state.querychannels.internal

import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class QueryChannelsMutableStateTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val initialFilter = Filters.eq("type", "messaging")
    private val initialSort = QuerySortByField.descByName<Channel>("last_message_at")

    private fun newState(
        identifier: QueryChannelsIdentifier = QueryChannelsIdentifier.Standard(initialFilter, initialSort),
    ) = QueryChannelsMutableState(
        identifier = identifier,
        initialFilter = initialFilter,
        initialSort = initialSort,
        scope = testCoroutines.scope,
        latestUsers = MutableStateFlow(emptyMap()),
        activeLiveLocations = MutableStateFlow(emptyList()),
    )

    private val predefinedIdentifier = QueryChannelsIdentifier.Predefined(
        name = "predefined",
        filterValues = null,
        sortValues = null,
    )

    @Test
    fun `applyResolvedSpec updates filter and sort accessors`() {
        val state = newState(identifier = predefinedIdentifier)
        val newFilter = Filters.eq("type", "team")
        val newSort = QuerySortByField.ascByName<Channel>("name")

        state.applyResolvedSpec(newFilter, newSort)

        assertEquals(newFilter, state.filter)
        assertEquals(newSort, state.sort)
    }

    @Test
    fun `applyResolvedSpec updates the in-memory queryChannelsSpec`() {
        val state = newState(identifier = predefinedIdentifier)
        val newFilter = Filters.eq("type", "team")
        val newSort = QuerySortByField.ascByName<Channel>("name")

        state.applyResolvedSpec(newFilter, newSort)

        assertEquals(newFilter, state.queryChannelsSpec.filter)
        assertEquals(newSort, state.queryChannelsSpec.querySort)
    }

    @Test
    fun `applyResolvedSpec re-sorts the channels flow with the new comparator`() {
        // Given a predefined-identifier state seeded with channels sorted descending by name.
        val descByName = QuerySortByField.descByName<Channel>("name")
        val descState = QueryChannelsMutableState(
            identifier = predefinedIdentifier,
            initialFilter = initialFilter,
            initialSort = descByName,
            scope = testCoroutines.scope,
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
        )
        val a = randomChannel(id = "a", type = "messaging", name = "alpha")
        val b = randomChannel(id = "b", type = "messaging", name = "bravo")
        val c = randomChannel(id = "c", type = "messaging", name = "charlie")
        descState.setChannels(mapOf(a.cid to a, b.cid to b, c.cid to c))

        val sortedDesc = descState.channels.value!!.map { it.name }
        assertEquals(listOf("charlie", "bravo", "alpha"), sortedDesc)

        // When the resolved sort flips to ascending, channels re-emit in the new order.
        val ascByName = QuerySortByField.ascByName<Channel>("name")
        descState.applyResolvedSpec(initialFilter, ascByName)

        val sortedAsc = descState.channels.value!!.map { it.name }
        assertEquals(listOf("alpha", "bravo", "charlie"), sortedAsc)
    }

    @Test
    fun `applyResolvedSpec is a no-op for Standard identifier`() {
        val state = newState(identifier = QueryChannelsIdentifier.Standard(initialFilter, initialSort))
        val newFilter = Filters.eq("type", "team")
        val newSort = QuerySortByField.ascByName<Channel>("name")

        state.applyResolvedSpec(newFilter, newSort)

        assertEquals(initialFilter, state.filter)
        assertEquals(initialSort, state.sort)
        assertEquals(initialFilter, state.queryChannelsSpec.filter)
        assertEquals(initialSort, state.queryChannelsSpec.querySort)
    }

    @Test
    fun `predefined identifier wires predefined fields into the spec`() {
        val identifier = QueryChannelsIdentifier.Predefined(
            name = "p",
            filterValues = mapOf("a" to 1),
            sortValues = mapOf("b" to 2),
        )

        val state = newState(identifier = identifier)

        assertEquals("p", state.queryChannelsSpec.predefinedFilterName)
        assertEquals(mapOf("a" to 1), state.queryChannelsSpec.predefinedFilterValues)
        assertEquals(mapOf("b" to 2), state.queryChannelsSpec.predefinedSortValues)
    }
}
