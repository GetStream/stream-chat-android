package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GetThreadImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun getThread() = runBlocking(Dispatchers.IO) {
        // start a new thread
        val message1 = data.createMessage()
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var result = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result)
        var parentId = result.data().id
        val message2 = data.createMessage().apply { this.parentId = parentId }
        var result2 = chatDomain.useCases.sendMessage(message2).execute()
        assertSuccess(result2)
        val parentMessage = channelState.getMessage(parentId)!!
        Truth.assertThat(parentMessage.id).isEqualTo(parentId)

        // get the thread
        val result3 = chatDomain.useCases.getThread(data.channel1.cid, parentId).execute()
        assertSuccess(result3)
        val threadController = result3.data()

        val messages = threadController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(2)
    }
}
