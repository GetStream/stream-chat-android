package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class KeystrokeImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun keystroke() = runBlockingTest {
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        val result = chatDomain.useCases.keystroke(data.channel1.cid).execute()
        Truth.assertThat(result.data()).isTrue()
        val result2 = chatDomain.useCases.keystroke(data.channel1.cid).execute()
        Truth.assertThat(result2.data()).isFalse()
    }
}
