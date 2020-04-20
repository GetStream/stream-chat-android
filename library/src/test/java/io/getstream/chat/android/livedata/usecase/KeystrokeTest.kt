package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.BaseIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.shadows.ShadowLooper
import java.util.*


@RunWith(AndroidJUnit4::class)
class KeystrokeTest: BaseConnectedIntegrationTest() {

    @Test
    fun keystroke() = runBlocking(Dispatchers.IO) {
        var channelState = chatDomain.useCases.watchChannel(data.channel1.cid, 10).execute().data()
        val result = chatDomain.useCases.keystroke(data.channel1.cid).execute()
        Truth.assertThat(result.data()).isTrue()
        val result2 = chatDomain.useCases.keystroke(data.channel1.cid).execute()
        Truth.assertThat(result2.data()).isFalse()
    }

}