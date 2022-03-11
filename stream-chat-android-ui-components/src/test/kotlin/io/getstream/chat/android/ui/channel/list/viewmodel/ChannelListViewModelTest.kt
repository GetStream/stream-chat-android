package io.getstream.chat.android.ui.channel.list.viewmodel

import org.mockito.Mockito.times

/*
@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChannelListViewModelTest {

    @Test
    fun `Should display channels when there are channels available`() {
        // given
        val viewModel = Fixture().givenInitialChannelList(mockChannels).please()
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver, times(2))
            .onChanged(ChannelListViewModel.State(isLoading = false, channels = mockChannels))
    }

    @Test
    fun `Should display empty state info when there are no channels available`() {
        // given
        val viewModel = Fixture().givenNoChannelsAvailable().please()
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver, times(2))
            .onChanged(ChannelListViewModel.State(isLoading = false, channels = emptyList()))
    }

    @Test
    fun `Should load more channels when list is scrolled to the end region`() {
        // given
        val viewModel = Fixture()
            .givenInitialChannelList(mockChannels)
            .givenMoreChannels(moreChannels)
            .please()
        val mockObserver = TestObserver<ChannelListViewModel.State>()
        viewModel.state.observeForever(mockObserver)

        // when
        viewModel.onAction(ChannelListViewModel.Action.ReachedEndOfList)

        // then
        val result = mockObserver.lastObservedValue.shouldBeInstanceOf<ChannelListViewModel.State>()
        result.channels shouldBeEqualTo mockChannels + moreChannels
    }

    companion object {
        val mockChannels = listOf(Channel(cid = "1", hidden = false), Channel(cid = "2", hidden = false))
        val moreChannels = listOf(Channel(cid = "3", hidden = false), Channel(cid = "3", hidden = false))
    }
}

private class Fixture {
    private val user = createUser()
    private val chatDomain: ChatDomain = mock {
        on(it.channelMutes) doReturn MutableStateFlow(emptyList())
    }
    private val chatClient: ChatClient = mock()
    private val queryChannelsControllerResult: Result<QueryChannelsController> = mock()
    private val queryChannelsCall = TestCall(queryChannelsControllerResult)
    private val queryChannelsLoadMoreCall: Call<List<Channel>> = mock()
    private val queryChannelsController: QueryChannelsController = mock()

    private val channelsStateFlow: MutableStateFlow<List<Channel>> = MutableStateFlow(emptyList())
    private val channelsState = MutableStateFlow<QueryChannelsController.ChannelsState>(
        QueryChannelsController.ChannelsState.NoQueryActive
    )

    init {
        whenever(chatDomain.user) doReturn MutableStateFlow(user)
        whenever(
            chatDomain.queryChannels(
                any(),
                eq(ChannelListViewModel.DEFAULT_SORT),
                any(),
                any(),
                any(),
            )
        ) doReturn queryChannelsCall
        whenever(queryChannelsControllerResult.isSuccess) doReturn true
        whenever(queryChannelsControllerResult.data()) doReturn queryChannelsController
        whenever(queryChannelsController.channels) doReturn channelsStateFlow
        whenever(queryChannelsController.channelsState) doReturn channelsState
        whenever(queryChannelsController.loading) doReturn MutableStateFlow(true)
        whenever(queryChannelsController.loadingMore) doReturn MutableStateFlow(false)
        whenever(queryChannelsController.endOfChannels) doReturn MutableStateFlow(false)
    }

    fun givenNoChannelsAvailable(): Fixture = apply {
        channelsStateFlow.value = emptyList()
        channelsState.value = QueryChannelsController.ChannelsState.OfflineNoResults
    }

    fun givenInitialChannelList(channels: List<Channel>): Fixture {
        channelsStateFlow.value = channels
        channelsState.value = QueryChannelsController.ChannelsState.Result(channels)
        return this
    }

    fun givenMoreChannels(moreChannels: List<Channel>): Fixture {
        whenever(chatDomain.queryChannelsLoadMore(any(), any(), any(), any(), any())) doReturn queryChannelsLoadMoreCall
        whenever(chatClient.queryChannels(any())) doReturn queryChannelsLoadMoreCall
        whenever(queryChannelsLoadMoreCall.enqueue(any())) doAnswer {
            val channels = channelsStateFlow.value + moreChannels
            channelsStateFlow.value = channels
            channelsState.value = QueryChannelsController.ChannelsState.Result(channels)
        }
        return this
    }

    fun please() = ChannelListViewModel(chatDomain = chatDomain, chatClient = chatClient)
}
 */
