package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.Observer
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Filters
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.global.GlobalState
import io.getstream.chat.android.offline.plugin.state.querychannels.ChannelsStateData
import io.getstream.chat.android.offline.plugin.state.querychannels.QueryChannelsState
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.validateMockitoUsage
import org.mockito.kotlin.whenever

@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChannelListViewModelTest {

    private val channelStateData = MutableStateFlow(ChannelsStateData.Loading as ChannelsStateData)
    private val channels: MutableStateFlow<List<Channel>> = MutableStateFlow(listOf())
    private val endOfChannels = MutableStateFlow(false)
    private val nextPageRequest = MutableStateFlow<QueryChannelsRequest?>(null)
    private val user = MutableStateFlow(User(id = "ID"))

    private val queryChannelsMock: Call<List<Channel>> = mock()

    private val globalState: GlobalState = mock {
        whenever(it.user) doReturn user
        whenever(it.channelMutes) doReturn MutableStateFlow(listOf())
    }

    private val queryChannelState: QueryChannelsState = mock {
        whenever(it.channelsStateData) doReturn channelStateData
        whenever(it.loadingMore) doReturn MutableStateFlow(false)
        whenever(it.endOfChannels) doReturn endOfChannels
        whenever(it.channels) doReturn channels
        whenever(it.nextPageRequest) doReturn nextPageRequest
    }

    private val stateRegistry: StateRegistry = mock {
        whenever(it.queryChannels(any(), any())) doReturn queryChannelState
    }

    private val chatClient: ChatClient = mock {
        whenever(it.queryChannels(any())) doReturn queryChannelsMock
    }

    init {
        StateRegistry.instance = stateRegistry
    }

    @AfterEach
    fun validate(){
        validateMockitoUsage()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should display channels when there are channels available`() = runBlockingTest {
        // given
        val viewModel =
            ChannelListViewModel(chatClient = chatClient, globalState = globalState, chatEventHandlerFactory = mock())
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // when
        channelStateData.emit(ChannelsStateData.Result(mockChannels))
        advanceUntilIdle()

        // then
        verify(mockObserver, times(1))
            .onChanged(ChannelListViewModel.State(isLoading = false, channels = mockChannels))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should display empty state info when there are no channels available`() = runBlockingTest {
        // given
        val viewModel =
            ChannelListViewModel(chatClient = chatClient, globalState = globalState, chatEventHandlerFactory = mock())
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // when
        channelStateData.emit(ChannelsStateData.Result(listOf()))
        advanceUntilIdle()

        // then
        verify(mockObserver, times(1))
            .onChanged(ChannelListViewModel.State(isLoading = false, channels = emptyList()))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `Should load more channels when list is scrolled to the end region`() = runBlockingTest {
        // given
        val viewModel =
            ChannelListViewModel(chatClient = chatClient, globalState = globalState, chatEventHandlerFactory = mock())

        // when
        nextPageRequest.emit(QueryChannelsRequest(filter = Filters.neutral(), offset = 0, limit = 0))
        viewModel.onAction(ChannelListViewModel.Action.ReachedEndOfList)

        // then
        verify(queryChannelsMock, times(2)).enqueue(any())
    }

    companion object {
        val mockChannels = listOf(Channel(cid = "1", hidden = false), Channel(cid = "2", hidden = false))
    }
}
