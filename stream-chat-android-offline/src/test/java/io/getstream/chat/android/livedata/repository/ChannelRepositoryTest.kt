package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest2
import io.getstream.chat.android.livedata.repository.mapper.toModel
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelRepositoryTest : BaseDomainTest2() {
    val repo by lazy { chatDomainImpl.repos.channels }

    @Test
    fun `inserting a channel and reading it should be equal`() = runBlocking {
        repo.insertChannels(listOf(data.channel1))
        val entity = repo.select(data.channel1.cid)
        val channel = entity!!.toModel(getUser = { data.userMap[it]!! }, getMessage = { null })
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
        val channel = entity!!.toModel(getUser = { data.userMap[it]!! }, getMessage = { null })

        // ignore these 4 fields
        channel.config = data.channel1.config
        channel.createdBy = data.channel1.createdBy
        channel.watchers = data.channel1Updated.watchers
        channel.watcherCount = data.channel1Updated.watcherCount
        Truth.assertThat(channel).isEqualTo(data.channel1Updated)
    }
}
