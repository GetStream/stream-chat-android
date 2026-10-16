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

package io.getstream.chat.android.state.plugin.logic.querychannels.internal

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.events.NotificationChannelPartialUpdatedEvent
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.client.test.randomNotificationChannelPartialUpdatedEvent
import io.getstream.chat.android.client.test.randomNotificationMessageNewEvent
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomString
import io.getstream.chat.android.state.event.handler.chat.EventHandlingResult
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date
import java.util.concurrent.TimeUnit

internal class QueryChannelsLogicGroupedWatchTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    private val client: ChatClient = mock()
    private val queryChannelsStateLogic: QueryChannelsStateLogic = mock()
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic = mock()
    private val queryChannelsState: QueryChannelsState = mock()
    private lateinit var logic: QueryChannelsLogic

    @BeforeEach
    fun setUp() {
        whenever(queryChannelsStateLogic.getState()) doReturn queryChannelsState
        whenever(queryChannelsState.recoveryNeeded) doReturn MutableStateFlow(false)
        whenever(queryChannelsState.currentRequest) doReturn MutableStateFlow(null)
        logic = QueryChannelsLogic(
            identifier = QueryChannelsIdentifier.Grouped(GROUP_KEY),
            client = client,
            queryChannelsStateLogic = queryChannelsStateLogic,
            queryChannelsDatabaseLogic = queryChannelsDatabaseLogic,
        )
    }

    @Test
    fun `a partial update watches a channel that sorts within the loaded channels`() = runTest {
        givenLoadedChannels(lastLoadedAt = daysAgo(5), endOfChannels = false)
        val event = partialUpdate(createdAt = daysAgo(1))

        val results = logic.parseChatEventResults(listOf(event))

        assertEquals(listOf(EventHandlingResult.WatchAndAdd(event.cid)), results)
    }

    @Test
    fun `a partial update skips a channel that sorts below the loaded channels`() = runTest {
        givenLoadedChannels(lastLoadedAt = daysAgo(5), endOfChannels = false)
        val event = partialUpdate(createdAt = daysAgo(30))

        val results = logic.parseChatEventResults(listOf(event))

        assertEquals(listOf(EventHandlingResult.Skip), results)
    }

    @Test
    fun `a partial update watches a channel below the loaded channels when the group has no more pages`() = runTest {
        givenLoadedChannels(lastLoadedAt = daysAgo(5), endOfChannels = true)
        val event = partialUpdate(createdAt = daysAgo(30))

        val results = logic.parseChatEventResults(listOf(event))

        assertEquals(listOf(EventHandlingResult.WatchAndAdd(event.cid)), results)
    }

    @Test
    fun `a partial update places a stored channel by its last message`() = runTest {
        givenLoadedChannels(lastLoadedAt = daysAgo(5), endOfChannels = false)
        val event = partialUpdate(createdAt = daysAgo(30))
        val stored = randomChannel(
            id = event.channelId,
            type = event.channelType,
            createdAt = daysAgo(30),
            lastMessageAt = daysAgo(1),
        )
        whenever(queryChannelsDatabaseLogic.selectChannels(listOf(event.cid))) doReturn listOf(stored)

        val results = logic.parseChatEventResults(listOf(event))

        assertEquals(listOf(EventHandlingResult.WatchAndAdd(event.cid)), results)
    }

    @Test
    fun `other events still watch a channel that sorts below the loaded channels`() = runTest {
        givenLoadedChannels(lastLoadedAt = daysAgo(5), endOfChannels = false)
        val event = randomNotificationMessageNewEvent(channel = randomChannel(createdAt = daysAgo(30)))
        whenever(queryChannelsStateLogic.handleChatEvent(any(), anyOrNull())) doReturn
            EventHandlingResult.WatchAndAdd(event.cid)

        val results = logic.parseChatEventResults(listOf(event))

        assertEquals(listOf(EventHandlingResult.WatchAndAdd(event.cid)), results)
    }

    private suspend fun givenLoadedChannels(lastLoadedAt: Date, endOfChannels: Boolean) {
        val loaded = listOf(
            randomChannel(createdAt = lastLoadedAt, lastMessageAt = null),
            randomChannel(createdAt = Date(lastLoadedAt.time + 1_000), lastMessageAt = null),
        ).sortedWith(DEFAULT_SORT.comparator)
        whenever(queryChannelsState.channels) doReturn MutableStateFlow(loaded)
        whenever(queryChannelsState.endOfChannels) doReturn MutableStateFlow(endOfChannels)
        whenever(queryChannelsState.sort) doReturn DEFAULT_SORT
        whenever(queryChannelsDatabaseLogic.selectChannels(any())) doReturn emptyList()
    }

    /** A partial update the group's handler resolved to a watch, carrying only the slim channel. */
    private fun partialUpdate(createdAt: Date): NotificationChannelPartialUpdatedEvent {
        val slim = Channel(id = randomString(), type = "messaging", createdAt = createdAt)
        val event = randomNotificationChannelPartialUpdatedEvent(
            cid = slim.cid,
            channelType = slim.type,
            channelId = slim.id,
            channel = slim,
        )
        whenever(queryChannelsStateLogic.handleChatEvent(any(), anyOrNull())) doReturn
            EventHandlingResult.WatchAndAdd(event.cid)
        return event
    }

    private fun daysAgo(days: Long): Date = Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days))

    @Test
    fun `watchAndAddChannel adds the watched channel`() = runTest {
        val channel = randomChannel(hidden = false)
        givenWatchReturns(channel)

        logic.watchAndAddChannel(channel.cid)

        verify(queryChannelsStateLogic).addChannelsState(listOf(channel))
    }

    @Test
    fun `watchAndAddChannel does not add a hidden channel to a grouped query`() = runTest {
        val channel = randomChannel(hidden = true)
        givenWatchReturns(channel)

        logic.watchAndAddChannel(channel.cid)

        verify(queryChannelsStateLogic, never()).addChannelsState(any())
    }

    private fun givenWatchReturns(channel: Channel) {
        val channelClient = mock<ChannelClient> {
            on { watch() } doReturn channel.asCall()
        }
        whenever(client.channel(channel.cid)) doReturn channelClient
    }

    private companion object {
        private const val GROUP_KEY = "test-group"
        private val DEFAULT_SORT = QuerySortByField.descByName<Channel>("last_updated")
    }
}
