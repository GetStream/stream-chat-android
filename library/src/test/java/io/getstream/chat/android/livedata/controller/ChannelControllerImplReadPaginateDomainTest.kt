package io.getstream.chat.android.livedata.controller

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.api.models.Pagination
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.BaseConnectedIntegrationTest
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChannelControllerImplReadPaginateDomainTest : BaseConnectedIntegrationTest() {

    /**
     * test that a message added only to the local storage is picked up
     */
    @Test
    fun watchSetsMessagesAndChannelOffline() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.setOffline()
        // add a message to local storage
        chatDomainImpl.repos.users.insertUser(data.user1)
        chatDomainImpl.repos.channels.insertChannel(data.channel1)
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
    fun watchSetsMessagesAndChannelOnline() = runBlocking(Dispatchers.IO) {
        chatDomainImpl.setOnline()
        // setup an online message
        val message = Message()
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
    fun recovery() = runBlocking(Dispatchers.IO) {
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

    @Test
    fun loadOlderMessages() = runBlocking(Dispatchers.IO) {
        val channelRepo = chatDomainImpl.channel("messaging", "testabc")
        Truth.assertThat(channelRepo.loading.getOrAwaitValue()).isFalse()
        channelRepo.upsertMessages(listOf(data.message1, data.message2Older))
        // verify we sort correctly
        val messages = channelRepo.sortedMessages()
        Truth.assertThat(messages[0].createdAt!!.before(messages[1].createdAt)).isTrue()
        // verify we generate the right request
        val request = channelRepo.loadMoreMessagesRequest(10, Pagination.LESS_THAN)
        // message 2 is older, we should use message 2 for getting older messages
        Truth.assertThat(request.messageFilterValue).isEqualTo(data.message2Older.id)
        // verify that running the query doesn't error
        val result = channelRepo.runChannelQueryOnline(request)
    }
}
