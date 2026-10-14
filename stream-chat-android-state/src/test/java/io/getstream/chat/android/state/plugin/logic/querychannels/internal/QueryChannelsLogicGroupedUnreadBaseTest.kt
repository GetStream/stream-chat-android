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
import io.getstream.chat.android.client.internal.state.plugin.QueryChannelsIdentifier
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.GroupedChannelsGroup
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomChannel
import io.getstream.chat.android.randomChannelUserRead
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.TestCoroutineRule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class QueryChannelsLogicGroupedUnreadBaseTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

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
        val user = randomUser()
        whenever(client.getCurrentUser()) doReturn user
        whenever(queryChannelsStateLogic.getFilter()) doReturn Filters.greaterThanEquals("query_timestamp", "now-14d")
        whenever(queryChannelsStateLogic.getChannels()) doReturn null
        val page = listOf(channelWithUnread(user, 2), channelWithUnread(user, 2), channelWithUnread(user, 0))

        // When
        logic.applyGroupedResult(groupOf(page, unreadChannels = 5), isFirstPage = true)

        // Then
        verify(queryChannelsStateLogic).setGroupedUnreadBase(3)
    }

    @Test
    fun `applyGroupedResult on a filtered next page takes its unread channels out of the badge base`() = runTest {
        val user = randomUser()
        whenever(client.getCurrentUser()) doReturn user
        whenever(queryChannelsStateLogic.getFilter()) doReturn Filters.greaterThanEquals("query_timestamp", "now-14d")
        whenever(queryChannelsStateLogic.getGroupedUnreadBase()) doReturn 3

        logic.applyGroupedResult(groupOf(listOf(channelWithUnread(user, 2), channelWithUnread(user, 0))), isFirstPage = false)

        verify(queryChannelsStateLogic).setGroupedUnreadBase(2)
    }

    @Test
    fun `applyGroupedResult for a group without a filter clears the badge base`() = runTest {
        whenever(client.getCurrentUser()) doReturn randomUser()
        whenever(queryChannelsStateLogic.getFilter()) doReturn Filters.neutral()
        whenever(queryChannelsStateLogic.getChannels()) doReturn null

        logic.applyGroupedResult(groupOf(emptyList(), unreadChannels = 5), isFirstPage = true)

        verify(queryChannelsStateLogic).setGroupedUnreadBase(null)
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
        read = listOf(randomChannelUserRead(user = user, unreadMessages = unreadMessages)),
    )

    private companion object {
        private const val GROUP_KEY = "test-group"
    }
}
