package io.getstream.chat.android.offline.usecase

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.offline.integration.BaseConnectedIntegrationTest
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsLoadMoreTest : BaseConnectedIntegrationTest() {

    @Test
    @Ignore("mock me")
    fun loadMoreTest(): Unit = runBlocking {
        // use case style syntax
        val queryChannelResult = chatDomain.queryChannels(data.filter1, QuerySort(), 0).execute()
        assertSuccess(queryChannelResult)
        val queryChannelsController = queryChannelResult.data()

        val loadMoreResult = chatDomain.queryChannelsLoadMore(data.filter1, QuerySort(), 1).execute()
        assertSuccess(loadMoreResult)

        val channels = queryChannelsController.channels.value
        channels.size shouldBeEqualTo 1
    }
}
