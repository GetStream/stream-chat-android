package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class WatchChannelImplTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("Flaky test. The list of messages into the livedata has some messages with a `createdAt` date in the future and break our test logic")
    fun watchChannelUseCase() = runBlocking {
        // use case style syntax
        val message1 = data.createMessage()
        val result0 = chatDomain.useCases.watchChannel(data.channel1.cid, 0).execute()
        val channelController = result0.data()
        val result = chatDomain.useCases.loadOlderMessages(data.channel1.cid, 10).execute()
        assertSuccess(result)
        var messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isGreaterThan(0)
        val result2 = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result2)
        messages = channelController.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)
    }
}
