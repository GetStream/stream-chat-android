package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.offline.integration.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class WatchChannelTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("Flaky test. The list of messages into the livedata has some messages with a `createdAt` date in the future and break our test logic")
    fun watchChannelUseCase() = runBlocking {
        // use case style syntax
        val message1 = data.createMessage()
        val result0 = chatDomain.watchChannel(data.channel1.cid, 0).execute()
        val channelController = result0.data()
        val result = chatDomain.loadOlderMessages(data.channel1.cid, 10).execute()
        assertSuccess(result)
        var messages = channelController.messages.value
        Truth.assertThat(messages.size).isGreaterThan(0)
        val result2 = chatDomain.sendMessage(message1).execute()
        assertSuccess(result2)
        messages = channelController.messages.value
        Truth.assertThat(messages.last()).isEqualTo(message1)
    }
}
