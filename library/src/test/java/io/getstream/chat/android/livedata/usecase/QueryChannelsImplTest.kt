package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QueryChannelsImplTest : BaseConnectedIntegrationTest() {

    @Test
    fun filter() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, null).execute()
        assertSuccess(queryChannelResult as Result<Any>)
        val queryChannelsController = queryChannelResult.data()
        val channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels).isNotEmpty()
    }
}
