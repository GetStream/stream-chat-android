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

package io.getstream.chat.android.ui.viewmodels.channels

import androidx.lifecycle.Observer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.event.ChatEventHandlerFactory
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.state.ChannelsStateData
import io.getstream.chat.android.client.api.state.GlobalState
import io.getstream.chat.android.client.api.state.QueryChannelsState
import io.getstream.chat.android.client.api.state.StateRegistry
import io.getstream.chat.android.client.channel.ChannelClient
import io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.observeAll
import io.getstream.chat.android.ui.common.utils.extensions.defaultChannelListFilter
import io.getstream.chat.android.ui.viewmodel.channels.ChannelListViewModel
import io.getstream.result.call.Call
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

internal class ChannelListViewModelTest {

    @Test
    fun `Given channel list in loading state When showing the channel list Should show loading state`() =
        runTest {
            val channelsStateData = ChannelsStateData.Loading
            val isLoading = true

            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(channelsStateData = channelsStateData, loading = isLoading)
                .givenChannelMutes()
                .get()

            val state = viewModel.state.observeAll()

            state.last() shouldBeEqualTo ChannelListViewModel.State(
                channels = listOf(),
                isLoading = isLoading,
            )
        }

    @Test
    fun `Given channel list in content state When showing the channel list Should show the list of channels`() =
        runTest {
            val channels = listOf(channel1, channel2)
            val isLoading = false

            val viewModel = Fixture()
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(channels),
                    loading = isLoading,
                )
                .givenChannelMutes()
                .get()

            val state = viewModel.state.observeAll()

