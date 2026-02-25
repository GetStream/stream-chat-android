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

package io.getstream.chat.android.compose.viewmodel.channels

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.persistance.repository.RepositoryFacade
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.compose.state.channels.list.ItemState
import io.getstream.chat.android.compose.state.channels.list.SearchQuery
import io.getstream.chat.android.models.AndFilterObject
import io.getstream.chat.android.models.AutocompleteFilterObject
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.OrFilterObject
import io.getstream.chat.android.models.SearchMessagesResult
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.state.event.handler.chat.factory.ChatEventHandlerFactory
import io.getstream.chat.android.state.plugin.internal.StatePlugin
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.state.plugin.state.global.GlobalState
import io.getstream.chat.android.state.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.state.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class ChannelListViewModelTest {

    @Test
    fun `Given channel list in loading state When showing the channel list Should show loading state`() = runTest {
        val channelsStateData = ChannelsStateData.Loading
        val viewModel = Fixture()
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(channelsStateData = channelsStateData, loading = true)
            .givenChannelMutes()
            .get(this)

        val channelsState = viewModel.channelsState
        assertEquals(0, channelsState.channelItems.size)
        assertTrue(channelsState.isLoading)
    }

    @Test
    fun `Given channel list in content state When showing the channel list Should show the list of channels`() =
        runTest {
            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    loading = false,
                )
                .givenChannelMutes()
                .givenTypingChannels()
                .get(this)

            val channelsState = viewModel.channelsState
            assertEquals(2, channelsState.channelItems.size)
            assertFalse(channelsState.isLoading)
        }

    @Test
    fun `Given channel list in content state When deleting a channel Should delete the channel`() = runTest {
        val chatClient: ChatClient = mock()
        val channelClient: ChannelClient = mock()
        val viewModel = Fixture(chatClient, channelClient)
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(
                channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                loading = false,
            )
            .givenChannelMutes()
            .givenDeleteChannel()
            .get(this)

        viewModel.selectChannel(channel1)
        viewModel.performChannelAction(DeleteConversation(channel1))
        viewModel.deleteConversation(channel1)

        assertNull(viewModel.activeChannelAction)
        assertNull(viewModel.selectedChannel.value)
        verify(channelClient).delete()
    }

    @Test
    fun `Given channel list in content state When muting a channel Should mute the channel`() = runTest {
        val chatClient: ChatClient = mock()
        val viewModel = Fixture(chatClient)
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(
                channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                loading = false,
            )
            .givenChannelMutes()
            .givenMuteChannel()
            .get(this)

        viewModel.selectChannel(channel1)
        viewModel.muteChannel(channel1)

        assertNull(viewModel.activeChannelAction)
        assertNull(viewModel.selectedChannel.value)
        verify(chatClient).muteChannel("messaging", "channel1", null)
    }

    @Test
    fun `Given channel list in content state with a muted channel When unmuting the channel Should unmute the channel`() =
        runTest {
            val channelMute = ChannelMute(
                user = User(id = "Jc"),
                channel = channel1,
                createdAt = Date(),
                updatedAt = Date(),
                expires = null,
            )
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    loading = false,
                )
                .givenChannelMutes(listOf(channelMute))
                .givenUnmuteChannel()
                .givenTypingChannels()
                .get(this)

            viewModel.selectChannel(channel1)
            viewModel.unmuteChannel(channel1)

            assertTrue((viewModel.channelsState.channelItems.first() as ItemState.ChannelItemState).isMuted)
            assertNull(viewModel.activeChannelAction)
            assertNull(viewModel.selectedChannel.value)
            verify(chatClient).unmuteChannel("messaging", "channel1")
        }

    @Test
    fun `Given channel list in content state When selecting a channel and dismissing the menu Should hide the menu`() =
        runTest {
            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    loading = false,
                )
                .givenChannelMutes()
                .get(this)

            viewModel.selectChannel(channel1)
            viewModel.dismissChannelAction()

            assertNull(viewModel.activeChannelAction)
            assertNull(viewModel.selectedChannel.value)
        }

    @Test
    fun `Given channel list in content state and the current user is online When loading more channels Should load more channels`() =
        runTest {
            val nextPageRequest = QueryChannelsRequest(
                filter = queryFilter,
                querySort = querySort,
                offset = 30,
                limit = 30,
            )
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    nextPageRequest = nextPageRequest,
                    loading = false,
                )
                .givenChannelMutes()
                .givenIsOffline(false)
                .get(this)

            viewModel.loadMore()

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(2)).queryChannels(captor.capture())
            assertEquals(0, captor.firstValue.offset)
            assertEquals(30, captor.secondValue.offset)
        }

    @Test
    fun `Given channel list in content state and the current user is offline When loading more channels Should do nothing`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    loading = false,
                )
                .givenChannelMutes()
                .givenIsOffline(true)
                .get(this)

            viewModel.loadMore()

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(1)).queryChannels(captor.capture())
            assertEquals(0, captor.firstValue.offset)
        }

    @Test
    fun `Given channel list When setting search query Should query channels with the name matching the filter`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    loading = false,
                )
                .givenChannelMutes()
                .get(this)

            viewModel.setSearchQuery(SearchQuery.Channels("Search query"))
            advanceUntilIdle()

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(2)).queryChannels(captor.capture())
            val andFilterObject = captor.secondValue.filter as AndFilterObject
            val orFilterObject = andFilterObject.filterObjects.last() as OrFilterObject
            val autoCompleteFilterObject = orFilterObject.filterObjects.last() as AutocompleteFilterObject
            assertEquals("name", autoCompleteFilterObject.fieldName)
            assertEquals("Search query", autoCompleteFilterObject.value)
        }

    @Test
    fun `Given channel list When setting multiple search query a short period of time Should only query channels once for the last value`() =
        runTest {
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    loading = false,
                )
                .givenChannelMutes()
                .get(this)

            "Search query".fold("") { previousSearch, newCharacter ->
                (previousSearch + newCharacter).also {
                    viewModel.setSearchQuery(SearchQuery.Channels(it))
                }
            }
            advanceUntilIdle()

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(2)).queryChannels(captor.capture())
            val andFilterObject = captor.secondValue.filter as AndFilterObject
            val orFilterObject = andFilterObject.filterObjects.last() as OrFilterObject
            val autoCompleteFilterObject = orFilterObject.filterObjects.last() as AutocompleteFilterObject
            assertEquals("name", autoCompleteFilterObject.fieldName)
            assertEquals("Search query", autoCompleteFilterObject.value)
        }

    @Test
    fun `Given channel list in content state and the current user is online When loading more channels Should filter out duplicate calls`() =
        runTest {
            val nextPageRequest = QueryChannelsRequest(
                filter = queryFilter,
                querySort = querySort,
                offset = 30,
                limit = 60,
            )
            val chatClient: ChatClient = mock()
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    nextPageRequest = nextPageRequest,
                    loading = false,
                )
                .givenChannelMutes()
                .givenIsOffline(false)
                .get(this)

            viewModel.loadMore()
            viewModel.loadMore()
            viewModel.loadMore()

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(2)).queryChannels(captor.capture())
            assertEquals(2, captor.allValues.size)
            assertEquals(0, captor.firstValue.offset)
            assertEquals(30, captor.secondValue.offset)
        }

    @Test
    fun `Given channel list When setting message search query Should search messages without offset or cursor`() =
        runTest {
            val chatClient: ChatClient = mock()
            val messages = listOf(randomMessage(cid = "messaging:channel1"))
            val searchResult = SearchMessagesResult(messages = messages, next = "cursor_page2")
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1)),
                    loading = false,
                )
                .givenChannelMutes()
                .givenSearchMessagesResult(searchResult)
                .givenRepositorySelectChannels(listOf(channel1))
                .get(this)

            viewModel.setSearchQuery(SearchQuery.Messages("hello"))
            advanceUntilIdle()

            verify(chatClient).searchMessages(
                channelFilter = any(),
                messageFilter = any(),
                offset = eq(null),
                limit = any(),
                next = eq(null),
                sort = eq(null),
            )
            val items = viewModel.channelsState.channelItems
            assertEquals(1, items.size)
            assertInstanceOf(ItemState.SearchResultItemState::class.java, items.first())
        }

    @Test
    fun `Given message search results with next cursor When loading more Should pass the cursor`() =
        runTest {
            val chatClient: ChatClient = mock()
            val firstPageMessages = listOf(randomMessage(cid = "messaging:channel1"))
            val firstPageResult = SearchMessagesResult(messages = firstPageMessages, next = "cursor_page2")
            val secondPageMessages = listOf(randomMessage(cid = "messaging:channel1"))
            val secondPageResult = SearchMessagesResult(messages = secondPageMessages, next = null)

            whenever(
                chatClient.searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()),
            ).doReturn(
                firstPageResult.asCall(),
                secondPageResult.asCall(),
            )

            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1)),
                    loading = false,
                )
                .givenChannelMutes()
                .givenRepositorySelectChannels(listOf(channel1))
                .get(this)

            viewModel.setSearchQuery(SearchQuery.Messages("hello"))
            advanceUntilIdle()

            viewModel.loadMore()
            advanceUntilIdle()

            val captor = argumentCaptor<String>()
            verify(chatClient, times(2)).searchMessages(
                channelFilter = any(),
                messageFilter = any(),
                offset = anyOrNull(),
                limit = anyOrNull(),
                next = captor.capture(),
                sort = anyOrNull(),
            )
            assertNull(captor.firstValue)
            assertEquals("cursor_page2", captor.secondValue)
        }

    @Test
    fun `Given message search results without next cursor When loading more Should not load more`() =
        runTest {
            val chatClient: ChatClient = mock()
            val messages = listOf(randomMessage(cid = "messaging:channel1"))
            val searchResult = SearchMessagesResult(messages = messages, next = null)
            val viewModel = Fixture(chatClient)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1)),
                    loading = false,
                )
                .givenChannelMutes()
                .givenSearchMessagesResult(searchResult)
                .givenRepositorySelectChannels(listOf(channel1))
                .get(this)

            viewModel.setSearchQuery(SearchQuery.Messages("hello"))
            advanceUntilIdle()

            assertTrue(viewModel.channelsState.endOfChannels)

            viewModel.loadMore()
            advanceUntilIdle()

            verify(chatClient, times(1)).searchMessages(
                channelFilter = any(),
                messageFilter = any(),
                offset = anyOrNull(),
                limit = anyOrNull(),
                next = anyOrNull(),
                sort = anyOrNull(),
            )
        }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelClient: ChannelClient = mock(),
        private val initialSort: QuerySorter<Channel> = querySort,
        private val initialFilters: FilterObject? = queryFilter,
    ) {
        private val clientState: ClientState = mock()
        private val stateRegistry: StateRegistry = mock()
        private val globalState: GlobalState = mock()
        private val repositoryFacade: RepositoryFacade = mock()

        init {
            val statePlugin: StatePlugin = mock()
            whenever(globalState.typingChannels) doReturn MutableStateFlow(emptyMap())
            whenever(statePlugin.resolveDependency(eq(StateRegistry::class))) doReturn stateRegistry
            whenever(statePlugin.resolveDependency(eq(GlobalState::class))) doReturn globalState
            whenever(chatClient.plugins) doReturn listOf(statePlugin)
            whenever(chatClient.channel(any())) doReturn channelClient
            whenever(chatClient.channel(any(), any())) doReturn channelClient
            whenever(chatClient.clientState) doReturn clientState
            whenever(chatClient.repositoryFacade) doReturn repositoryFacade
            whenever(globalState.channelDraftMessages) doReturn MutableStateFlow(emptyMap())
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
            whenever(clientState.initializationState) doReturn MutableStateFlow(InitializationState.COMPLETE)
            whenever(chatClient.awaitInitializationState(any())) doReturn InitializationState.COMPLETE
        }

        fun givenChannelMutes(channelMutes: List<ChannelMute> = emptyList()) = apply {
            whenever(globalState.channelMutes) doReturn MutableStateFlow(channelMutes)
        }

        fun givenTypingChannels(typingChannels: Map<String, TypingEvent> = emptyMap()) = apply {
            whenever(globalState.typingChannels) doReturn MutableStateFlow(typingChannels)
        }

        fun givenIsOffline(isOffline: Boolean = false) = apply {
            whenever(clientState.isOffline) doReturn isOffline
        }

        fun givenChannelsQuery(channels: List<Channel> = emptyList()) = apply {
            whenever(chatClient.queryChannels(any())) doReturn channels.asCall()
        }

        fun givenDeleteChannel() = apply {
            whenever(channelClient.delete()) doReturn Channel().asCall()
        }

        fun givenMuteChannel() = apply {
            whenever(chatClient.muteChannel(any(), any(), eq(null))) doReturn Unit.asCall()
        }

        fun givenUnmuteChannel() = apply {
            whenever(chatClient.unmuteChannel(any(), any())) doReturn Unit.asCall()
        }

        fun givenSearchMessagesResult(result: SearchMessagesResult) = apply {
            whenever(
                chatClient.searchMessages(any(), any(), anyOrNull(), anyOrNull(), anyOrNull(), anyOrNull()),
            ) doReturn result.asCall()
        }

        suspend fun givenRepositorySelectChannels(channels: List<Channel> = emptyList()) = apply {
            whenever(repositoryFacade.selectChannels(any<List<String>>())) doReturn channels
        }

        fun givenChannelsState(
            channelsStateData: ChannelsStateData = ChannelsStateData.Loading,
            channels: List<Channel>? = null,
            loading: Boolean = false,
            loadingMore: Boolean = false,
            endOfChannels: Boolean = false,
            nextPageRequest: QueryChannelsRequest? = null,
        ) = apply {
            val queryChannelsState: QueryChannelsState = mock {
                whenever(it.channelsStateData) doReturn MutableStateFlow(channelsStateData)
                whenever(it.channels) doReturn MutableStateFlow(channels)
                whenever(it.loading) doReturn MutableStateFlow(loading)
                whenever(it.loadingMore) doReturn MutableStateFlow(loadingMore)
                whenever(it.endOfChannels) doReturn MutableStateFlow(endOfChannels)
                whenever(it.nextPageRequest) doReturn MutableStateFlow(nextPageRequest)
            }
            whenever(stateRegistry.queryChannels(any(), any())) doReturn queryChannelsState
        }

        fun get(testScope: TestScope): ChannelListViewModel {
            val channelListViewModel = ChannelListViewModel(
                chatClient = chatClient,
                initialSort = initialSort,
                initialFilters = initialFilters,
                isDraftMessageEnabled = false,
                chatEventHandlerFactory = ChatEventHandlerFactory(clientState),
                globalState = MutableStateFlow(globalState),
            )
            testScope.advanceUntilIdle()
            return channelListViewModel
        }
    }

    companion object {

        private val queryFilter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", "jc"),
        )
        private val querySort = QuerySortByField.descByName<Channel>("lastUpdated")

        private val channel1: Channel = Channel(
            type = "messaging",
            id = "channel1",
        )
        private val channel2: Channel = Channel(
            type = "messaging",
            id = "channel2",
        )
    }
}
