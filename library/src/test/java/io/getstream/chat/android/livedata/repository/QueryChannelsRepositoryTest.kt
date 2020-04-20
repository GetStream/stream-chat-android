package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class QueryChannelsRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomain.repos.queryChannels }

    @Test
    fun testInsertAndRead() = runBlocking(Dispatchers.IO) {
        val queryChannelsEntity = QueryChannelsEntity(data.filter1, null)
        queryChannelsEntity.channelCIDs = sortedSetOf("a", "b", "c")
        repo.insert(queryChannelsEntity)
        val entity = repo.select(queryChannelsEntity.id)
        Truth.assertThat(queryChannelsEntity).isEqualTo(entity)
    }


    @Test
    fun testUpdate() = runBlocking(Dispatchers.IO) {
        val queryChannelsEntity = QueryChannelsEntity(data.filter1, null)
        queryChannelsEntity.channelCIDs = sortedSetOf("a", "b", "c")
        repo.insert(queryChannelsEntity)
        queryChannelsEntity.channelCIDs = sortedSetOf("a", "b", "c", "d")
        repo.insert(queryChannelsEntity)

        val entity = repo.select(queryChannelsEntity.id)
        Truth.assertThat(entity).isEqualTo(queryChannelsEntity)


    }


}