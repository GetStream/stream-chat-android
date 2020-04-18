package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryChannelsTest: BaseIntegrationTest() {

    @Test
    fun filter() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, null).execute()
        Truth.assertThat(queryChannelResult.isSuccess).isTrue()
        val queryChannelsController = queryChannelResult.data()
        val channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels).isNotEmpty()
    }

}