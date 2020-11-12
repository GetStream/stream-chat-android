package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SendMessageImplTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("Flaky test. The list of messages into the livedata has some messages with a `createdAt` date in the future and break our test logic")
    fun sendMessageUseCase() = runBlocking {
        val message1 = data.createMessage()
        message1.extraData = mutableMapOf("location" to "Amsterdam")
        val channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        val result = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result as Result<Any>)

        val messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)

        val message = client.getMessage(result.data().id).execute().data()
        Truth.assertThat(message.id).isEqualTo(message1.id)
        Truth.assertThat(message.extraData).isEqualTo(message1.extraData)
    }
}
