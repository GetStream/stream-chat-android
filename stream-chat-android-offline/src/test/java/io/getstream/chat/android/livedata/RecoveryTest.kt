package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ConnectedRecoveryTest : BaseDomainTest2() {

    @Test
    fun `Active channels should be stored in sync state`(): Unit = runBlocking {
        val cid = "messaging:myspecialchannel"
        chatDomainImpl.channel(cid)
        chatDomainImpl.initJob?.await()
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

        whenever(clientMock.createChannel(any(), any(), any(), any())) doReturn TestCall(Result(data.channel1))
        channels = chatDomainImpl.retryChannels()
        Truth.assertThat(channels.size).isEqualTo(1)
        Truth.assertThat(channels.first().syncStatus).isEqualTo(SyncStatus.COMPLETED)

        channels = chatDomainImpl.repos.selectChannelsSyncNeeded()
        Truth.assertThat(channels.size).isEqualTo(0)
    }
}
