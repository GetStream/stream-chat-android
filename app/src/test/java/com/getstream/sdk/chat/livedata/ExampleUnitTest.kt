package com.getstream.sdk.chat.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.getstream.sdk.chat.livedata.entity.ChannelQuery
import io.getstream.chat.android.client.api.models.QueryChannelsRequest
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.FilterObject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class ExampleUnitTest {

    lateinit var database: ChatDatabase
    lateinit var repo: StreamChatRepository

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        database = ChatDatabase.getDatabase(ApplicationProvider.getApplicationContext())
        repo = StreamChatRepository(database.queryChannelsQDao(), database.userDao(), database.reactionDao())
    }

    @Test
    fun runInsertUser() {
        val u = User("mr-tester")
        repo.insertUser(u)
    }

    @Test
    fun insertReaction() {
        val r = Reaction("message-123")
        repo.insertReaction(r)
    }

    @Test
    fun insertQuery() {
        val q = ChannelQuery(FilterObject(
            "type",
            "messaging"
        ))
        repo.insertQuery(q)
    }

    @Test
    fun insertMessage() {
        val m = Message()
        m.text = "hello world"
        repo.insertMessage(m)
    }


    @Test
    fun runQueryChannelsWhileOffline() {

        repo.setOffline()
        val query = ChannelQuery(
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
