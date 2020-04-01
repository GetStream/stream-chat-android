package io.getstream.chat.android.livedata

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
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


/**
 * Verify that all events correctly update state in room
 */
@RunWith(AndroidJUnit4::class)
class ChatRepoEventTest {

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

        repo = StreamChatRepository(client,data.user1, db)
        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
        channelRepo = repo.channel(data.channel1.type, data.channel1.id)
        // TODO: should this be part of the constructor?
        channelRepo.updateChannel(data.channel1)
        repo.insertChannel(data.channel1)
    }

    @After
    fun tearDown() {
        db.close()
        client.disconnect()
    }


    @Test
    fun newMessageEvent() {
        // new messages should be stored in room
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.newMessageEvent)}
        val message = runBlocking(Dispatchers.IO) {
            repo.selectMessageEntity(data.newMessageEvent.message.id)
        }
        Truth.assertThat(message).isNotNull()
    }

    @Test
    fun initializeAndConnect() {
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.connectedEvent)}
        Truth.assertThat(repo.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(repo.online.getOrAwaitValue()).isTrue()
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.disconnectedEvent)}
        Truth.assertThat(repo.initialized.getOrAwaitValue()).isTrue()
        Truth.assertThat(repo.online.getOrAwaitValue()).isFalse()
    }

    @Test
    fun unreadCounts() {
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.connectedEvent2)}
        Truth.assertThat(repo.channelUnreadCount.getOrAwaitValue()).isEqualTo(2)
        Truth.assertThat(repo.totalUnreadCount.getOrAwaitValue()).isEqualTo(3)
    }

    @Test
    fun messageRead() {
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.readEvent)}
        // check channel level read info
        val cid = data.readEvent.cid!!
        val channel = runBlocking(Dispatchers.IO) { repo.selectChannelEntity(cid) }
        Truth.assertThat(channel!!.reads.size).isEqualTo(1)
        val read = channel!!.reads.values.first()
        Truth.assertThat(read.userId).isEqualTo(data.readEvent.user!!.id)
    }

    @Test
    fun reactionEvent() {
        // add the message
        val messageId = data.newMessageEvent.message.id
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.newMessageEvent)}
        // add the reaction
        val secondId = data.reactionEvent.reaction!!.messageId
        Truth.assertThat(secondId).isEqualTo(messageId)
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.reactionEvent)}
        // fetch the message
        var message = runBlocking(Dispatchers.IO) {
            repo.selectMessageEntity(messageId)!!
        }
        // reaction from yourself (so it goes into ownReactions)
        Truth.assertThat(message.reactionCounts.get("like")).isEqualTo(1)
        Truth.assertThat(message.latestReactions.first().userId).isEqualTo(data.reaction1.user!!.id)
        Truth.assertThat(message.ownReactions.first().userId).isEqualTo(data.reaction1.user!!.id)

        // add a reaction from a different user, it should not go into own reaction
        runBlocking(Dispatchers.IO) {repo.handleEvent(data.reactionEvent2)}
        message = runBlocking(Dispatchers.IO) {
            repo.selectMessageEntity(messageId)!!
        }
        Truth.assertThat(message.reactionCounts.get("like")).isEqualTo(2)
        Truth.assertThat(message.latestReactions.size).isEqualTo(2)
        Truth.assertThat(message.ownReactions.size).isEqualTo(1)
    }

    @Test
    fun memberEvent() {

    }

    @Test
    fun channelUpdatedEvent() {

    }





}
