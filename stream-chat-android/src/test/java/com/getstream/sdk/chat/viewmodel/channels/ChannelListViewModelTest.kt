package com.getstream.sdk.chat.viewmodel.channels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.createUser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestObserver
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantTaskExecutorExtension::class)
internal class ChannelListViewModelTest {

    @Test
    fun `Should display channels when there are channels available`() {
        // given
        val viewModel = Fixture().givenInitialChannelList(mockChannels).please()
        val mockObserver: Observer<ChannelsViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(ChannelsViewModel.State.Result(mockChannels))
    }

    @Test
    fun `Should display empty state info when there are no channels available`() {
        // given
        val viewModel = Fixture().givenNoChannelsAvailable().please()
        val mockObserver: Observer<ChannelsViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(ChannelsViewModel.State.NoChannelsAvailable)
    }

    @Test
    fun `Should load more channels when list is scrolled to the end region`() {
        // given
        val viewModel = Fixture()
            .givenInitialChannelList(mockChannels)
            .givenMoreChannels(moreChannels)
            .please()
        val mockObserver = TestObserver<ChannelsViewModel.State>()
        viewModel.state.observeForever(mockObserver)

        // when
        viewModel.onEvent(ChannelsViewModel.Event.ReachedEndOfList)

        // then
        val result = mockObserver.lastObservedValue.shouldBeInstanceOf<ChannelsViewModel.State.Result>()
        result.channels shouldBeEqualTo mockChannels + moreChannels
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
    private val queryChannelsCall = TestCall<QueryChannelsController>(queryChannelsControllerResult)
    private val queryChannelsController: QueryChannelsController = mock()

    private val channelsLiveData: MutableLiveData<List<Channel>> = MutableLiveData()
    private val channelsState = MutableLiveData<QueryChannelsController.ChannelsState>()

    init {
        whenever(chatDomain.user) doReturn MutableLiveData(user)
        whenever(chatDomain.queryChannels(any(), any(), any(), any())) doReturn queryChannelsCall
        whenever(queryChannelsControllerResult.isSuccess) doReturn true
        whenever(queryChannelsControllerResult.data()) doReturn queryChannelsController
        whenever(queryChannelsController.channels) doReturn channelsLiveData
        whenever(queryChannelsController.channelsState) doReturn channelsState
        whenever(queryChannelsController.loading) doReturn MutableLiveData()
        whenever(queryChannelsController.loadingMore) doReturn MutableLiveData()
    }

    fun givenNoChannelsAvailable(): Fixture = apply {
        channelsLiveData.postValue(emptyList())
        channelsState.postValue(QueryChannelsController.ChannelsState.OfflineNoResults)
    }

    fun givenInitialChannelList(channels: List<Channel>): Fixture {
        channelsLiveData.postValue(channels)
        channelsState.postValue(QueryChannelsController.ChannelsState.Result(channels))
        return this
    }

    fun givenMoreChannels(moreChannels: List<Channel>): Fixture {
        val mockCall: Call<List<Channel>> = mock()
        whenever(chatDomain.queryChannelsLoadMore(any(), any(), any(), any(), any())) doReturn mockCall
        whenever(chatDomain.queryChannelsLoadMore(any(), any(), any())) doReturn mockCall
        whenever(chatDomain.queryChannelsLoadMore(any(), any())) doReturn mockCall
        whenever(mockCall.enqueue()) doAnswer {
            val channels = (channelsLiveData.value ?: emptyList()) + moreChannels
            channelsLiveData.postValue(channels)
            channelsState.postValue(QueryChannelsController.ChannelsState.Result(channels))
        }
        return this
    }

    fun please() = ChannelsViewModel(chatDomain = chatDomain)
}
