package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class QueryChannelsImplRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomainImpl.repos.queryChannels }

    @Test
    fun testInsertAndRead() = runBlocking(Dispatchers.IO) {
        val queryChannelsEntity = QueryChannelsEntity(data.filter1, QuerySort())
        queryChannelsEntity.channelCids = listOf("a", "b", "c")
        repo.insert(queryChannelsEntity)
        val entity = repo.select(queryChannelsEntity.id)
        Truth.assertThat(queryChannelsEntity).isEqualTo(entity)
    }

    @Test
    fun testUpdate() = runBlocking(Dispatchers.IO) {
        val queryChannelsEntity = QueryChannelsEntity(data.filter1, QuerySort())
        queryChannelsEntity.channelCids = listOf("a", "b", "c")
        repo.insert(queryChannelsEntity)
        queryChannelsEntity.channelCids = listOf("a", "b", "c", "d")
        repo.insert(queryChannelsEntity)

        val entity = repo.select(queryChannelsEntity.id)
        Truth.assertThat(entity).isEqualTo(queryChannelsEntity)
    }
}
