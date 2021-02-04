package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.request.QueryChannelPaginationRequest
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.When
import org.amshove.kluent.calling
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class DisconnectedRecoveryTest : BaseDisconnectedMockedTest() {

    @Test
    fun replayEventsForActiveChannels() = runBlocking {
        // - when you receive a push notification you want to sync all data for the specific channel you received the push on
        // - alternatively we could sync all channels you are interested in
        // - in theory (new channel) you could not be watching the channel yet
        // - your client is typically not connected when running this recover flow
        // - you don't want to watch the channel
        val events = chatDomainImpl.replayEventsForActiveChannels(data.channel1.cid)

        // verify we now have 2 message in offline storage
        val channelState = chatDomainImpl.selectAndEnrichChannel(
            data.channel1.cid,
            QueryChannelPaginationRequest()
        )
        Truth.assertThat(channelState!!.messages.size).isEqualTo(2)
    }
}

@RunWith(AndroidJUnit4::class)
internal class ConnectedRecoveryTest : BaseDomainTest2() {

    @Test
    fun `Active channels should be stored in sync state`(): Unit = runBlocking {
        val cid = "messaging:myspecialchannel"
        chatDomainImpl.channel(cid)
        chatDomainImpl.initJob.await()
        val syncState1 = chatDomainImpl.storeSyncState()
        val syncState2 = chatDomainImpl.repos.selectSyncState(data.user1.id)
        Truth.assertThat(syncState2!!.activeChannelIds).contains(cid)
    }

    @Test
    fun `Connection recovery should not raise an error`() = runBlocking {
        // when the connection is lost and we recover the connection we do the following
        // - query all active channels
        // - repeat all active queries
        // - retry message inserts
        // - replay events
        // - we want to watch channels and enable presence
        chatDomainImpl.connectionRecovered(true)
    }

    @Test
    fun `sync needed is used for our offline to online recovery flow`() = runBlocking {
        data.channel1.syncStatus = SyncStatus.SYNC_NEEDED
        data.channel2.syncStatus = SyncStatus.COMPLETED
        chatDomainImpl.repos.insertChannels(listOf(data.channel1, data.channel2))

        var channels = chatDomainImpl.repos.selectChannelsSyncNeeded()
        Truth.assertThat(channels.size).isEqualTo(1)
        Truth.assertThat(channels.first().syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)

        When calling clientMock.createChannel(any(), any(), any(), any()) doReturn TestCall(Result(data.channel1))
        channels = chatDomainImpl.retryChannels()
        Truth.assertThat(channels.size).isEqualTo(1)
        Truth.assertThat(channels.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        channels = chatDomainImpl.repos.selectChannelsSyncNeeded()
        Truth.assertThat(channels.size).isEqualTo(0)
    }
}
