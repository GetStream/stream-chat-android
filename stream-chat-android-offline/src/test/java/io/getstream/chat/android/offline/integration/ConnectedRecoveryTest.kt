package io.getstream.chat.android.offline.integration

// Todo: Move those tests to correct place
// @RunWith(AndroidJUnit4::class)
// internal class ConnectedRecoveryTest : BaseDomainTest2() {
//
//     @Test
//     fun `Active channels should be stored in sync state`(): Unit = runBlocking {
//         val cid = "messaging:myspecialchannel"
//         chatDomainImpl.channel(cid)
//         chatDomainImpl.initJob?.await()
//         val syncState1 = chatDomainImpl.storeSyncState()
//         val syncState2 = repos.selectSyncState(data.user1.id)
//
//         syncState2.shouldNotBeNull()
//         syncState2.activeChannelIds shouldContain cid
//     }
//
//     @Test
//     fun `Connection recovery should not raise an error`(): Unit = runBlocking {
//         // when the connection is lost and we recover the connection we do the following
//         // - query all active channels
//         // - repeat all active queries
//         // - retry message inserts
//         // - replay events
//         // - we want to watch channels and enable presence
//         chatDomainImpl.connectionRecovered(true)
//     }
//
//     @Test
//     fun `sync needed is used for our offline to online recovery flow`(): Unit = runBlocking {
//         data.channel1.syncStatus = SyncStatus.SYNC_NEEDED
//         data.channel2.syncStatus = SyncStatus.COMPLETED
//         chatDomainImpl.repos.insertChannels(listOf(data.channel1, data.channel2))
//
//         var channels = chatDomainImpl.repos.selectChannelsSyncNeeded()
//         channels.size shouldBeEqualTo 1
//         channels.first().syncStatus shouldBeEqualTo SyncStatus.SYNC_NEEDED
//
//         whenever(clientMock.createChannel(any(), any(), any(), any())) doReturn TestCall(Result(data.channel1))
//         channels = chatDomainImpl.retryChannels()
//         channels.size shouldBeEqualTo 1
//         channels.first().syncStatus shouldBeEqualTo SyncStatus.COMPLETED
//
//         channels = chatDomainImpl.repos.selectChannelsSyncNeeded()
//         channels.size shouldBeEqualTo 0
//     }
// }
