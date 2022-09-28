package com.getstream.sdk.chat.messages

import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Config
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.TypingEvent
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.chat.android.common.messagelist.MessageListController
import io.getstream.chat.android.common.messagelist.MessageListState
import io.getstream.chat.android.offline.extensions.loadMessageById
import io.getstream.chat.android.offline.model.channel.ChannelData
import io.getstream.chat.android.offline.plugin.state.StateRegistry
import io.getstream.chat.android.offline.plugin.state.channel.ChannelState
import io.getstream.chat.android.offline.plugin.state.channel.MessagesState
import io.getstream.chat.android.offline.plugin.state.global.internal.GlobalMutableState
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.asCall
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.internal.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class MessageListControllerTests {

    @Test
    fun `Given no messages When no one is Typing Should return an empty message list`() = runTest {
        val messageState = MessagesState.Result(emptyList())
        val controller = Fixture()
            .givenCurrentUser()
            .givenChannelQuery()
            .givenNotifications()
            .givenChannelState(messageState = messageState)
            .get()

        val expectedResult = MessageListState(
            currentUser = user1,
            endOfNewMessagesReached = true,
        )

        assertEquals(expectedResult, controller.messageListState.value)
    }

    @Test
    fun `Given messageId on initialization Should load the message`() {
        val chatClient: ChatClient = mock()

        Fixture(chatClient = chatClient)
            .givenCurrentUser()
            .givenChannelQuery()
            .givenNotifications()
            .givenChannelState()
            .get(messageId = message1.id)

        verify(chatClient, times(2)).loadMessageById(CID, message1.id)
    }

    // test typing indicator logic
    @Test
    fun `Given current user is typing Should exclude the current user`() {
    }

    @Test
    fun `Given other users are typing When there are no messages Should return only the typing indicator`() {
    }

    @Test
    fun `Given other users are typing When there are messages Should add typing indicator to end`() {
    }

    // test how we merge read state
    @Test
    fun `Last message should contain the read state`() {
    }

    @Test
    fun `First message should contain the read state`() {
    }

    // test message grouping
    @Test
    fun `Given regular message followed and preceded by current user message When grouping messages Should add middle position to message`() {
    }

    @Test
    fun `Given regular message followed by other user message When grouping messages Should add top and bottom positions to messages`() {
    }

    @Test
    fun `Given regular message followed by system message When grouping messages Should add bottom position to the regular message`() {
    }

    // test date separators
    @Test
    fun `Given date separators with time difference Should add 3 date separators`() {
    }

    @Test
    fun `Given no date separators Should not add date separators`() {
    }

    // test typing action
    @Test
    fun `When the user is the only one typing, no broadcast is made`() {
    }

    // deleted visibility
    @Test
    fun `When deleted visibility is never When grouping messages Should not add any deleted messages`() {
    }

    @Test
    fun `When deleted visibility is always When grouping messages Should add all deleted messages`() {
    }

    @Test
    fun `When deleted visibility is current user When grouping messages Should add only users deleted messages`() {
    }

    // footer visibility
    @Test
    fun `When footer visibility is with time difference When message is after specified time Show message footer`() {
    }

    private class Fixture(
        private val chatClient: ChatClient = mock(),
        private val channelId: String = CID,
    ) {
        private val globalState: GlobalMutableState = mock()
        private val clientState: ClientState = mock()
        private val stateRegistry: StateRegistry = mock()

        init {
            StateRegistry.instance = stateRegistry
            GlobalMutableState.instance = globalState

            whenever(chatClient.clientState) doReturn clientState
        }

        fun givenCurrentUser(currentUser: User = user1) = apply {
            whenever(clientState.user) doReturn MutableStateFlow(currentUser)
        }

        fun givenChannelQuery(channel: Channel = Channel()) = apply {
            whenever(chatClient.queryChannel(any(), any(), any())) doReturn channel.asCall()
            whenever(chatClient.queryChannelInternal(any(), any(), any())) doReturn channel.asCall()
        }

        fun givenNotifications() = apply {
            whenever(chatClient.notifications) doReturn mock()
        }

        fun givenSendReaction() = apply {
            whenever(chatClient.sendReaction(any(), any(), any())) doReturn Reaction().asCall()
        }

        fun givenChannelState(
            channelData: ChannelData = ChannelData(
                type = CHANNEL_TYPE,
                channelId = CHANNEL_ID,
            ),
            messageState: MessagesState = MessagesState.Result(
                messages = emptyList()
            ),
        ) = apply {
            val channelState: ChannelState = mock {
                whenever(it.cid) doReturn CID
                whenever(it.channelData) doReturn MutableStateFlow(channelData)
                whenever(it.channelConfig) doReturn MutableStateFlow(Config())
                whenever(it.members) doReturn MutableStateFlow(listOf())
                whenever(it.messagesState) doReturn MutableStateFlow(messageState)
                whenever(it.typing) doReturn MutableStateFlow(TypingEvent(channelId, emptyList()))
                whenever(it.reads) doReturn MutableStateFlow(listOf())
                whenever(it.endOfOlderMessages) doReturn MutableStateFlow(false)
                whenever(it.endOfNewerMessages) doReturn MutableStateFlow(true)
                whenever(it.toChannel()) doReturn Channel(type = CHANNEL_TYPE, id = CHANNEL_ID)
                whenever(it.unreadCount) doReturn MutableStateFlow(0)
                whenever(it.insideSearch) doReturn MutableStateFlow(false)
                whenever(it.loadingNewerMessages) doReturn MutableStateFlow(false)
                whenever(it.loadingOlderMessages) doReturn MutableStateFlow(false)
            }
            whenever(stateRegistry.channel(any(), any())) doReturn channelState
            whenever(stateRegistry.scope) doReturn testCoroutines.scope
        }

        fun get(messageId: String? = null): MessageListController {
            return MessageListController(
                cid = channelId,
                chatClient = chatClient,
                clipboardHandler = mock(),
                messageId = messageId
            )
        }
    }

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()

        private const val CHANNEL_TYPE = "messaging"
        private const val CHANNEL_ID = "123"
        private const val CID = "messaging:123"

        private val user1 = User(id = "Jc", name = "Jc Miñarro")
        private val message1 = Message(id = "message-id-1", createdAt = Date())
        private val message2 = Message(id = "message-id-2", createdAt = Date())
        private val reaction1 = Reaction("message-id-1", "like", 1).apply { user = user1 }
    }
}