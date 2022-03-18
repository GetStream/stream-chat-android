package com.getstream.sdk.chat.viewmodel.messages

import com.getstream.sdk.chat.MockChatClientBuilder
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.createMessage
import com.getstream.sdk.chat.createMessageList
import com.getstream.sdk.chat.createUser
import com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel.Companion.DEFAULT_MESSAGES_LIMIT
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.offline.extensions.loadOlderMessages
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.test.InstantTaskExecutorExtension
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.observeAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.Date

private const val CID = "CID:messaging"
private const val CHANNEL_TYPE = "messaging"
private const val CHANNEL_ID = "awesome-channel"

private val CURRENT_USER = createUser(online = true)
private val MESSAGES = createMessageList {
    createMessage(
        user = CURRENT_USER,
        createdAt = Date.from(Instant.now()),
        parentId = null
    )
}
private val MESSAGE = createMessage(createdAt = Date.from(Instant.now()), user = CURRENT_USER)
private val THREAD_PARENT_MESSAGE =
    createMessage(text = "parent message", createdAt = Date.from(Instant.now()), user = CURRENT_USER)
private val THREAD_MESSAGES = createMessageList {
    createMessage(
        createdAt = Date.from(Instant.now()),
        parentId = THREAD_PARENT_MESSAGE.id,
        user = CURRENT_USER
    )
}

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(InstantTaskExecutorExtension::class)
internal class MessageListViewModelTest {

    private val loadingOlderMessages = MutableStateFlow(false)
    private val messagesState = MutableStateFlow(MessagesState.Loading as MessagesState)
    private val messages = MutableStateFlow(listOf<Message>())

    private val channelState: ChannelState = spy {
        whenever(it.channelType) doReturn CHANNEL_TYPE
        whenever(it.channelId) doReturn CHANNEL_ID
        whenever(it.messages) doReturn messages
        whenever(it.reads) doReturn MutableStateFlow(listOf())
        whenever(it.messagesState).doAnswer { messagesState }
        whenever(it.loading) doReturn MutableStateFlow(false)
        whenever(it.loadingOlderMessages) doReturn loadingOlderMessages
        whenever(it.channelData) doReturn MutableStateFlow(ChannelData(Channel()))
    }

    private val stateRegistry: StateRegistry = mock {
        whenever(it.channel(any(), any())) doReturn channelState
    }
    private val chatClient: ChatClient = MockChatClientBuilder {
        mock {
            whenever(it.queryChannel(any(), any(), any())) doReturn Channel().asCall()
            whenever(it.notifications) doReturn mock()
            whenever(it.flagMessage(any())) doReturn mock()
            whenever(it.deleteMessage(any(), any())) doReturn mock()
        }
    }.build()

    @BeforeEach
    fun setup() {
        StateRegistry.instance = stateRegistry
    }

    @Test
    @Disabled("Can not be tested until we use mockk or other way to mock static function")
    fun `Should request more messages when end region reached`() {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.EndRegionReached)

        verify(chatClient).loadOlderMessages(CID, DEFAULT_MESSAGES_LIMIT)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    // TODO fix this test
    @Disabled("State does not update properly")
    @Test
    fun `Should display progressbar and messages`() = runBlockingTest {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        val stateList = viewModel.state.observeAll()

        stateList.first() shouldBeEqualTo MessageListViewModel.State.Loading

        messagesState.emit(MessagesState.Result(listOf()))
        advanceUntilIdle()

        stateList.last().apply {
            this shouldBeInstanceOf MessageListViewModel.State.Result::class
            val state = (this as MessageListViewModel.State.Result)
            state.messageListItem.items.filterIsInstance<MessageListItem.MessageItem>().map {
                it.message
            } shouldBeEqualTo MESSAGES
        }
    }

    @Test
    fun `When delete event doesn't have hard flag Should delete message`() {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.DeleteMessage(MESSAGE, hard = false))

        verify(chatClient).deleteMessage(MESSAGE.id, false)
    }

    @Test
    fun `When delete event has hard flag Should hard delete message`() {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.DeleteMessage(MESSAGE, hard = true))

        verify(chatClient).deleteMessage(MESSAGE.id, true)
    }

    @Test
    fun `Should flag message`() {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.FlagMessage(MESSAGE))

        verify(chatClient).flagMessage(MESSAGE.id)
    }

    @Test
    fun `Should navigate up from normal mode on back click`() {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        val states = viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.BackButtonPressed)

        states.last() shouldBeEqualTo MessageListViewModel.State.NavigateUp
    }

    @Test
    @Disabled(
        "Can not be tested until we use mockk or other way to mock static function. " +
            "ChatClient.getRepliesAsState is called."
    )
    fun `Should return from thread to normal mode on back click`() {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        val states = viewModel.state.observeAll()
        viewModel.onEvent(MessageListViewModel.Event.ThreadModeEntered(MESSAGE))

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
    @Disabled(
        "Can not be tested until we use mockk or other way to mock static function. " +
            "ChatClient.getRepliesAsState is called."
    )
    fun `Should display thread messages when thread mode entered`() {
        val viewModel = MessageListViewModel(CID, chatClient = chatClient)
        val states = viewModel.state.observeAll()

        viewModel.onEvent(MessageListViewModel.Event.ThreadModeEntered(MESSAGE))

        states.last().run {
            this shouldBeInstanceOf MessageListViewModel.State.Result::class
            (this as MessageListViewModel.State.Result).let {
                it.messageListItem.isThread shouldBeEqualTo true
                it.messageListItem.items.run {
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
