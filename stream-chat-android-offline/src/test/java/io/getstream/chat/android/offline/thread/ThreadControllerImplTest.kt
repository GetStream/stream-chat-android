package io.getstream.chat.android.offline.thread

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.logic.ChannelLogic
import io.getstream.chat.android.offline.experimental.channel.state.ChannelMutableState
import io.getstream.chat.android.offline.experimental.channel.thread.logic.ThreadLogic
import io.getstream.chat.android.offline.experimental.channel.thread.state.ThreadMutableState
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.test.asCall
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalStreamChatApi::class)
internal class ThreadControllerImplTest : BaseDomainTest2() {
    private val threadId = randomString()
    lateinit var threadMessage: Message
    lateinit var threadReply: Message
    lateinit var channelMessages: List<Message>
    lateinit var threadController: ThreadController
    lateinit var threadMutableState: ThreadMutableState
    lateinit var channelMutableState: ChannelMutableState

    @Before
    override fun setup() {
        super.setup()

        // setup a mock of channel controller that returns 2 messages that should be shown and 1 that should be filtered
        threadMessage = data.createMessage().apply { id = threadId }
        threadReply = data.createMessage().apply { parentId = threadId }
        channelMessages = listOf(data.message1, threadMessage, threadReply)
        channelMutableState =
            ChannelMutableState(
                "channelType", "channelId", chatDomainImpl.scope, chatDomainImpl.user,
                MutableStateFlow(
                    emptyMap()
                )
            ).apply {
                hideMessagesBefore = null
            }
        setMessages(channelMessages)

        val channelLogic = ChannelLogic(channelMutableState, chatDomainImpl)
        threadMutableState = ThreadMutableState(threadId, channelMutableState, chatDomainImpl.scope)
        val threadLogic = ThreadLogic(threadMutableState, channelLogic)
        threadController = ThreadController(threadMutableState, threadLogic, clientMock)
    }

    @Test
    fun `the correct messages on the channelController should be shown on the thread`() =
        testCoroutines.scope.runBlockingTest {
            threadController.messages.test {
                awaitItem()
                // verify we see the correct 2 messages
                val expectedMessages = listOf(threadMessage, threadReply)
                awaitItem() shouldBeEqualTo expectedMessages
            }
        }

    @Test
    fun `new messages on the channel controller should show up on the thread`() = testCoroutines.scope.runBlockingTest {
        // add an extra message
        val threadReply2 = data.createMessage().apply { parentId = threadId }
        threadController.messages.test {
            awaitItem()
            setMessages(listOf(data.message1, threadMessage, threadReply, threadReply2))
            // verify we see the correct 3 messages
            val expectedMessages = listOf(threadMessage, threadReply, threadReply2)
            awaitItem() shouldBeEqualTo expectedMessages
        }
    }

    @Test
    fun `removing messages on the channel controller should remove them from the thread`() =
        testCoroutines.scope.runBlockingTest {
            // remove a message
            threadController.messages.test {
                awaitItem()
                setMessages(listOf(data.message1, threadMessage))
                // verify we see the correct message
                val expectedMessages = listOf(threadMessage)
                awaitItem() shouldBeEqualTo expectedMessages
            }
        }

    @Test
    fun `loading more should set loading and endReached variables`() = testCoroutines.scope.runBlockingTest {
        // mock the loadOlderThreadMessages to return 1 result when asking for 2 messages
        val threadReply2 = data.createMessage().apply { parentId = threadId }
        whenever(clientMock.getRepliesInternal(any(), any())) doReturn listOf(threadReply2).asCall()
        threadController.loadOlderMessages(2)

        // verify that loading is false and end reached is true
        val loading = threadController.loadingOlderMessages.value
        val endReached = threadController.endOfOlderMessages.value
        loading shouldBeEqualTo false
        endReached shouldBeEqualTo true
    }

    private fun setMessages(messages: List<Message>) {
        channelMutableState._messages.value = messages.associateBy(Message::id)
    }
}
