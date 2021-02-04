package io.getstream.chat.android.livedata.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.livedata.BaseDomainTest2
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelRepositoryTest : BaseDomainTest2() {
    private val helper by lazy { chatDomainImpl.repos }

    @Test
    fun `inserting a channel and reading it should be equal`() = runBlocking {
        helper.insertChannels(listOf(data.channel1))
        val channel = helper.selectChannelWithoutMessages(data.channel1.cid)!!
        channel.config = data.channel1.config
        channel.watchers = data.channel1.watchers
        channel.watcherCount = data.channel1.watcherCount

        Truth.assertThat(channel).isEqualTo(data.channel1)
    }

    @Test
    fun `deleting a channel should work`() = runBlocking {
        helper.insertChannels(listOf(data.channel1))
        helper.deleteChannel(data.channel1.cid)
        val entity = helper.selectChannelWithoutMessages(data.channel1.cid)

        Truth.assertThat(entity).isNull()
    }

    @Test
    fun `updating a channel should work as intended`() = runBlocking {
        helper.insertChannels(listOf(data.channel1, data.channel1Updated))
        val channel = helper.selectChannelWithoutMessages(data.channel1.cid)!!

        // ignore these 4 fields
        channel.config = data.channel1.config
        channel.createdBy = data.channel1.createdBy
        channel.watchers = data.channel1Updated.watchers
        channel.watcherCount = data.channel1Updated.watcherCount
        Truth.assertThat(channel).isEqualTo(data.channel1Updated)
    }
}
