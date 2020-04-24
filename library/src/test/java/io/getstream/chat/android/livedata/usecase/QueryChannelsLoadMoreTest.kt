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
class QueryChannelsLoadMoreTest : BaseConnectedIntegrationTest() {

    @Test
    fun loadMoreTest() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        var queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, null, 0).execute()
        assertSuccess(queryChannelResult as Result<Any>)
        val queryChannelsController = queryChannelResult.data()

        var loadMoreResult =
            chatDomain.useCases.queryChannelsLoadMore(data.filter1, null, 1).execute()
        assertSuccess(loadMoreResult as Result<Any>)

        var channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(1)
    }
}
