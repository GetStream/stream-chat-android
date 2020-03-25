package com.getstream.sdk.chat.livedata

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.getstream.sdk.chat.livedata.entity.QueryChannelsEntity
import com.google.common.truth.Truth.assertThat
import io.getstream.chat.android.client.BuildConfig
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.logger.ChatLoggerHandler
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.utils.FilterObject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep
import java.util.*


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
        repo.errorEvents.observeForever( EventObserver {
            System.out.println("error event$it")
        })
    }

    @After
    fun teardown() {
        //client.disconnect()
    }

    @Test
    fun getMessages() {
        val channelRepo = repo.channel("messaging", "test123")
        channelRepo.watch()


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
        // TODO: test reads as well as inserts
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
