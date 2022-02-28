package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.offline.integration.BaseConnectedIntegrationTest
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeTrue
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ThreadLoadMoreTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("Mock Me")
    fun loadMoreForThread() = runBlockingTest {
        // start a new thread
        val message1 = data.createMessage()
        val channelState = chatDomain.watchChannel(data.channel1.cid, 10).execute().data()
        val result = client.channel(data.channel1.cid).sendMessage(message1).execute()
        assertSuccess(result)
        val parentId = result.data().id
        val message2 = data.createMessage().copy().apply { this.parentId = parentId }
        val result2 = client.channel(data.channel1.cid).sendMessage(message2).execute()
        assertSuccess(result2)
        val parentMessage = channelState.getMessage(parentId)!!
        parentMessage.id shouldBeEqualTo parentId
        // get the thread
        val result3 = chatDomain.getThread(data.channel1.cid, parentId).execute()
        assertSuccess(result3)
        val threadController = result3.data()

        // ask for more results than we have
        val result4 = chatDomain.threadLoadMore(data.channel1.cid, parentId, 100).execute()
        assertSuccess(result4)
        val endReached = threadController.endOfOlderMessages.value
        endReached.shouldBeTrue()
    }
}
