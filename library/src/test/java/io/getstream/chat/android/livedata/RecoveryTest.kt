package io.getstream.chat.android.livedata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class RecoveryTest : BaseConnectedIntegrationTest() {

    @Test
    fun replayEventsForActiveChannels() = runBlocking {
        // - when you receive a push notification you want to sync all data for the specific channel you received the push on
        // - alternatively we could sync all channels you are interested in
        // - in theory (new channel) you could not be watching the channel yet
        // - your client is typically not connected when running this recover flow
        // - you don't want to watch the channel
        val cid = "messaging:123"
        val events = chatDomainImpl.replayEventsForActiveChannels(cid)
    }

    @Test
    fun storeSyncState() = runBlocking(Dispatchers.IO) {
        val cid = "messaging:myspecialchannel"
        chatDomainImpl.channel(cid)
        chatDomainImpl.initJob.await()
        val syncState1 = chatDomainImpl.storeSyncState()
        val syncState2 = chatDomainImpl.repos.syncState.select(data.user1.id)
        Truth.assertThat(syncState2!!.activeChannelIds).contains(cid)
    }

    @Test
    fun connectionRecovered() = runBlocking(Dispatchers.IO) {
        // when the connection is lost and we recover the connection we do the following
        // - query all active channels
        // - repeat all active queries
        // - retry message inserts
        // - replay events
        // - we want to watch channels and enable presence
        chatDomainImpl.connectionRecovered(true)
    }
}
