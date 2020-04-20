package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ThreadLoadMoreTest : BaseConnectedIntegrationTest() {

    @Test
    fun loadMoreForThread() = runBlocking(Dispatchers.IO) {
        // start a new thread
        val message1 = data.createMessage()
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var result = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result as Result<Any>)
        var parentId = result.data().id
        val message2 = data.createMessage().copy().apply { this.parentId = parentId }
        var result2 = chatDomain.useCases.sendMessage(message2).execute()
        assertSuccess(result2 as Result<Any>)
        val parentMessage = channelState.getMessage(parentId)!!
        Truth.assertThat(parentMessage.id).isEqualTo(parentId)

        // get the thread
        val result3 = chatDomain.useCases.getThread(data.channel1.cid, parentId).execute()
        assertSuccess(result3 as Result<Any>)
        val threadController = result3.data()

        // ask for more results than we have
        val result4 = chatDomain.useCases.threadLoadMore(data.channel1.cid, parentId, 100).execute()
        assertSuccess(result4 as Result<Any>)
        val endReached = threadController.endOfOlderMessages.getOrAwaitValue()
        Truth.assertThat(endReached).isTrue()

    }

}