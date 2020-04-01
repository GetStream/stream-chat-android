package io.getstream.chat.android.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.models.QuerySort
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.utils.FilterObject
import io.getstream.chat.android.livedata.entity.QueryChannelsEntity
import io.getstream.chat.android.livedata.utils.TestDataHelper
import io.getstream.chat.android.livedata.utils.TestLoggerHandler
import io.getstream.chat.android.livedata.utils.waitForSetUser
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class IdGenerationTest {
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
        repo = StreamChatRepository(client, data.user1,  db)
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
    fun messageIdGeneration() {
        val messageId = repo.generateMessageId()
        Truth.assertThat(messageId).isNotNull()
        Truth.assertThat(messageId).isNotEmpty()
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
        Truth.assertThat(query2.id).isEqualTo(query.id)
        // verify that 3 and 4 are not equal to 2
        Truth.assertThat(query2.id).isNotEqualTo(query3.id)
        Truth.assertThat(query2.id).isNotEqualTo(query4.id)
    }

}