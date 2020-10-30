package com.getstream.sdk.chat.viewmodel.messages

import androidx.arch.core.executor.testing.InstantExecutorExtension
import androidx.lifecycle.MutableLiveData
import com.getstream.sdk.chat.adapter.MessageListItem
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
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.ChannelUserRead
import io.getstream.chat.android.client.models.Flag
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.MessagesUpdate
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.ChatDomain
import io.getstream.chat.android.livedata.controller.ChannelController
import io.getstream.chat.android.livedata.controller.ThreadController
import io.getstream.chat.android.livedata.usecase.DeleteMessage
import io.getstream.chat.android.livedata.usecase.GetThread
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
import java.time.Instant
import java.util.Date

private const val CID = "CID:messaging"
private const val LIMIT = 30
private val CURRENT_USER = createUser(online = true)
private val CHANNEL = createChannel(CID)
private val CHANNEL_USER_READ = createChannelUserRead(CURRENT_USER)
private val MESSAGES = createMessageList {
    createMessage(
        user = CURRENT_USER,
        createdAt = Date.from(Instant.now()),
        parentId = null
    )
}
private val MESSAGES_UPDATE = MessagesUpdate(false, MESSAGES)

private val MESSAGE = createMessage(createdAt = Date.from(Instant.now()), user = CURRENT_USER)
private val THREAD_PARENT_MESSAGE = createMessage(text = "parent message", createdAt = Date.from(Instant.now()), user = CURRENT_USER)
private val THREAD_MESSAGES = createMessageList {
    createMessage(
        createdAt = Date.from(Instant.now()),
        parentId = THREAD_PARENT_MESSAGE.id,
        user = CURRENT_USER
    )
}

@ExtendWith(InstantExecutorExtension::class)
internal class MessageListViewModelTest {
    private val domain: ChatDomain = mock()
    private val client: ChatClient = mock()
    private val useCases: UseCaseHelper = mock()
    private val watchChannel: WatchChannel = mock()
    private val watchChannelCall: Call2<ChannelController> = mock()
    private val channelControllerResult: Result<ChannelController> = mock()
    private val channelController: ChannelController = mock()
    private val threadLoadMore: ThreadLoadMore = mock()
    private val threadLoadMoreCall: Call2<List<Message>> = mock()
    private val threadLoadMoreResult: Result<List<Message>> = mock()
    private val loadOlderMessages: LoadOlderMessages = mock()
    private val loadOlderMessagesCall: Call2<Channel> = mock()
    private val loadOlderMessagesResult: Result<Channel> = mock()
    private val getThread: GetThread = mock()
    private val deleteMessage: DeleteMessage = mock()
    private val deleteMessageCall: Call2<Message> = mock()
    private val getThreadCall: Call2<ThreadController> = mock()
    private val getThreadResult: Result<ThreadController> = mock()
    private val threadController: ThreadController = mock()
    private val flagCall: Call<Flag> = mock()
    private val flagResult: Call<Flag> = mock()

    private val messages = MutableLiveData<MessagesUpdate>()
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
        whenever(threadLoadMore.invoke(any(), any(), any())) doReturn threadLoadMoreCall
        whenever(threadLoadMoreCall.execute()) doReturn threadLoadMoreResult
        whenever(threadLoadMoreResult.data()) doReturn emptyList()
        whenever(useCases.loadOlderMessages) doReturn loadOlderMessages
        whenever(useCases.loadOlderMessages.invoke(any(), any())) doReturn loadOlderMessagesCall
        whenever(loadOlderMessagesCall.execute()) doReturn loadOlderMessagesResult
        whenever(useCases.deleteMessage) doReturn deleteMessage
        whenever(useCases.getThread) doReturn getThread
        whenever(deleteMessage.invoke(any())) doReturn deleteMessageCall
        whenever(getThread.invoke(any(), any())) doReturn getThreadCall
        whenever(getThreadCall.execute()) doReturn getThreadResult
        whenever(getThreadResult.data()) doReturn threadController
        whenever(threadController.messages) doReturn MutableLiveData(listOf(THREAD_PARENT_MESSAGE) + THREAD_MESSAGES)
        whenever(client.flagMessage(any())) doReturn flagCall

        messages.value = MESSAGES_UPDATE
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
            state.messageListItem.items.filterIsInstance<MessageListItem.MessageItem>().map {
                it.message
            } shouldBeEqualTo MESSAGES
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

        verify(client).flagMessage(MESSAGE.id)
    }

    @Test
    fun `Should return from thread to normal mode on back click`() {
        val viewModel = MessageListViewModel(CID, domain, client)
        viewModel.onEvent(MessageListViewModel.Event.ThreadModeEntered(MESSAGE))
        val states = viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)

        states.last().run {
            this shouldBeInstanceOf MessageListViewModel.State.Result::class
            (this as MessageListViewModel.State.Result).let { it ->
                it.messageListItem.run {
                    isThread shouldBeEqualTo false
                    isTyping shouldBeEqualTo false

                    val messages = items.run {
                        get(1).apply {
                            this as MessageListItem.MessageItem
                            positions shouldBeEqualTo listOf(MessageListItem.Position.TOP)
                        }
                        last().apply {
                            this as MessageListItem.MessageItem
                            positions shouldBeEqualTo listOf(MessageListItem.Position.BOTTOM)
                        }
                        (2 until size - 1).forEach {
                            get(it).apply {
                                this as MessageListItem.MessageItem
                                positions shouldBeEqualTo listOf(MessageListItem.Position.MIDDLE)
                            }
                        }
                        filterIsInstance<MessageListItem.MessageItem>().map {
                            it.message
                        } shouldBeEqualTo MESSAGES
                    }
                }
            }
        }
    }

    @Test
    fun `Should navigate up from normal mode on back click`() {
        val viewModel = MessageListViewModel(CID, domain, client)
        val states = viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)

        states.last() shouldBeEqualTo MessageListViewModel.State.NavigateUp
    }

    @Test
    fun `Should display thread messages when thread mode entered`() {
        val viewModel = MessageListViewModel(CID, domain, client)
        val states = viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.ThreadModeEntered(MESSAGE))

        states.last().run {
            this shouldBeInstanceOf MessageListViewModel.State.Result::class
            (this as MessageListViewModel.State.Result).let {
                it.messageListItem.isThread shouldBeEqualTo true
                it.messageListItem.items.run {
                    println("JcLog: list -> $this")
                    first().run {
                        this shouldBeInstanceOf MessageListItem.MessageItem::class.java
                        this as MessageListItem.MessageItem
                        message shouldBeEqualTo THREAD_PARENT_MESSAGE
                    }
                    drop(1).first().apply {
                        this shouldBeInstanceOf MessageListItem.ThreadSeparatorItem::class.java
                    }
                    drop(2).map { item ->
                        (item as MessageListItem.MessageItem).message
                    } shouldBeEqualTo THREAD_MESSAGES
                }
            }
        }
    }
}
