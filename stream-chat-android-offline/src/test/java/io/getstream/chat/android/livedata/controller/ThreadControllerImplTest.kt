package io.getstream.chat.android.livedata.controller

import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertions.`should be equal to result`
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.livedata.positiveRandomInt
import io.getstream.chat.android.livedata.randomMessage
import io.getstream.chat.android.livedata.randomMessages
import io.getstream.chat.android.livedata.randomString
import io.getstream.chat.android.livedata.utils.TestCall
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.When
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.calling
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Date

@RunWith(AndroidJUnit4::class)
internal class ThreadControllerImplTest : BaseDomainTest2() {
    // TODO: feels like this class mocks too much, would be better to only mock the ChatClient

    val threadId = randomString()
    val channelControllerImpl2: ChannelControllerImpl = mock()
    val chatClient: ChatClient = mock()

    @Test
    fun `should return only one parent message`() = testCoroutines.scope.runBlockingTest {
        val parentMessage = randomMessage(id = threadId, parentId = null)
        val channelMessages = (randomMessages() + parentMessage).shuffled()
        When calling channelControllerImpl2.unfilteredMessages doReturn MutableLiveData(channelMessages)
        val threadController = ThreadControllerImpl(threadId, channelControllerImpl2, chatClient, mock())
        threadController.messages.observeForever { }

        threadController.messages.getOrAwaitValue() `should be equal to` listOf(parentMessage)
    }

    @Test
    fun `Should watch the first messages`() = testCoroutines.scope.runBlockingTest {
        val parentMessage = randomMessage(threadId, parentId = null, createdAt = Date(0))
        val channelMessages = (randomMessages() + parentMessage).shuffled()
        val limit = positiveRandomInt(30)
        val replies = randomMessages(limit) {
            randomMessage(
                parentId = threadId,
                createdAt = Date((it).toLong())
            )
        }
        When calling channelControllerImpl2.unfilteredMessages doReturn MutableLiveData(channelMessages)
        When calling chatClient.getReplies(eq(threadId), eq(limit)) doReturn TestCall(
            Result(
                replies
            )
        )
        val threadController = ThreadControllerImpl(threadId, channelControllerImpl2, chatClient, chatDomainImpl)
        threadController.messages.observeForever { }
        threadController.endOfOlderMessages.observeForever { }

        val result = threadController.watch(limit)

        result `should be equal to result` Result(replies)
        threadController.messages.getOrAwaitValue() `should be equal to` listOf(parentMessage) + replies
        threadController.endOfOlderMessages.getOrAwaitValue().`should be false`()
    }

    @Test
    fun `Should watch the first messages without receive limit messages`() = testCoroutines.scope.runBlockingTest {
        val parentMessage = randomMessage(threadId, parentId = null, createdAt = Date(0))
        val channelMessages = (randomMessages() + parentMessage).shuffled()
        val limit = positiveRandomInt(30)
        val replies = randomMessages(limit - 1) {
            randomMessage(
                parentId = threadId,
                createdAt = Date((it).toLong())
            )
        }
        When calling channelControllerImpl2.unfilteredMessages doReturn MutableLiveData(channelMessages)
        When calling chatClient.getReplies(eq(threadId), eq(limit)) doReturn TestCall(
            Result(
                replies
            )
        )
        val threadController = ThreadControllerImpl(threadId, channelControllerImpl2, chatClient, chatDomainImpl)
        threadController.messages.observeForever { }
        threadController.endOfOlderMessages.observeForever { }

        val result = threadController.watch(limit)

        result `should be equal to result` Result(replies)
        threadController.messages.getOrAwaitValue() `should be equal to` listOf(parentMessage) + replies
        threadController.endOfOlderMessages.getOrAwaitValue().`should be true`()
    }
}
