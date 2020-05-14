package com.getstream.sdk.chat.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.QueryChannelsController
import io.getstream.chat.android.livedata.usecase.QueryChannels
import io.getstream.chat.android.livedata.usecase.QueryChannelsLoadMore
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.utils.Call2
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ChannelListViewModelTest {
    @get:Rule
    var liveDataRule = InstantTaskExecutorRule()

    private lateinit var viewModel: ChannelsViewModelImpl

    private val chatDomain: ChatDomain = mock()
    private val useCases: UseCaseHelper = mock()
    private val queryChannels: QueryChannels = mock()
    private val queryChannelsLoadMore: QueryChannelsLoadMore = mock()
    private val queryChannelsCall: Call2<QueryChannelsController> = mock()
    private val queryChannelsControllerResult: Result<QueryChannelsController> = mock()
    private val queryChannelsController: QueryChannelsController = mock()

    @Before
    fun setup() {
        whenever(chatDomain.useCases) doReturn useCases
        whenever(useCases.queryChannels) doReturn queryChannels
        whenever(queryChannels.invoke(ChannelsViewModel.DEFAULT_FILTER, ChannelsViewModel.DEFAULT_SORT)) doReturn queryChannelsCall
        whenever(queryChannelsCall.execute()) doReturn queryChannelsControllerResult
        whenever(queryChannelsControllerResult.data()) doReturn queryChannelsController
        whenever(useCases.queryChannelsLoadMore) doReturn queryChannelsLoadMore
    }

    @Test
    fun `Should display channels when there are channels available`() {
        // given
        whenever(queryChannelsController.channels) doReturn MutableLiveData(mockChannels)
        whenever(queryChannelsController.loading) doReturn MutableLiveData()
        whenever(queryChannelsController.loadingMore) doReturn MutableLiveData()
        viewModel = ChannelsViewModelImpl(chatDomain = chatDomain)
        val mockObserver: Observer<ChannelsViewModel.State> = mock()

        // when
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(ChannelsViewModel.State.Result(mockChannels))
    }

    @Test
    fun `Should load more channels when list is scrolled to the end region`() {
        // given
        whenever(queryChannelsController.channels) doReturn MutableLiveData(mockChannels)
        whenever(queryChannelsController.loading) doReturn MutableLiveData()
        whenever(queryChannelsController.loadingMore) doReturn MutableLiveData()
        viewModel = ChannelsViewModelImpl(chatDomain = chatDomain)
        val mockObserver: Observer<ChannelsViewModel.State> = mock()

        // when
        viewModel.state.observeForever(mockObserver)
        viewModel.onAction(ChannelsViewModel.Action.ReachedEndOfList)

        // then
        verify(mockObserver).onChanged(ChannelsViewModel.State.Result(mockChannels))
        verify(queryChannelsLoadMore).invoke(ChannelsViewModel.DEFAULT_FILTER, ChannelsViewModel.DEFAULT_SORT)
    }

    companion object {
        val mockChannels = listOf(Channel(cid = "1"), Channel(cid = "2"))
    }

}