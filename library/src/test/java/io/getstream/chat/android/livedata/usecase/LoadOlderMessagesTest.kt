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
class LoadOlderMessagesTest: BaseIntegrationTest() {

    @Test
    fun watchChannelUseCase() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        val message1 = data.message1.apply { id=""; createdAt= Date() }
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        var messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isGreaterThan(0)
        var result = chatDomain.useCases.sendMessage(message1).execute()
        Truth.assertThat(result.isSuccess).isTrue()
        messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)

    }

}