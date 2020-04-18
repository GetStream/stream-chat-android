package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseIntegrationTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class StopTypingTest: BaseIntegrationTest() {

    @Test
    fun stopTyping() = runBlocking(Dispatchers.IO) {
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        val result = chatDomain.useCases.keystroke(data.channel1.cid).execute()
        Truth.assertThat(result.data()).isTrue()
        val result2 = chatDomain.useCases.stopTyping(data.channel1.cid).execute()
        Truth.assertThat(result2.data()).isTrue()

        // this shouldnt trigger an event since nobody is typing
        val result3 = chatDomain.useCases.stopTyping(data.channel1.cid).execute()
        Truth.assertThat(result3.data()).isFalse()
    }

}