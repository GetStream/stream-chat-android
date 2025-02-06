/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter
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
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
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
        channelsState.channelItems.size `should be equal to` 0
        channelsState.isLoading `should be equal to` true
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
            channelsState.channelItems.size `should be equal to` 2
            channelsState.isLoading `should be equal to` false
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

        viewModel.activeChannelAction `should be equal to` null
        viewModel.selectedChannel.value `should be equal to` null
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

        viewModel.activeChannelAction `should be equal to` null
        viewModel.selectedChannel.value `should be equal to` null
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

            (viewModel.channelsState.channelItems.first() as ItemState.ChannelItemState).isMuted `should be equal to` true
            viewModel.activeChannelAction `should be equal to` null
            viewModel.selectedChannel.value `should be equal to` null
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

            viewModel.activeChannelAction `should be equal to` null
            viewModel.selectedChannel.value `should be equal to` null
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
            captor.firstValue.offset `should be equal to` 0
            captor.secondValue.offset `should be equal to` 30
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
            captor.firstValue.offset `should be equal to` 0
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
            autoCompleteFilterObject.fieldName `should be equal to` "name"
            autoCompleteFilterObject.value `should be equal to` "Search query"
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
            autoCompleteFilterObject.fieldName `should be equal to` "name"
            autoCompleteFilterObject.value `should be equal to` "Search query"
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
            captor.allValues.size `should be equal to` 2
            captor.firstValue.offset `should be equal to` 0
            captor.secondValue.offset `should be equal to` 30
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

        init {
            val statePlugin: StatePlugin = mock()
            whenever(globalState.typingChannels) doReturn MutableStateFlow(emptyMap())
            whenever(statePlugin.resolveDependency(eq(StateRegistry::class))) doReturn stateRegistry
            whenever(statePlugin.resolveDependency(eq(GlobalState::class))) doReturn globalState
            whenever(chatClient.plugins) doReturn listOf(statePlugin)
            whenever(chatClient.channel(any())) doReturn channelClient
            whenever(chatClient.channel(any(), any())) doReturn channelClient
            whenever(chatClient.clientState) doReturn clientState
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
                chatEventHandlerFactory = ChatEventHandlerFactory(clientState),
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
