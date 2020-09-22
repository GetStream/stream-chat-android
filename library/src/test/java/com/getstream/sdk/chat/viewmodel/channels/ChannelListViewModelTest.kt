package com.getstream.sdk.chat.viewmodel.channels

import androidx.arch.core.executor.testing.InstantExecutorExtension
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.getstream.sdk.chat.createUser
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
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
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class ChannelListViewModelTest {
    private lateinit var viewModel: ChannelsViewModelImpl

    private val user = createUser()
    private val chatDomain: ChatDomain = mock()
    private val useCases: UseCaseHelper = mock()
    private val queryChannels: QueryChannels = mock()
    private val queryChannelsLoadMore: QueryChannelsLoadMore = mock()
    private val queryChannelsCall: Call2<QueryChannelsController> = mock()
    private val queryChannelsControllerResult: Result<QueryChannelsController> = mock()
    private val queryChannelsController: QueryChannelsController = mock()

    @BeforeEach
    fun setup() {
        When calling chatDomain.currentUser doReturn user
        When calling chatDomain.useCases doReturn useCases
        When calling useCases.queryChannels doReturn queryChannels
        When calling queryChannels.invoke(any(), eq(ChannelsViewModel.DEFAULT_SORT), any(), any()) doReturn queryChannelsCall
        When calling queryChannelsCall.execute() doReturn queryChannelsControllerResult
        When calling queryChannelsControllerResult.data() doReturn queryChannelsController
        When calling useCases.queryChannelsLoadMore doReturn queryChannelsLoadMore
    }

    @Test
    fun `Should display channels when there are channels available`() {
        // given
        mockChannels()
        val mockObserver: Observer<ChannelsViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(ChannelsViewModel.State.Result(mockChannels))
    }

    @Test
    fun `Should display empty state info when there are no channels available`() {
        // given
        mockNoChannelsAvailable()
        val mockObserver: Observer<ChannelsViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // then
        verify(mockObserver).onChanged(ChannelsViewModel.State.NoChannelsAvailable)
    }

    @Test
    fun `Should load more channels when list is scrolled to the end region`() {
        // given
        mockChannels()
        val mockObserver: Observer<ChannelsViewModel.State> = mock()
        viewModel.state.observeForever(mockObserver)

        // when
        viewModel.onEvent(ChannelsViewModel.Event.ReachedEndOfList)

        // then
        verify(mockObserver).onChanged(ChannelsViewModel.State.Result(mockChannels))
        verify(queryChannelsLoadMore).invoke(any(), eq(ChannelsViewModel.DEFAULT_SORT), any(), any())
    }

    private fun mockChannels() {
        whenever(queryChannelsController.channels) doReturn MutableLiveData(mockChannels)
        whenever(queryChannelsController.loading) doReturn MutableLiveData()
        whenever(queryChannelsController.loadingMore) doReturn MutableLiveData()
        viewModel = ChannelsViewModelImpl(chatDomain = chatDomain)
    }

    private fun mockNoChannelsAvailable() {
        whenever(queryChannelsController.channels) doReturn MutableLiveData(emptyList())
        whenever(queryChannelsController.loading) doReturn MutableLiveData()
        whenever(queryChannelsController.loadingMore) doReturn MutableLiveData()
        viewModel = ChannelsViewModelImpl(chatDomain = chatDomain)
    }

    companion object {
        val mockChannels = listOf(Channel(cid = "1"), Channel(cid = "2"))
    }
}
