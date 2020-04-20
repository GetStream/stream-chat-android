package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.BaseIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

@RunWith(AndroidJUnit4::class)
class EditMessageUseCase: BaseConnectedIntegrationTest() {

    @Test
    fun editMessageUseCase() = runBlocking(Dispatchers.IO) {
        val message1 = data.createMessage()
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var result = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result as Result<Any>)

        var messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)
        message1.extraData= mutableMapOf("plaid" to true)
        var result2 = chatDomain.useCases.editMessage(message1).execute()
        assertSuccess(result2 as Result<Any>)
        messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last().id).isEqualTo(result.data().id)
        Truth.assertThat(messages.last().extraData.get("plaid")).isEqualTo(true)

    }

}