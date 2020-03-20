package com.getstream.sdk.chat.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.getstream.sdk.chat.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.socket.InitConnectionListener
import io.getstream.chat.android.client.utils.FilterObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.lang.Thread.sleep


@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {

    lateinit var database: ChatDatabase
    lateinit var repo: StreamChatRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        val client = ChatClient.Builder("b67pax5b2wdq", ApplicationProvider.getApplicationContext()).build()

        val user = User("broad-lake-3")
        val token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"

        client.setUser(user, token, object : InitConnectionListener() {

            override fun onSuccess(data: ConnectionData) {
                val user = data.user
                val connectionId = data.connectionId
                System.out.println("user connected")
            }

            override fun onError(error: ChatError) {
                error.printStackTrace()
            }
        })

        database = ChatDatabase.getDatabase(ApplicationProvider.getApplicationContext())
        sleep(2000)
        repo = StreamChatRepository(database.queryChannelsQDao(), database.userDao(), database.reactionDao(), database.messageDao(), database.channelStateDao(), client)
        repo.errorEvents.observeForever( EventObserver {
            System.out.println("error event$it")
        })
        sleep(2000)
    }

    @Test
    fun getMessages() {
        /**
         * TODO:
         * - actually insert the data when you run .watch
         * - test that new message events update the livedata
         */
        val channelRepo = repo.channel("messaging", "test123")
        channelRepo.watch()

        // new messages, reactions, message changes etc are automatically handled
        channelRepo.messages.observeForever {

        }
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
        val channels = repo.queryChannels(query, request)
        // queryRepo exposes the channels livedata object
        channels.observeForever {

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
    fun insertReaction() {
        val r = Reaction("message-123")
        repo.insertReaction(r)
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
    fun runQueryChannelsWhileOffline() {

        repo.setOffline()
        val query = QueryChannelsEntity(
            FilterObject(
                "type",
                "messaging"
            )
        )
        query.sort = QuerySort()
        System.out.println("Start Test")
        val request = QueryChannelsRequest(query.filter, 0, 100, QuerySort()).withMessages(100)
        val channels = repo.queryChannels(query, request)

        System.out.println("Got a value2, yee" + channels.getOrAwaitValue().toString())
    }

}
