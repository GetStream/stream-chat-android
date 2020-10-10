package io.getstream.chat.android.livedata.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsImplLoadMoreTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("mock me")
    fun loadMoreTest() = runBlocking(Dispatchers.IO) {
        // use case style syntax
        val queryChannelResult = chatDomain.useCases.queryChannels(data.filter1, QuerySort(), 0).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()

        val loadMoreResult =
            chatDomain.useCases.queryChannelsLoadMore(data.filter1, QuerySort(), 1).execute()
        assertSuccess(loadMoreResult)

        val channels = queryChannelsController.channels.getOrAwaitValue()
        Truth.assertThat(channels.size).isEqualTo(1)
    }
}
