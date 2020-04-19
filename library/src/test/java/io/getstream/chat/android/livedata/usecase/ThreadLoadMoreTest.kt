package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class ThreadLoadMoreTest: BaseIntegrationTest() {

    @Test
    fun loadMoreForThread() = runBlocking(Dispatchers.IO) {
        // start a new thread
        val message1 = data.message1.copy().apply { id=""; createdAt= Date() }
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var result = chatDomain.useCases.sendMessage(message1).execute()
        Truth.assertThat(result.isSuccess).isTrue()
        var parentId = result.data().id
        val message2 = data.message1.copy().apply { id=""; createdAt= Date(); this.parentId=parentId }
        var result2 = chatDomain.useCases.sendMessage(message2).execute()
        Truth.assertThat(result2.isSuccess).isTrue()
        val parentMessage = channelState.getMessage(parentId)!!
        Truth.assertThat(parentMessage.id).isEqualTo(parentId)

        // get the thread
        val result3 = chatDomain.useCases.getThread(data.channel1.cid, parentId).execute()
        Truth.assertThat(result3.isSuccess).isTrue()
        val threadController = result3.data()

        // ask for more results than we have
        val result4 = chatDomain.useCases.threadLoadMore(data.channel1.cid, parentId, 100).execute()
        val endReached = threadController.endOfOlderMessages.getOrAwaitValue()
        Truth.assertThat(endReached).isTrue()

    }

}