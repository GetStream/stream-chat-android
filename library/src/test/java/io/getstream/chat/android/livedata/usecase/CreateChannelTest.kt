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
class CreateChannelTest: BaseIntegrationTest() {

    @Test
    fun createChannel() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var channel = chatDomain.useCases.createChannel(data.channel1).execute()
        Truth.assertThat(channel.isSuccess).isTrue()
    }

}