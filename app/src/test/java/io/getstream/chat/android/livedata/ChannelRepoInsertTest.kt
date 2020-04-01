package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import io.getstream.chat.android.livedata.utils.waitForSetUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep

@RunWith(AndroidJUnit4::class)
class ChannelRepoInsertTest {

    lateinit var database: ChatDatabase
    lateinit var repo: ChatRepo
    lateinit var client: ChatClient
    lateinit var data: TestDataHelper
    lateinit var channelRepo: ChannelRepo
    lateinit var db: ChatDatabase

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        client = ChatClient.Builder("b67pax5b2wdq", ApplicationProvider.getApplicationContext())
            .logLevel(
                ChatLogLevel.ALL
            ).loggerHandler(TestLoggerHandler()).build()

        val user = User("broad-lake-3")
        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"

        waitForSetUser(
            client,
            user,
            token
        )
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), ChatDatabase::class.java).build()
        data = TestDataHelper()

        repo = ChatRepo(client, data.user1, db)
        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
        channelRepo = repo.channel(data.channel1.type, data.channel1.id)
        // TODO: should this be part of the constructor?
        channelRepo.updateChannel(data.channel1)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }

    @Test
    fun sendMessage() {
        // send the message while offline
        repo.setOffline()
        runBlocking { repo.insertChannel(data.channel1) }
        channelRepo.sendMessage(data.message1)
        // get the message and channel state both live and offline versions
        sleep(100)
        var roomChannel = runBlocking(Dispatchers.IO) { repo.selectChannelEntity(data.message1.channel.cid) }
        var liveChannel = channelRepo.channel.getOrAwaitValue()
        var roomMessages = runBlocking(Dispatchers.IO) { repo.selectMessagesForChannel(data.message1.channel.cid) }
        var liveMessages = channelRepo.messages.getOrAwaitValue()

        Truth.assertThat(liveMessages.size).isEqualTo(1)
        Truth.assertThat(liveMessages[0].syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        Truth.assertThat(roomMessages.size).isEqualTo(1)
        Truth.assertThat(roomMessages[0].syncStatus).isEqualTo(SyncStatus.SYNC_NEEDED)
        // verify the message is stored in room, and set to retry
        // verify the channel is updated as well (lastMessage at and lastMessageAt)
        Truth.assertThat(liveChannel.lastMessageAt).isEqualTo(data.message1.createdAt)
        Truth.assertThat(roomChannel!!.lastMessageAt).isEqualTo(data.message1.createdAt)

        var messageEntities = runBlocking(Dispatchers.IO) { repo.retryMessages() }
        Truth.assertThat(messageEntities.size).isEqualTo(1)

        // now we go online and retry, after the retry all state should be updated
        repo.setOnline()
        messageEntities = runBlocking(Dispatchers.IO) { repo.retryMessages() }
        Truth.assertThat(messageEntities.size).isEqualTo(1)

        roomMessages = runBlocking(Dispatchers.IO) { repo.selectMessagesForChannel(data.message1.channel.cid) }
        liveMessages = channelRepo.messages.getOrAwaitValue()
        Truth.assertThat(liveMessages[0].syncStatus).isEqualTo(SyncStatus.SYNCED)
        Truth.assertThat(roomMessages[0].syncStatus).isEqualTo(SyncStatus.SYNCED)

    }

    // TODO: Test sendReaction
    // TODO: test markRead
    // TODO: Test recover/resend



}