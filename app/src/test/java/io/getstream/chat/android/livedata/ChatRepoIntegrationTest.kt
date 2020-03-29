package io.getstream.chat.android.livedata

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.ChatSocket
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.client.utils.SyncStatus
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.shadows.ShadowLooper
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
class ChatRepoIntegrationTest {

    lateinit var database: ChatDatabase
    lateinit var repo: StreamChatRepository
    lateinit var client: ChatClient

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Log.i("Hello", "world")
        client = ChatClient.Builder("b67pax5b2wdq", ApplicationProvider.getApplicationContext()).logLevel(
            ChatLogLevel.ALL).loggerHandler(TestLoggerHandler()).build()

        val user = User("broad-lake-3")
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"

        waitForSetUser(client, user, token)
        repo = StreamChatRepository(ApplicationProvider.getApplicationContext(),user.id, client)
        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
    }

    @After
    fun teardown() {
        //client.disconnect()
    }

    @Test
    fun watchSetsMessagesAndChannel() {
        val channelRepo = repo.channel("messaging", "test123")
        channelRepo.watch()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val messages = channelRepo.messages.getOrAwaitValue()
        val channel = channelRepo.channel.getOrAwaitValue()
        sleep(1000)

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertThat(messages).isNotNull()
        assertThat(channel).isNotNull()

        // TODO:

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

        repo.insertMessage(message)
        sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        channelRepo.watch()
        sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val messages = channelRepo.messages.getOrAwaitValue()
        val channel = channelRepo.channel.getOrAwaitValue()

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertThat(messages).isNotEmpty()
        assertThat(channel).isNotNull()
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
        sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()

        val messages = channelRepo.messages.getOrAwaitValue()
        val channel = channelRepo.channel.getOrAwaitValue()

        sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        assertThat(messages.size).isGreaterThan(0)
        assertThat(channel).isNotNull()
    }

    @Test
    fun watchSetsMessagesAndChannelEvent() {
        // start out empty, no offline storage and no query
        val channelRepo = repo.channel("messaging", "watchSetsMessagesAndChannelEvent")
        channelRepo.channelController.watch().execute()
        // a new message event is triggered
        // TODO: how to mock the events...
        val socket = Mockito.mock(ChatSocket::class.java)
        //Mockito.`when`(socket.events()).thenReturn(JustObservable(connectedEvent))


        // verify that the livedata is updated

    }

    @Test
    fun watchSetsMessagesAndChannelEvent2() {
        // TODO: test that a user event is also propagated on members, messages, reaction etc.
        // start out empty, no offline storage and no query
        val channelRepo = repo.channel("messaging", "watchSetsMessagesAndChannelEvent")
        channelRepo.channelController.watch().execute()
        // a new message event is triggered
        // TODO: how to mock the events...
        val socket = Mockito.mock(ChatSocket::class.java)
        //Mockito.`when`(socket.events()).thenReturn(JustObservable(connectedEvent))


        // verify that the livedata is updated

    }



    @Test
    fun getMessages() {
        // watch sets the channel data
        // watch sets the messages

        val channelRepo = repo.channel("messaging", "test123")
        channelRepo.watch()
        val message = Message()
        message.text = "hello world"
        message.user = User("thierry")
        sleep(1000)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        channelRepo.sendMessage(message)
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        val newMessages = channelRepo.messages.getOrAwaitValue()
        ShadowLooper.runUiThreadTasksIncludingDelayedTasks()
        System.out.println("newMessages $newMessages")

    }

    @Test
    fun sendMessage() {
        /**
         * TODO:
         * - actually insert the data when you run .watch
         * - test that new message events update the livedata
         */
        val channelRepo = repo.channel("messaging", "test123")
        channelRepo.watch()
        val message = Message()
        message.text = "Hello world"
        message.user = User("Jack")


        // new messages, reactions, message changes etc are automatically handled
        channelRepo.messages.observeForever {

        }

        channelRepo.sendMessage(message)
        sleep(1000)

    }

    @Test
    fun getChannels() {
        val query = QueryChannelsEntity(
            FilterObject(
                "type",
                "messaging"
            )
        )
        query.sort = QuerySort()
        val request = QueryChannelsRequest(query.filter, 0, 100, QuerySort()).withMessages(100)

        // runs the query and returns a queryRepo helper object
        val queryRepo = repo.queryChannels(query)
        queryRepo.query(request)
        // queryRepo exposes the channels livedata object
        queryRepo.channels.observeForever {

        }

        sleep(1000)

    }



    @Test
    fun runInsertUser() {
        val u = User("mr-tester")
        repo.insertUser(u)
    }

    @Test
    fun insertQuery() {
        val q = QueryChannelsEntity(FilterObject(
            "type",
            "messaging"
        ))
        repo.insertQuery(q)
    }

    @Test
    fun insertMessage() {
        val m = Message()
        m.user = User("jack")
        m.text = "hello world"
        repo.insertMessage(m)
    }

    @Test
    fun insertChannel() {
        val m = Message()
        m.user = User("jack")
        m.text = "hello world"
        val c = Channel()
        c.id = "123"
        c.type = "messaging"
        c.messages = listOf<Message>(m)

        repo.insertChannel(c)
    }
    @Test
    fun messageIdGeneration() {
        val messageId = repo.generateMessageId()
        assertThat(messageId).isNotNull()
        assertThat(messageId).isNotEmpty()
    }

    @Test
    fun queryId() {
        val query = QueryChannelsEntity(
            FilterObject(
                "type",
                "messaging"
            ), QuerySort()
        )
        val query2 = QueryChannelsEntity(
            FilterObject(
                "type",
                "messaging"
            ), QuerySort()
        )
        val query3 = QueryChannelsEntity(
            FilterObject(
                "type",
                "commerce"
            ), QuerySort()
        )
        val query4 = QueryChannelsEntity(
            FilterObject(
                "type",
                "messaging"
            ), QuerySort().asc("name")
        )
        // verify that 1 and 2 are equal
        assertThat(query2.id).isEqualTo(query.id)
        // verify that 3 and 4 are not equal to 2
        assertThat(query2.id).isNotEqualTo(query3.id)
        assertThat(query2.id).isNotEqualTo(query4.id)
    }



}