            state.last() shouldBeEqualTo ChannelListViewModel.State(
                channels = listOf(channel1, channel2),
                isLoading = isLoading,
            )
        }

    @Test
    fun `Given channel list in content state When another user connects Should clear the channels`() = runTest {
        val fixture = Fixture()
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(
                channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                loading = false,
                endOfChannels = true,
            )
            .givenChannelMutes()
        val viewModel = fixture.get()
        val state = viewModel.state.observeAll()
        val paginationState = viewModel.paginationState.observeAll()

        fixture.disconnectUser()
        fixture.startConnectingUser(User(id = "other"))

        state.last() shouldBeEqualTo ChannelListViewModel.State(isLoading = true, channels = emptyList())
        paginationState.last() shouldBeEqualTo ChannelListViewModel.PaginationState()
    }

    @Test
    fun `Given channel list in content state When another user connects mid-disconnect Should clear the channels`() =
        runTest {
            val fixture = Fixture()
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(
                    channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                    loading = false,
                )
                .givenChannelMutes()
            val viewModel = fixture.get()
            val state = viewModel.state.observeAll()

            fixture.startConnectingUser(User(id = "other"))

            state.last() shouldBeEqualTo ChannelListViewModel.State(isLoading = true, channels = emptyList())
        }

    @Test
    fun `Given channel list in content state When the user disconnects Should clear the channels`() = runTest {
        val fixture = Fixture()
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(
                channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                loading = false,
                endOfChannels = true,
            )
            .givenChannelMutes()
        val viewModel = fixture.get()
        val state = viewModel.state.observeAll()
        val paginationState = viewModel.paginationState.observeAll()

        fixture.disconnectUser()

        state.last() shouldBeEqualTo ChannelListViewModel.State(isLoading = true, channels = emptyList())
        paginationState.last() shouldBeEqualTo ChannelListViewModel.PaginationState()
    }

    @Test
    fun `Given no initial filter When another user connects Should not re-send the previous user's query`() = runTest {
        val chatClient: ChatClient = mock()
        val fixture = Fixture(chatClient, initialFilters = null)
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(channelsStateData = ChannelsStateData.Result(listOf(channel1)))
            .givenChannelMutes()
        val viewModel = fixture.get()
        viewModel.state.observeAll()

        fixture.disconnectUser()
        fixture.connectUser(User(id = "other"))

        val captor = argumentCaptor<QueryChannelsRequest>()
        verify(chatClient, atLeastOnce()).queryChannels(captor.capture())
        val previousUserFilter = Filters.defaultChannelListFilter(User(id = "Jc"))
        captor.allValues.count { it.filter == previousUserFilter } shouldBeEqualTo 1
    }

    @Test
    fun `Given channel list in content state When the same user connects again Should keep the channels`() = runTest {
        val fixture = Fixture()
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(
                channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                loading = false,
                endOfChannels = true,
            )
            .givenChannelMutes()
        val viewModel = fixture.get()
        val state = viewModel.state.observeAll()
        val emittedBefore = state.size

        fixture.reconnectSameUser()

        state.drop(emittedBefore).none { it.channels.isEmpty() } shouldBeEqualTo true
        state.last().channels shouldBeEqualTo listOf(channel1, channel2)
    }

    @Test
    fun `Given no initial filter When another user connects Should query with their default filter`() = runTest {
        val chatClient: ChatClient = mock()
        val fixture = Fixture(chatClient, initialFilters = null)
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(channelsStateData = ChannelsStateData.Result(listOf(channel1)))
            .givenChannelMutes()
        val viewModel = fixture.get()
        viewModel.state.observeAll()
        val otherUser = User(id = "other")

        fixture.disconnectUser()
        fixture.connectUser(otherUser)

        val captor = argumentCaptor<QueryChannelsRequest>()
        verify(chatClient, atLeastOnce()).queryChannels(captor.capture())
        captor.lastValue.filter shouldBeEqualTo Filters.defaultChannelListFilter(otherUser)
    }

    @Test
    fun `Given filters set by the app When another user connects Should keep querying with them`() = runTest {
        val chatClient: ChatClient = mock()
        val fixture = Fixture(chatClient, initialFilters = null)
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(channelsStateData = ChannelsStateData.Result(listOf(channel1)))
            .givenChannelMutes()
        val viewModel = fixture.get()
        viewModel.state.observeAll()
        val appFilter = Filters.eq("type", "livestream")
        viewModel.setFilters(appFilter)

        fixture.disconnectUser()
        fixture.connectUser(User(id = "other"))

        val captor = argumentCaptor<QueryChannelsRequest>()
        verify(chatClient, atLeastOnce()).queryChannels(captor.capture())
        captor.lastValue.filter shouldBeEqualTo appFilter
    }

    @Test
    fun `Given filters set before the first user connects When they connect Should query with those filters`() =
        runTest {
            val chatClient: ChatClient = mock()
            val fixture = Fixture(chatClient, initialFilters = null)
                .givenNoUserYet()
                .givenChannelsQuery()
                .givenChannelsState(channelsStateData = ChannelsStateData.Result(listOf(channel1)))
                .givenChannelMutes()
            val viewModel = fixture.get()
            viewModel.state.observeAll()
            val appFilter = Filters.eq("type", "livestream")
            viewModel.setFilters(appFilter)

            fixture.connectUser(User(id = "Jc"))

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, atLeastOnce()).queryChannels(captor.capture())
            captor.lastValue.filter shouldBeEqualTo appFilter
        }

    @Test
    fun `Given no initial filter When the same user logs in again Should query with their default filter`() =
        runTest {
            val chatClient: ChatClient = mock()
            val fixture = Fixture(chatClient, initialFilters = null)
                .givenCurrentUser()
                .givenChannelsQuery()
                .givenChannelsState(channelsStateData = ChannelsStateData.Result(listOf(channel1)))
                .givenChannelMutes()
            val viewModel = fixture.get()
            viewModel.state.observeAll()

            fixture.disconnectUser()
            fixture.connectUser(User(id = "Jc"))

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(2)).queryChannels(captor.capture())
            captor.allValues.all { it.filter == Filters.defaultChannelListFilter(User(id = "Jc")) } shouldBeEqualTo true
        }

    @Test
    fun `Given channel list in content state When leaving a channel Should leave the channel`() = runTest {
        val chatClient: ChatClient = mock()
        val channelClient: ChannelClient = mock()

        val user = User(id = "jc")

        val viewModel = Fixture(chatClient, channelClient)
            .givenCurrentUser(user)
            .givenChannelsQuery()
            .givenChannelsState(
                channelsStateData = ChannelsStateData.Result(listOf(channel1, channel2)),
                loading = false,
            )
            .givenChannelMutes()
            .givenLeaveChannel(listOf(user.id))
            .get()

        viewModel.leaveChannel(channel1)

        verify(channelClient).removeMembers(listOf(user.id))
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
            .get()

        viewModel.deleteChannel(channel1)

        verify(channelClient).delete()
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
                .get()

            val mockObserver: Observer<ChannelListViewModel.State> = mock()
            viewModel.state.observeForever(mockObserver)

            viewModel.onAction(ChannelListViewModel.Action.ReachedEndOfList)

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(2)).queryChannels(captor.capture())

            captor.firstValue.offset `should be equal to` 0
            captor.secondValue.offset `should be equal to` 30

            viewModel.state.removeObserver(mockObserver)
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
                .get()

            val mockObserver: Observer<ChannelListViewModel.State> = mock()
            viewModel.state.observeForever(mockObserver)

            viewModel.onAction(ChannelListViewModel.Action.ReachedEndOfList)

            val captor = argumentCaptor<QueryChannelsRequest>()
            verify(chatClient, times(1)).queryChannels(captor.capture())

            captor.firstValue.offset `should be equal to` 0

            viewModel.state.removeObserver(mockObserver)
        }

    @Test
    fun `Given predefined filter When initializing Should issue predefined-shaped query`() = runTest {
        val chatClient: ChatClient = mock()
        Fixture(chatClient)
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(channelsStateData = ChannelsStateData.Loading, loading = true)
            .givenChannelMutes()
            .givenPredefined(
                name = "vip_filter",
                filterValues = mapOf("foo" to "bar"),
                sortValues = mapOf("baz" to 1),
            )
            .get()

        val captor = argumentCaptor<QueryChannelsRequest>()
        verify(chatClient).queryChannels(captor.capture())
        val request = captor.firstValue
        assertEquals("vip_filter", request.predefinedFilter)
        assertEquals(mapOf("foo" to "bar"), request.filterValues)
        assertEquals(mapOf("baz" to 1), request.sortValues)
        assertEquals(Filters.neutral(), request.filter)
    }

    @Test
    fun `Given predefined filter When calling setFilters Should not re-issue the query`() = runTest {
        val chatClient: ChatClient = mock()
        val viewModel = Fixture(chatClient)
            .givenCurrentUser()
            .givenChannelsQuery()
            .givenChannelsState(
                channelsStateData = ChannelsStateData.Result(listOf(channel1)),
                loading = false,
            )
            .givenChannelMutes()
            .givenPredefined(name = "vip_filter")
            .get()

        viewModel.setFilters(Filters.eq("type", "messaging"))

        verify(chatClient, times(1)).queryChannels(any())
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelClient: ChannelClient = mock(),
        private val initialSort: QuerySortByField<Channel> = querySort,
        private val initialFilters: FilterObject? = queryFilter,
    ) {

        private val stateRegistry: StateRegistry = mock()
        private val clientState: ClientState = mock()
        private val user = MutableStateFlow<User?>(null)
        private val initializationState = MutableStateFlow(InitializationState.NOT_INITIALIZED)
        private val globalState: GlobalState = mock()
        private var predefinedFilterName: String? = null
        private var predefinedFilterValues: Map<String, Any>? = null
        private var predefinedSortValues: Map<String, Any>? = null

        init {
            whenever(chatClient.channel(any())) doReturn channelClient
            whenever(chatClient.channel(any(), any())) doReturn channelClient
            val statePlugin: StatePlugin = mock()
            whenever(statePlugin.resolveDependency(eq(StateRegistry::class))) doReturn stateRegistry
            whenever(chatClient.plugins) doReturn listOf(statePlugin)
            whenever(chatClient.clientState) doReturn clientState
            whenever(globalState.typingChannels) doReturn MutableStateFlow(emptyMap())
        }

        fun givenNoUserYet() = apply {
            whenever(clientState.user) doReturn user
            whenever(clientState.initializationState) doReturn initializationState
            whenever(chatClient.awaitInitializationState(any())) doReturn InitializationState.COMPLETE
        }

        fun givenCurrentUser(currentUser: User = User(id = "Jc")) = apply {
            user.value = currentUser
            initializationState.value = InitializationState.COMPLETE
            whenever(clientState.user) doReturn user
            whenever(clientState.initializationState) doReturn initializationState
            whenever(chatClient.getCurrentUser()) doReturn currentUser
            whenever(chatClient.awaitInitializationState(any())) doReturn InitializationState.COMPLETE
        }

        fun reconnectSameUser() {
            initializationState.value = InitializationState.INITIALIZING
            initializationState.value = InitializationState.COMPLETE
        }

        fun startConnectingUser(newUser: User) {
            // connectUser sets the user before the client is ready.
            initializationState.value = InitializationState.INITIALIZING
            user.value = newUser
        }

        fun connectUser(newUser: User) {
            user.value = newUser
            initializationState.value = InitializationState.COMPLETE
        }

        fun disconnectUser() {
            // Same order as ChatClient.disconnect.
            initializationState.value = InitializationState.NOT_INITIALIZED
            user.value = null
        }

        fun givenChannelMutes(channelMutes: List<ChannelMute> = emptyList()) = apply {
            whenever(globalState.channelMutes) doReturn MutableStateFlow(channelMutes)
        }

        fun givenChannelsQuery(channels: List<Channel> = emptyList()) = apply {
            whenever(chatClient.queryChannels(any())) doReturn channels.asCall()
        }

        fun givenLeaveChannel(memberIds: List<String>) = apply {
            whenever(channelClient.removeMembers(memberIds)) doReturn Channel().asCall()
        }

        fun givenDeleteChannel() = apply {
            whenever(channelClient.delete()) doReturn Channel().asCall()
        }

        fun givenPredefined(
            name: String,
            filterValues: Map<String, Any>? = null,
            sortValues: Map<String, Any>? = null,
        ) = apply {
            predefinedFilterName = name
            predefinedFilterValues = filterValues
            predefinedSortValues = sortValues
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
            whenever(stateRegistry.queryChannels(any())) doReturn queryChannelsState
        }

        fun get(): ChannelListViewModel {
            val name = predefinedFilterName
            return if (name != null) {
                ChannelListViewModel(
                    chatClient = chatClient,
                    predefinedFilterName = name,
                    filterValues = predefinedFilterValues,
                    sortValues = predefinedSortValues,
                    isDraftMessagesEnabled = false,
                    chatEventHandlerFactory = ChatEventHandlerFactory(
                        clientState = clientState,
                    ),
                    globalState = MutableStateFlow(globalState),
                )
            } else {
                ChannelListViewModel(
                    chatClient = chatClient,
                    sort = initialSort,
                    filter = initialFilters,
                    isDraftMessagesEnabled = false,
                    chatEventHandlerFactory = ChatEventHandlerFactory(
                        clientState = clientState,
                    ),
                    globalState = MutableStateFlow(globalState),
                )
            }
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines: TestCoroutineExtension = TestCoroutineExtension()

        @JvmField
        @RegisterExtension
        val instantExecutorExtension: InstantTaskExecutorExtension = InstantTaskExecutorExtension()

        private val queryFilter = Filters.and(
            Filters.eq("type", "messaging"),
            Filters.`in`("members", "jc"),
        )
        private val querySort = QuerySortByField.descByName<Channel>("last_updated")

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
