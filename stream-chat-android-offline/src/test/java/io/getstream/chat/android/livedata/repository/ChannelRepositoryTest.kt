package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelRepositoryTest : BaseDomainTest2() {
    val repo by lazy { chatDomainImpl.repos.channels }

    @Test
    fun `inserting a channel and reading it should be equal`() = runBlocking {
        repo.insertChannels(listOf(data.channel1))
        val entity = repo.select(data.channel1.cid)
        val channel = entity!!.toModel({ data.userMap[it]!! }) { null }
        channel.config = data.channel1.config
        channel.watchers = data.channel1.watchers
        channel.watcherCount = data.channel1.watcherCount

        Truth.assertThat(channel).isEqualTo(data.channel1)
    }

    @Test
    fun `deleting a channel should work`() = runBlocking {
        repo.insertChannels(listOf(data.channel1))
        repo.delete(data.channel1.cid)
        val entity = repo.select(data.channel1.cid)

        Truth.assertThat(entity).isNull()
    }

    @Test
    fun `updating a channel should work as intended`() = runBlocking {
        repo.insertChannels(listOf(data.channel1, data.channel1Updated))
        val entity = repo.select(data.channel1.cid)
        val channel = entity!!.toModel({ data.userMap[it]!! }) { null }

        // ignore these 4 fields
        channel.config = data.channel1.config
        channel.createdBy = data.channel1.createdBy
        channel.watchers = data.channel1Updated.watchers
        channel.watcherCount = data.channel1Updated.watcherCount
        Truth.assertThat(channel).isEqualTo(data.channel1Updated)
    }

    @Test
    fun `sync needed is used for our offline to online recovery flow`() = runBlocking {
        data.channel1.syncStatus = SyncStatus.SYNC_NEEDED
        data.channel2.syncStatus = SyncStatus.COMPLETED

        repo.insertChannels(listOf(data.channel1, data.channel2))

        var channels = repo.selectSyncNeeded()
        Truth.assertThat(channels.size).isEqualTo(1)
        Truth.assertThat(channels.first().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)

        When calling clientMock.createChannel(any(), any(), any(), any()) doReturn TestCall(Result(data.channel1))
        channels = repo.retryChannels()
        Truth.assertThat(channels.size).isEqualTo(1)
        Truth.assertThat(channels.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        channels = repo.selectSyncNeeded()
        Truth.assertThat(channels.size).isEqualTo(0)
    }
}
