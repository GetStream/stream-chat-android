package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.controller.QueryChannelsSpec
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsImplRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomainImpl.repos.queryChannels }

    @Test
    fun testInsertAndRead() = runBlocking {
        val queryChannelsSpec = QueryChannelsSpec(data.filter1, QuerySort())
        queryChannelsSpec.cids = listOf("a", "b", "c")
        repo.insert(queryChannelsSpec)
        val fromDB = repo.selectByFilterAndQuerySort(queryChannelsSpec)
        Truth.assertThat(queryChannelsSpec).isEqualTo(fromDB)
    }

    @Test
    fun testUpdate() = runBlocking {
        val queryChannelsSpec = QueryChannelsSpec(data.filter1, QuerySort())
        queryChannelsSpec.cids = listOf("a", "b", "c")
        repo.insert(queryChannelsSpec)
        queryChannelsSpec.cids = listOf("a", "b", "c", "d")
        repo.insert(queryChannelsSpec)

        val fromDB = repo.selectByFilterAndQuerySort(queryChannelsSpec)
        Truth.assertThat(fromDB).isEqualTo(queryChannelsSpec)
    }
}
