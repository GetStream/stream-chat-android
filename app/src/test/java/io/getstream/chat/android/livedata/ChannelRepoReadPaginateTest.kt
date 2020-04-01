package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.getOrAwaitValue
import io.getstream.chat.android.livedata.utils.waitForSetUser
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.robolectric.shadows.ShadowLooper

class ChannelRepoReadPaginateTest {
    lateinit var database: ChatDatabase
    lateinit var repo: StreamChatRepository
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

        repo = StreamChatRepository(client,data.user1,  db)
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

    /**
     * test that a message added only to the local storage is picked up
     */
    @Test
    fun watchSetsMessagesAndChannelOffline() {
        repo.setOffline()
        val channelRepo = repo.channel("messaging", "test123")
        // setup an offline message
        val message = Message()
        message.user = User("thierry")
        message.cid = channelRepo.cid
        message.syncStatus = SyncStatus.SYNC_NEEDED
        val c = Channel()
        c.type = "messaging"
        c.id = "test123"
        c.cid = "${c.type}:${c.id}"
        c.createdBy = User("john")
        repo.insertUser(c.createdBy)
        repo.insertUser(message.user)
        repo.insertChannel(c)

        //repo.insertMessage(message)
        Thread.sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        channelRepo.watch()
        Thread.sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val messages = channelRepo.messages.getOrAwaitValue()
        val channel = channelRepo.channel.getOrAwaitValue()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        Truth.assertThat(messages).isNotEmpty()
        Truth.assertThat(channel).isNotNull()
    }

    /**
     * test that a message added only to the local storage is picked up
     */
    @Test
    fun watchSetsMessagesAndChannelOnline() {
        repo.setOnline()
        val channelRepo = repo.channel("messaging", "testabc")
        // setup an online message
        val message = Message()
        message.cid = channelRepo.cid
        message.syncStatus = SyncStatus.SYNC_NEEDED
        // create the channel
        channelRepo.channelController.watch().execute()
        // write a message
        val result = channelRepo.channelController.sendMessage(message).execute()

        channelRepo.watch()
        Thread.sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val messages = channelRepo.messages.getOrAwaitValue()
        val channel = channelRepo.channel.getOrAwaitValue()

        Thread.sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        Truth.assertThat(messages.size).isGreaterThan(0)
        Truth.assertThat(channel).isNotNull()
    }


}