package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest
import io.getstream.chat.android.livedata.entity.SyncStateEntity
import io.getstream.chat.android.livedata.utils.calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class SyncStateRepositoryTest : BaseDomainTest() {
    val repo by lazy { chatDomainImpl.repos.syncState }

    @Test
    fun testInsertAndRead() = runBlocking(Dispatchers.IO) {
        val syncState = SyncStateEntity(data.user1.id, listOf(data.channel1.id), listOf(data.query1.id), calendar(2020, 2, 2))
        repo.insert(syncState)
        val syncStateFound = repo.select(data.user1.id)
        Truth.assertThat(syncStateFound).isEqualTo(syncState)
    }
}
