package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.livedata.randomString
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ThreadControllerImplTest : BaseDomainTest2() {
    val threadId = randomString()
    val channelControllerMock = mockk<ChannelControllerImpl>()
    lateinit var threadMessage: Message
    lateinit var threadReply: Message
    lateinit var channelMessages: MutableStateFlow<List<Message>>
    lateinit var threadController: ThreadControllerImpl

    @Before
    override fun setup() {
        super.setup()

        // setup a mock of channel controller that returns 2 messages that should be shown and 1 that should be filtered
        threadMessage = data.createMessage().apply { id = threadId }
        threadReply = data.createMessage().apply { parentId = threadId }
        channelMessages = MutableStateFlow(listOf(data.message1, threadMessage, threadReply))
        every { channelControllerMock.unfilteredMessages } returns channelMessages
        every { channelControllerMock.hideMessagesBefore } returns null
        threadController = ThreadControllerImpl(threadId, channelControllerMock, clientMock, chatDomainImpl)
    }

    @Test
    fun `the correct messages on the channelController should be shown on the thread`() = testCoroutines.scope.runBlockingTest {
        val messages = threadController.messages.getOrAwaitValue()

        // verify we see the correct 2 messages
        val expectedMessages = listOf(threadMessage, threadReply)
        Truth.assertThat(messages).isEqualTo(expectedMessages)
    }

    @Test
    fun `new messages on the channel controller should show up on the thread`() = testCoroutines.scope.runBlockingTest {
        // add an extra message
        val threadReply2 = data.createMessage().apply { parentId = threadId }
        channelMessages.value = listOf(data.message1, threadMessage, threadReply, threadReply2)
        val messages = threadController.messages.getOrAwaitValue()

        // verify we see the correct 3 messages
        val expectedMessages = listOf(threadMessage, threadReply, threadReply2)
        Truth.assertThat(messages).isEqualTo(expectedMessages)
    }

    @Test
    fun `removing messages on the channel controller should remove them from the thread`() = testCoroutines.scope.runBlockingTest {
        // remove a message
        channelMessages.value = listOf(data.message1, threadMessage)
        val messages = threadController.messages.getOrAwaitValue()

        // verify we see the correct message
        val expectedMessages = listOf(threadMessage)
        Truth.assertThat(messages).isEqualTo(expectedMessages)
    }

    @Test
    fun `loading more should set loading and endReached variables`() = testCoroutines.scope.runBlockingTest {
        // mock the loadOlderThreadMessages to return 1 result when asking for 2 messages
        val threadReply2 = data.createMessage().apply { parentId = threadId }
        every { channelControllerMock.loadOlderThreadMessages(any(), eq(2), any()) } returns Result(listOf(threadReply2))
        threadController.loadOlderMessages(2)

        // verify that loading is false and end reached is true
        val loading = threadController.loadingOlderMessages.getOrAwaitValue()
        val endReached = threadController.endOfOlderMessages.getOrAwaitValue()
        Truth.assertThat(loading).isEqualTo(false)
        Truth.assertThat(endReached).isEqualTo(true)
    }
}
