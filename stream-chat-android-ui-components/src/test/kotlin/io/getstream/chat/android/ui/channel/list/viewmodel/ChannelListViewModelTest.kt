package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.querychannels.QueryChannelsController
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineRule
import io.getstream.chat.android.test.TestObserver
import io.getstream.chat.android.ui.createUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Rule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChannelListViewModelTest {

    @get:Rule
    val testCoroutines = TestCoroutineRule()

    @Test
    fun `Should display channels when there are channels available`() {
        // given
        val viewModel = Fixture().givenInitialChannelList(mockChannels).please()
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver, times(2))
            .onChanged(ChannelListViewModel.State.Result(isLoading = false, channels = mockChannels))
    }

    @Test
    fun `Should display empty state info when there are no channels available`() {
        // given
        val viewModel = Fixture().givenNoChannelsAvailable().please()
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver, times(2))
            .onChanged(ChannelListViewModel.State.Result(isLoading = false, channels = emptyList()))
    }

    @Test
    fun `Should load more channels when list is scrolled to the end region`(): Unit = runBlocking {
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
        (result as ChannelListViewModel.State.Result).channels shouldBeEqualTo mockChannels + moreChannels
    }

    companion object {
        val mockChannels = listOf(Channel(cid = "1", hidden = false), Channel(cid = "2", hidden = false))
        val moreChannels = listOf(Channel(cid = "3", hidden = false), Channel(cid = "3", hidden = false))
    }
}

private class Fixture {
    private val user = createUser()
    private val chatDomain: ChatDomain = mock()
    private val queryChannelsControllerResult: Result<QueryChannelsController> = mock()
    private val queryChannelsCall = TestCall(queryChannelsControllerResult)
    private val queryChannelsLoadMoreCall: Call<List<Channel>> = mock()
    private val queryChannelsController: QueryChannelsController = mock {
        on(it.mutedChannelIds) doReturn MutableStateFlow(emptyList())
    }

    private val channelsLiveData: MutableStateFlow<List<Channel>> = MutableStateFlow(emptyList())
    private val channelsState = MutableStateFlow<QueryChannelsController.ChannelsState>(
        QueryChannelsController.ChannelsState.NoQueryActive
    )

    init {
        whenever(chatDomain.user) doReturn MutableStateFlow(user)
        whenever(chatDomain.user) doReturn MutableStateFlow(user)
        whenever(
            chatDomain.queryChannels(
                any(),
                eq(ChannelListViewModel.DEFAULT_SORT),
                any(),
                any()
            )
        ) doReturn queryChannelsCall
        whenever(queryChannelsControllerResult.isSuccess) doReturn true
        whenever(queryChannelsControllerResult.data()) doReturn queryChannelsController
        whenever(queryChannelsController.channels) doReturn channelsLiveData
        whenever(queryChannelsController.channelsState) doReturn channelsState
        whenever(queryChannelsController.loading) doReturn MutableStateFlow(true)
        whenever(queryChannelsController.loadingMore) doReturn MutableStateFlow(false)
        whenever(queryChannelsController.loadingMore) doReturn MutableStateFlow(false)
        whenever(queryChannelsController.endOfChannels) doReturn MutableStateFlow(false)
    }

    fun givenNoChannelsAvailable(): Fixture = apply {
        channelsLiveData.value = emptyList()
        channelsState.value = QueryChannelsController.ChannelsState.OfflineNoResults
    }

    fun givenInitialChannelList(channels: List<Channel>): Fixture {
        channelsLiveData.value = channels
        channelsState.value = QueryChannelsController.ChannelsState.Result(channels)
        return this
    }

    fun givenMoreChannels(moreChannels: List<Channel>): Fixture {
        whenever(queryChannelsController.endOfChannels) doReturn MutableStateFlow(true)
        whenever(chatDomain.queryChannelsLoadMore(any(), any())) doReturn queryChannelsLoadMoreCall
        whenever(queryChannelsLoadMoreCall.enqueue()) doAnswer {
            val channels = channelsLiveData.value + moreChannels
            channelsLiveData.value = channels
            channelsState.value = QueryChannelsController.ChannelsState.Result(channels)
        }
        return this
    }

    fun please() = ChannelListViewModel(chatDomain = chatDomain)
}
