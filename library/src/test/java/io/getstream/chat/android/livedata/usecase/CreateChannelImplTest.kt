package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateChannelImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun createChannel() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var channel = chatDomain.useCases.createChannel(data.channel1).execute()
        Truth.assertThat(channel.isSuccess).isTrue()
    }
}
