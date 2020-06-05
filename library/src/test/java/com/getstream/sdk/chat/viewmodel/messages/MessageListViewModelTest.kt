package com.getstream.sdk.chat.viewmodel.messages

import androidx.arch.core.executor.testing.InstantExecutorExtension
import androidx.lifecycle.MutableLiveData
import com.getstream.sdk.chat.createChannel
import com.getstream.sdk.chat.createChannelUserRead
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.createMessageList
import com.getstream.sdk.chat.createUser
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Companion.MESSAGES_LIMIT
import com.getstream.sdk.chat.viewmodel.observeAll
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.call.Call
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.usecase.DeleteMessage
import io.getstream.chat.android.livedata.usecase.LoadOlderMessages
import io.getstream.chat.android.livedata.usecase.ThreadLoadMore
import io.getstream.chat.android.livedata.usecase.UseCaseHelper
import io.getstream.chat.android.livedata.usecase.WatchChannel
import io.getstream.chat.android.livedata.utils.Call2
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldNotBeNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

private const val CID = "CID:messaging"
private const val LIMIT = 30
private val CURRENT_USER = createUser(online = true)
private val CHANNEL = createChannel(CID)
private val CHANNEL_USER_READ = createChannelUserRead(CURRENT_USER)
private val MESSAGES = createMessageList()
private val MESSAGE = createMessage()

@ExtendWith(InstantExecutorExtension::class)
class MessageListViewModelTest {
    private val domain: ChatDomain = mock()
    private val client: ChatClient = mock()
    private val useCases: UseCaseHelper = mock()
    private val watchChannel: WatchChannel = mock()
    private val watchChannelCall: Call2<ChannelController> = mock()
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelController: ChannelController = mock()
    private val threadLoadMore: ThreadLoadMore = mock()
    private val loadOlderMessages: LoadOlderMessages = mock()
    private val deleteMessage: DeleteMessage = mock()
    private val deleteMessageCall: Call2<Message> = mock()
    private val flagCall: Call<Flag> = mock()
    private val flagResult: Call<Flag> = mock()

    private val messages = MutableLiveData<List<Message>>()
    private val threadMessages = MutableLiveData<List<Message>>()
    private val typing = MutableLiveData<List<User>>()
    private val reads = MutableLiveData<List<ChannelUserRead>>()


    @BeforeEach
    fun setup() {
        whenever(domain.useCases) doReturn useCases
        whenever(useCases.watchChannel) doReturn watchChannel
        whenever(watchChannel.invoke(any(), any())) doReturn watchChannelCall
        whenever(watchChannelCall.execute()) doReturn channelControllerResult
        whenever(channelControllerResult.data()) doReturn channelController
        whenever(domain.currentUser) doReturn CURRENT_USER
        whenever(channelController.messages) doReturn messages
        whenever(channelController.typing) doReturn typing
        whenever(channelController.reads) doReturn reads
        whenever(useCases.threadLoadMore) doReturn threadLoadMore
        whenever(useCases.loadOlderMessages) doReturn loadOlderMessages
        whenever(useCases.deleteMessage) doReturn deleteMessage
        whenever(deleteMessage.invoke(any())) doReturn deleteMessageCall
        whenever(client.flag(any())) doReturn flagCall

        messages.value = MESSAGES
        reads.value = listOf(CHANNEL_USER_READ)

        domain.useCases.shouldNotBeNull()
        useCases.watchChannel.shouldNotBeNull()
        watchChannel.invoke(CID, LIMIT).shouldNotBeNull()
        watchChannelCall.execute().shouldNotBeNull()
        channelControllerResult.data().shouldNotBeNull()
        domain.currentUser.shouldNotBeNull()
    }

    @Test
    fun `Should display progressbar and messages`() {
        val viewModel = MessageListViewModel(CID, domain, client)
        val stateList = viewModel.state.observeAll()

        stateList.first() shouldBeEqualTo MessageListViewModel.State.Loading
        stateList.last().apply {
            this shouldBeInstanceOf MessageListViewModel.State.Result::class
            val state = (this as MessageListViewModel.State.Result)
            state.messageListItem.listEntities.map { it.message } shouldBeEqualTo MESSAGES
        }
    }

    @Test
    fun `Should request more messages when end region reached`() {
        val viewModel = MessageListViewModel(CID, domain, client)
        viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.EndRegionReached)

        verify(loadOlderMessages).invoke(CID, MESSAGES_LIMIT)
    }

    @Test
    fun `Should delete message`() {
        val viewModel = MessageListViewModel(CID, domain, client)
        viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.DeleteMessage(MESSAGE))

        verify(deleteMessage).invoke(MESSAGE)
    }

    @Test
    fun `Should flag message`() {
        val viewModel = MessageListViewModel(CID, domain, client)
        viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.FlagMessage(MESSAGE))

        verify(client).flag(MESSAGE.user.id)
    }
}