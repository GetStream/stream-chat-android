package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class StopTypingImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun stopTyping() = runBlocking {
        chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute()
        val result = chatDomain.useCases.keystroke(data.channel1.cid).execute()
        Truth.assertThat(result.data()).isTrue()
        val result2 = chatDomain.useCases.stopTyping(data.channel1.cid).execute()
        Truth.assertThat(result2.data()).isTrue()

        // this shouldnt trigger an event since nobody is typing
        val result3 = chatDomain.useCases.stopTyping(data.channel1.cid).execute()
        Truth.assertThat(result3.data()).isFalse()
    }
}
