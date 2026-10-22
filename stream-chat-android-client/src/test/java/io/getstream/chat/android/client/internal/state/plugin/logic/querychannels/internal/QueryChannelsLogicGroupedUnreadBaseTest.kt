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

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomUser
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsLogicGroupedUnreadBaseTest {

    private val client: ChatClient = mock()
    private val queryChannelsStateLogic: QueryChannelsStateLogic = mock()
    private val queryChannelsDatabaseLogic: QueryChannelsDatabaseLogic = mock {
        onBlocking { selectLocalOnlyMessages(any()) } doReturn emptyList()
    }
    private lateinit var logic: QueryChannelsLogic

    @BeforeEach
    fun setUp() {
        val queryChannelsState: QueryChannelsState = mock()
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
    fun `applyGroupedResult on a filtered first page keeps the unread channels not loaded as the badge base`() = runTest {
        // Given - the server counts 5 unread channels, 2 of them are on the page
        val user = givenFilteredGroupFor()
        val page = listOf(channelWithUnread(user, 2), channelWithUnread(user, 2), channelWithUnread(user, 0))
        whenever(queryChannelsStateLogic.getLoadedChannelsShownNow()) doReturn page

        // When
        logic.applyGroupedResult(groupOf(page, unreadChannels = 5), isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic).setGroupedUnreadBase(3)
    }

    @Test
    fun `applyGroupedResult on a filtered next page takes the loaded unread channels out of the fresh count`() = runTest {
        // Given - an earlier base of 10, but the server now counts 4, and 2 unread channels are loaded
        val user = givenFilteredGroupFor()
        whenever(queryChannelsStateLogic.getGroupedUnreadBase()) doReturn 10
        val page = listOf(channelWithUnread(user, 2), channelWithUnread(user, 0))
        whenever(queryChannelsStateLogic.getLoadedChannelsShownNow()) doReturn page + channelWithUnread(user, 1)

        // When
        logic.applyGroupedResult(groupOf(page, unreadChannels = 4), isFirstPage = false)

        // Then
        verify(queryChannelsStateLogic).setGroupedUnreadBase(2)
    }

    @Test
    fun `applyGroupedResult does not let the badge base go below zero`() = runTest {
        val user = givenFilteredGroupFor()
        val page = listOf(channelWithUnread(user, 2), channelWithUnread(user, 2))
        whenever(queryChannelsStateLogic.getLoadedChannelsShownNow()) doReturn page

        logic.applyGroupedResult(groupOf(page, unreadChannels = 1), isFirstPage = true)

        verify(queryChannelsStateLogic).setGroupedUnreadBase(0)
    }

    @Test
    fun `applyGroupedResult for a group without a filter clears the badge base`() = runTest {
        whenever(client.getCurrentUser()) doReturn randomUser()
        whenever(queryChannelsStateLogic.getFilter()) doReturn Filters.neutral()
        whenever(queryChannelsStateLogic.getChannels()) doReturn null

        logic.applyGroupedResult(groupOf(emptyList(), unreadChannels = 5), isFirstPage = true)

        verify(queryChannelsStateLogic).setGroupedUnreadBase(null)
    }

    @Test
    fun `filteredGroupUnreadCount adds the loaded unread channels to the badge base`() = runTest {
        val user = givenFilteredGroupFor()
        whenever(queryChannelsStateLogic.getGroupedUnreadBase()) doReturn 3
        whenever(queryChannelsStateLogic.getLoadedChannelsShownNow()) doReturn listOf(
            channelWithUnread(user, 1),
            channelWithUnread(user, 0),
            channelWithUnread(user, 1).copy(hidden = true),
        )

        assertEquals(4, logic.filteredGroupUnreadCount())
    }

    @Test
    fun `filteredGroupUnreadCount is null without a badge base`() = runTest {
        givenFilteredGroupFor()
        whenever(queryChannelsStateLogic.getGroupedUnreadBase()) doReturn null

        assertNull(logic.filteredGroupUnreadCount())
    }

    @Test
    fun `filteredGroupUnreadCount waits until a page is fully applied`() = runTest {
        // Given - a badge of 3 unread channels, none loaded yet
        val user = givenFilteredGroupFor()
        var base: Int? = 3
        var loaded = emptyList<Channel>()
        whenever(queryChannelsStateLogic.getGroupedUnreadBase()) doAnswer { base }
        whenever(queryChannelsStateLogic.setGroupedUnreadBase(anyOrNull())) doAnswer { base = it.getArgument(0) }
        whenever(queryChannelsStateLogic.getLoadedChannelsShownNow()) doAnswer { loaded }
        val pageAdded = CompletableDeferred<Unit>()
        queryChannelsStateLogic.stub {
            onBlocking { addChannelsState(any()) } doSuspendableAnswer {
                loaded = loaded + it.getArgument<List<Channel>>(0)
                pageAdded.await()
            }
        }
        val page = listOf(channelWithUnread(user, 1), channelWithUnread(user, 1))

        // When - the count is read after the page's channels are added but before its base is set
        val applying = launch { logic.applyGroupedResult(groupOf(page, unreadChannels = 3), isFirstPage = true) }
        runCurrent()
        val count = async { logic.filteredGroupUnreadCount() }
        runCurrent()
        assertFalse(count.isCompleted)
        pageAdded.complete(Unit)
        applying.join()

        // Then - the read waits for the page, so it never adds the page's channels to the old base
        assertEquals(3, count.await())
    }

    @Test
    fun `handleMarkAllRead sets the badge base of a filtered group to zero`() = runTest {
        whenever(queryChannelsStateLogic.getGroupedUnreadBase()) doReturn 4

        logic.handleMarkAllRead()

        verify(queryChannelsStateLogic).setGroupedUnreadBase(0)
    }

    @Test
    fun `handleMarkAllRead leaves a group without a badge base unchanged`() = runTest {
        whenever(queryChannelsStateLogic.getGroupedUnreadBase()) doReturn null

        logic.handleMarkAllRead()

        verify(queryChannelsStateLogic, never()).setGroupedUnreadBase(anyOrNull())
    }

    private fun givenFilteredGroupFor(): User {
        val user = randomUser()
        whenever(client.getCurrentUser()) doReturn user
        whenever(queryChannelsStateLogic.getFilter()) doReturn Filters.greaterThanEquals("query_timestamp", "now-14d")
        whenever(queryChannelsStateLogic.getChannels()) doReturn null
        return user
    }

    private fun groupOf(channels: List<Channel>, unreadChannels: Int = 0) = GroupedChannelsGroup(
        groupKey = GROUP_KEY,
        channels = channels,
        unreadChannels = unreadChannels,
        next = null,
        prev = null,
    )

    private fun channelWithUnread(user: User, unreadMessages: Int) = randomChannel(
        hidden = false,
        disabled = false,
        read = listOf(randomChannelUserRead(user = user, unreadMessages = unreadMessages)),
    )

    private companion object {
        private const val GROUP_KEY = "test-group"
    }
}
