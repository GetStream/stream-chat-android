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
class LoadOlderMessagesImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun watchChannelUseCase() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        val message1 = data.createMessage()
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 0).execute().data()
        val result = chatDomain.useCases.loadOlderMessages(data.channel1.cid, 10).execute()
        assertSuccess(result as Result<Any>)
        var messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isGreaterThan(0)
        var result2 = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result2 as Result<Any>)
        messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)
    }
}
