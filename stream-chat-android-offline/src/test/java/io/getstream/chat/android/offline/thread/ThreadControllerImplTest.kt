package io.getstream.chat.android.offline.thread

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.offline.channel.ChannelController
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ThreadControllerImplTest : BaseDomainTest2() {
    private val threadId = randomString()
    private val channelControllerMock = mock<ChannelController>()
    lateinit var threadMessage: Message
    lateinit var threadReply: Message
    lateinit var channelMessages: MutableStateFlow<List<Message>>
    lateinit var threadController: ThreadController

    @Before
    override fun setup() {
        super.setup()

        // setup a mock of channel controller that returns 2 messages that should be shown and 1 that should be filtered
        threadMessage = data.createMessage().apply { id = threadId }
        threadReply = data.createMessage().apply { parentId = threadId }
        channelMessages = MutableStateFlow(listOf(data.message1, threadMessage, threadReply))
        whenever(channelControllerMock.unfilteredMessages) doReturn channelMessages
        whenever(channelControllerMock.hideMessagesBefore) doReturn null
        threadController = ThreadController(threadId, channelControllerMock, chatDomainImpl)
    }

    @Test
    fun `the correct messages on the channelController should be shown on the thread`() =
        testCoroutines.scope.runBlockingTest {
            val messages = threadController.messages.value

            // verify we see the correct 2 messages
            val expectedMessages = listOf(threadMessage, threadReply)
            Truth.assertThat(messages).isEqualTo(expectedMessages)
        }

    @Test
    fun `new messages on the channel controller should show up on the thread`() = testCoroutines.scope.runBlockingTest {
        // add an extra message
        val threadReply2 = data.createMessage().apply { parentId = threadId }
        channelMessages.value = listOf(data.message1, threadMessage, threadReply, threadReply2)
        val messages = threadController.messages.value

        // verify we see the correct 3 messages
        val expectedMessages = listOf(threadMessage, threadReply, threadReply2)
        Truth.assertThat(messages).isEqualTo(expectedMessages)
    }

    @Test
    fun `removing messages on the channel controller should remove them from the thread`() =
        testCoroutines.scope.runBlockingTest {
            // remove a message
            channelMessages.value = listOf(data.message1, threadMessage)
            val messages = threadController.messages.value

            // verify we see the correct message
            val expectedMessages = listOf(threadMessage)
            Truth.assertThat(messages).isEqualTo(expectedMessages)
        }

    @Test
    fun `loading more should set loading and endReached variables`() = testCoroutines.scope.runBlockingTest {
        // mock the loadOlderThreadMessages to return 1 result when asking for 2 messages
        val threadReply2 = data.createMessage().apply { parentId = threadId }
        whenever(channelControllerMock.loadOlderThreadMessages(any(), any(), eq(null))) doReturn Result(
            listOf(
                threadReply2
            )
        )
        threadController.loadOlderMessages(2)

        // verify that loading is false and end reached is true
        val loading = threadController.loadingOlderMessages.value
        val endReached = threadController.endOfOlderMessages.value
        Truth.assertThat(loading).isEqualTo(false)
        Truth.assertThat(endReached).isEqualTo(true)
    }
}
