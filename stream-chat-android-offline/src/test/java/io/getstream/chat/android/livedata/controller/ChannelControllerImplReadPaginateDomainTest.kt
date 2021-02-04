package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.test.getOrAwaitValue
import kotlinx.coroutines.runBlocking
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ChannelControllerImplReadPaginateDomainTest : BaseConnectedIntegrationTest() {

    /**
     * test that a message added only to the local storage is picked up
     */
    @Test
    fun watchSetsMessagesAndChannelOffline() = runBlocking {
        chatDomainImpl.setOffline()
        // add a message to local storage
        chatDomainImpl.repos.insertUser(data.user1)
        chatDomainImpl.repos.insertChannel(data.channel1)
        val message1 = data.createMessage()
        channelControllerImpl.sendMessage(message1)
        // remove the livedata
        channelControllerImpl =
            ChannelControllerImpl(
                data.channel1.type,
                data.channel1.id,
                chatDomainImpl.client,
                chatDomainImpl
            )

        // run watch while we're offline
        channelControllerImpl.watch()

        // the message should still show up
        val messages = channelControllerImpl.messages.getOrAwaitValue()
        val channelData = channelControllerImpl.channelData.getOrAwaitValue()
        val channel = channelControllerImpl.toChannel()

        Truth.assertThat(messages).isNotEmpty()
        Truth.assertThat(channelData).isNotNull()
        Truth.assertThat(channel.config).isNotNull()
    }

    /**
     * test that a message added only to the local storage is picked up
     */
    @Test
    fun watchSetsMessagesAndChannelOnline() = runBlocking {
        chatDomainImpl.setOnline()
        // setup an online message
        val message = data.createMessage()
        message.syncStatus = SyncStatus.SYNC_NEEDED
        // write a message
        channelControllerImpl.sendMessage(message)

        val messages = channelControllerImpl.messages.getOrAwaitValue()
        val channel = channelControllerImpl.toChannel()

        Truth.assertThat(messages.size).isGreaterThan(0)
        Truth.assertThat(messages.first().id).isEqualTo(message.id)
        Truth.assertThat(channel).isNotNull()
        Truth.assertThat(channel.config).isNotNull()
    }

    @Test
    @Ignore("mock me")
    fun recovery() = runBlocking {
        // running recover should trigger channels to show up for active queries and channels
        chatDomainImpl.connectionRecovered(true)

        // verify channel data is loaded
        val channelRepos = queryControllerImpl.channels.getOrAwaitValue()
        Truth.assertThat(channelRepos.size).isGreaterThan(0)

        // verify we have messages as well
        val channelId = channelRepos.first().cid

        val messages = chatDomainImpl.channel(channelId).messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isGreaterThan(0)
    }
}
