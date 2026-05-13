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

package io.getstream.chat.android.state.plugin.state.querychannels.internal

import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.Location
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class QueryChannelsMutableStateTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private val filter = Filters.eq("type", "messaging")
    private val sort = QuerySortByField.descByName<Channel>("last_message_at")
    private val identifier = io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
        .Standard(filter, sort)
    private val latestUsers = MutableStateFlow<Map<String, User>>(emptyMap())
    private val activeLiveLocations = MutableStateFlow<List<Location>>(emptyList())

    private lateinit var state: QueryChannelsMutableState

    @BeforeEach
    fun setUp() {
        state = QueryChannelsMutableState(
            identifier = identifier,
            scope = testCoroutines.scope,
            latestUsers = latestUsers,
            activeLiveLocations = activeLiveLocations,
        )
    }

    // region Setters

    @Test
    fun `setLoadingMore updates loadingMore flow`() = runTest {
        assertFalse(state.loadingMore.value)
        state.setLoadingMore(true)
        assertTrue(state.loadingMore.value)
    }

    @Test
    fun `setLoadingFirstPage updates loading flow`() = runTest {
        assertFalse(state.loading.value)
        state.setLoadingFirstPage(true)
        assertTrue(state.loading.value)
    }

    @Test
    fun `setCurrentRequest updates currentRequest flow`() = runTest {
        assertNull(state.currentRequest.value)
        val request = QueryChannelsRequest(filter = filter, limit = 30, querySort = sort)
        state.setCurrentRequest(request)
        assertEquals(request, state.currentRequest.value)
    }

    @Test
    fun `setEndOfChannels updates endOfChannels flow`() = runTest {
        assertFalse(state.endOfChannels.value)
        state.setEndOfChannels(true)
        assertTrue(state.endOfChannels.value)
    }

    @Test
    fun `setRecoveryNeeded updates recoveryNeeded flow`() = runTest {
        assertFalse(state.recoveryNeeded.value)
        state.setRecoveryNeeded(true)
        assertTrue(state.recoveryNeeded.value)
    }

    @Test
    fun `setChannelsOffset updates channelsOffset flow`() = runTest {
        assertEquals(0, state.channelsOffset.value)
        state.setChannelsOffset(42)
        assertEquals(42, state.channelsOffset.value)
    }

    @Test
    fun `setChannels updates rawChannels and channels flow`() = runTest {
        assertNull(state.rawChannels)
        val channel = randomChannel()
        state.setChannels(mapOf(channel.cid to channel))
        assertEquals(mapOf(channel.cid to channel), state.rawChannels)
        advanceUntilIdle()
        assertEquals(1, state.channels.value?.size)
    }

    // endregion

    // region channelsStateData

    @Test
    fun `channelsStateData emits Loading when loading is true`() = runTest {
        state.setLoadingFirstPage(true)
        advanceUntilIdle()
        assertEquals(ChannelsStateData.Loading, state.channelsStateData.value)
    }

    @Test
    fun `channelsStateData emits Loading when channels are null`() = runTest {
        // channels are null by default (never set), loading is false
        state.setLoadingFirstPage(false)
        advanceUntilIdle()
        assertEquals(ChannelsStateData.Loading, state.channelsStateData.value)
    }

    @Test
    fun `channelsStateData emits OfflineNoResults when not loading and channels empty`() = runTest {
        state.setChannels(emptyMap())
        state.setLoadingFirstPage(false)
        advanceUntilIdle()
        assertEquals(ChannelsStateData.OfflineNoResults, state.channelsStateData.value)
    }

    @Test
    fun `channelsStateData emits Result when channels available`() = runTest {
        val channel = randomChannel()
        state.setChannels(mapOf(channel.cid to channel))
        state.setLoadingFirstPage(false)
        advanceUntilIdle()
        val result = state.channelsStateData.value
        assertTrue(result is ChannelsStateData.Result)
        assertEquals(1, (result as ChannelsStateData.Result).channels.size)
    }

    // endregion

    // region nextPageRequest

    @Test
    fun `nextPageRequest is null when currentRequest is null`() = runTest {
        advanceUntilIdle()
        assertNull(state.nextPageRequest.value)
    }

    @Test
    fun `nextPageRequest combines currentRequest with channelsOffset and updates when offset changes`() = runTest {
        val request = QueryChannelsRequest(filter = filter, offset = 0, limit = 30, querySort = sort)
        state.setCurrentRequest(request)
        state.setChannelsOffset(30)
        advanceUntilIdle()

        val nextPage = state.nextPageRequest.value
        assertEquals(30, nextPage?.offset)

        state.setChannelsOffset(60)
        advanceUntilIdle()
        assertEquals(60, state.nextPageRequest.value?.offset)
    }

    // endregion

    // region currentLoading

    @Test
    fun `currentLoading returns loading when channels are null or empty`() = runTest {
        // channels null by default
        state.setLoadingFirstPage(true)
        assertTrue(state.currentLoading.value)
    }

    @Test
    fun `currentLoading returns loadingMore when channels are non-empty`() = runTest {
        val channel = randomChannel()
        state.setChannels(mapOf(channel.cid to channel))
        advanceUntilIdle()
        state.setLoadingMore(true)
        assertTrue(state.currentLoading.value)
    }

    // endregion

    // region sortedChannels

    @Test
    fun `channels returns sorted list per sort comparator`() = runTest {
        val older = randomChannel(id = "older").copy(lastMessageAt = Date(1000))
        val newer = randomChannel(id = "newer").copy(lastMessageAt = Date(2000))
        // Sort is descByName("last_message_at"), so newer should come first
        state.setChannels(mapOf(older.cid to older, newer.cid to newer))
        advanceUntilIdle()
        val channels = state.channels.value!!
        assertEquals(2, channels.size)
        assertEquals(newer.cid, channels[0].cid)
        assertEquals(older.cid, channels[1].cid)
    }

    @Test
    fun `channels update when latestUsers flow changes`() = runTest {
        val user = randomUser(id = "user1", name = "Original")
        val channel = randomChannel(id = "ch1").copy(
            createdBy = user,
        )
        state.setChannels(mapOf(channel.cid to channel))
        advanceUntilIdle()

        val updatedUser = user.copy(name = "Updated")
        latestUsers.value = mapOf(updatedUser.id to updatedUser)
        advanceUntilIdle()

        val result = state.channels.value!!.first()
        assertEquals("Updated", result.createdBy.name)
    }

    // endregion

    // region destroy

    @Test
    fun `destroy nullifies flows and setters become no-ops`() = runTest {
        state.destroy()
        // After destroy, setters should not throw (they use ?. safe calls)
        state.setLoadingMore(true)
        state.setLoadingFirstPage(true)
        state.setEndOfChannels(true)
        state.setRecoveryNeeded(true)
        state.setChannelsOffset(99)
        // rawChannels should be null since _channels was nullified
        assertNull(state.rawChannels)
    }

    // endregion
}
