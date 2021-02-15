package io.getstream.chat.android.ui.channel.list.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.usecase.QueryChannels
import io.getstream.chat.android.livedata.usecase.QueryChannelsLoadMore
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestObserver
import io.getstream.chat.android.ui.createUser
import org.amshove.kluent.When
import org.amshove.kluent.calling
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
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(ChannelListViewModel.State(isLoading = false, channels = mockChannels))
    }

    @Test
    fun `Should display empty state info when there are no channels available`() {
        // given
        val viewModel = Fixture().givenNoChannelsAvailable().please()
        val mockObserver: Observer<ChannelListViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(ChannelListViewModel.State(isLoading = false, channels = emptyList()))
    }

    @Test
    fun `Should load more channels when list is scrolled to the end region`() {
        // given
        val queryChannelsLoadMore: QueryChannelsLoadMore = mock()
        val viewModel = Fixture().givenMoreChannelsQuery(queryChannelsLoadMore)
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
        verify(queryChannelsLoadMore).invoke(any(), eq(ChannelListViewModel.DEFAULT_SORT), any(), any())
    }

    companion object {
        val mockChannels = listOf(Channel(cid = "1", hidden = false), Channel(cid = "2", hidden = false))
        val moreChannels = listOf(Channel(cid = "3", hidden = false), Channel(cid = "3", hidden = false))
    }
}

private class Fixture {
    private val user = createUser()
    private val chatDomain: ChatDomain = mock()
    private val useCases: UseCaseHelper = mock()
    private val queryChannels: QueryChannels = mock()
    private var queryChannelsLoadMore: QueryChannelsLoadMore = mock()
    private val queryChannelsControllerResult: Result<QueryChannelsController> = mock()
    private val queryChannelsCall = TestCall<QueryChannelsController>(queryChannelsControllerResult)
    private val queryChannelsController: QueryChannelsController = mock()

    private val channelsLiveData: MutableLiveData<List<Channel>> = MutableLiveData()
    private val channelsState = MutableLiveData<QueryChannelsController.ChannelsState>()

    init {
        When calling chatDomain.currentUser doReturn user
        When calling chatDomain.useCases doReturn useCases
        When calling useCases.queryChannels doReturn queryChannels
        When calling queryChannels.invoke(
            any(),
            eq(ChannelListViewModel.DEFAULT_SORT),
            any(),
            any()
        ) doReturn queryChannelsCall
        When calling queryChannelsControllerResult.isSuccess doReturn true
        When calling queryChannelsControllerResult.data() doReturn queryChannelsController
        When calling useCases.queryChannelsLoadMore doReturn queryChannelsLoadMore
        When calling queryChannelsController.channels doReturn channelsLiveData
        When calling queryChannelsController.channelsState doReturn channelsState
        When calling queryChannelsController.loading doReturn MutableLiveData()
        When calling queryChannelsController.loadingMore doReturn MutableLiveData()
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

    fun givenMoreChannelsQuery(queryChannelsLoadMore: QueryChannelsLoadMore): Fixture {
        this.queryChannelsLoadMore = queryChannelsLoadMore
        When calling useCases.queryChannelsLoadMore doReturn queryChannelsLoadMore
        return this
    }

    fun givenMoreChannels(moreChannels: List<Channel>): Fixture {
        val mockCall: Call<List<Channel>> = mock()
        When calling queryChannelsLoadMore.invoke(any(), any(), any(), any()) doReturn mockCall
        When calling mockCall.enqueue() doAnswer {
            val channels = (channelsLiveData.value ?: emptyList()) + moreChannels
            channelsLiveData.postValue(channels)
            channelsState.postValue(QueryChannelsController.ChannelsState.Result(channels))
        }
        return this
    }

    fun please() = ChannelListViewModel(chatDomain = chatDomain)
}
