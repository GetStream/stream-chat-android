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


data class Person(var name: String)
data class Animal(var name: String)


@RunWith(AndroidJUnit4::class)
class SendMessageImplTest : BaseConnectedIntegrationTest() {
    @Test
    fun sendMessageUseCase() = runBlocking(Dispatchers.Main) {
        val message1 = data.createMessage()
        message1.extraData = mutableMapOf("location" to "Amsterdam")
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var result = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result as Result<Any>)

        var messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)

        var message = client.getMessage(result.data().id).execute().data()
        Truth.assertThat(message.id).isEqualTo(message1.id)
        Truth.assertThat(message.extraData).isEqualTo(message1.extraData)
    }
}
