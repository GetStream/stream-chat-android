package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.offline.integration.BaseDomainTest2
import io.getstream.chat.android.test.TestCall
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldNotBeNull
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

        syncState2.shouldNotBeNull()
        syncState2.activeChannelIds shouldContain cid
    }

    @Test
    fun `Connection recovery should not raise an error`(): Unit = runBlocking {
        // when the connection is lost and we recover the connection we do the following
        // - query all active channels
        // - repeat all active queries
        // - retry message inserts
        // - replay events
        // - we want to watch channels and enable presence
        chatDomainImpl.connectionRecovered(true)
    }

    @Test
    fun `sync needed is used for our offline to online recovery flow`(): Unit = runBlocking {
        data.channel1.syncStatus = SyncStatus.SYNC_NEEDED
        data.channel2.syncStatus = SyncStatus.COMPLETED
        chatDomainImpl.repos.insertChannels(listOf(data.channel1, data.channel2))

        var channels = chatDomainImpl.repos.selectChannelsSyncNeeded()
        channels.size shouldBeEqualTo 1
        channels.first().syncStatus shouldBeEqualTo SyncStatus.SYNC_NEEDED

        whenever(clientMock.createChannel(any(), any(), any(), any())) doReturn TestCall(Result(data.channel1))
        channels = chatDomainImpl.retryChannels()
        channels.size shouldBeEqualTo 1
        channels.first().syncStatus shouldBeEqualTo SyncStatus.COMPLETED

        channels = chatDomainImpl.repos.selectChannelsSyncNeeded()
        channels.size shouldBeEqualTo 0
    }
}
