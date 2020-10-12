package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DeleteMessageImplTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("test occasionally fails, not sure why")
    fun deleteMessageUseCase() = runBlocking(Dispatchers.IO) {
        val message1 = data.createMessage()
        client.subscribe { println(it.type) }
        val channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        val result = chatDomain.useCases.sendMessage(message1).execute()
        assertSuccess(result)
        var messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last()).isEqualTo(message1)
        val result2 = chatDomain.useCases.deleteMessage(message1).execute()
        assertSuccess(result2)
        messages = channelState.messages.getOrAwaitValue()
        Truth.assertThat(messages.last().id).isEqualTo(result.data().id)
        Truth.assertThat(messages.last().deletedAt).isNotNull()
    }
}
