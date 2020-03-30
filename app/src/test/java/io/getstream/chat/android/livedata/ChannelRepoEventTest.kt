package io.getstream.chat.android.livedata

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.MessageUpdatedEvent
import io.getstream.chat.android.client.events.NewMessageEvent
import io.getstream.chat.android.client.events.UserStartWatchingEvent
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.Watcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatChannelRepoEventTest {

    lateinit var database: ChatDatabase
    lateinit var repo: StreamChatRepository
    lateinit var client: ChatClient

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        Log.i("Hello", "world")
        client = ChatClient.Builder("b67pax5b2wdq", ApplicationProvider.getApplicationContext())
            .logLevel(
                ChatLogLevel.ALL
            ).loggerHandler(TestLoggerHandler()).build()

        val user = User("broad-lake-3")
        val token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiYnJvYWQtbGFrZS0zIn0.SIb263bpikToka22ofV-9AakJhXzfeF8pU9cstvzInE"

        waitForSetUser(client, user, token)
        repo = StreamChatRepository(ApplicationProvider.getApplicationContext(), user.id, client)
        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
    }

    @After
    fun teardown() {
        client.disconnect()
    }

    fun getTestWatchEvent(): UserStartWatchingEvent {
        val event = UserStartWatchingEvent()
        val channel = Channel()
        channel.watcherCount = 100
        val user1 = User("1")
        val watcher = Watcher("whats this?")
        watcher.user = user1
        channel.watchers = listOf(watcher)
        event.channel = channel
        return event
    }

    fun getTestNewMessageEvent(): NewMessageEvent {
        val event = NewMessageEvent()
        val channel = Channel()
        channel.watcherCount = 100
        val user1 = User("1")
        val watcher = Watcher("whats this?")
        watcher.user = user1
        channel.watchers = listOf(watcher)
        event.channel = channel
        val message = Message()
        message.text = "hi there"
        message.id = "message-1"
        message.user = user1
        event.message = message
        return event
    }

    fun getTestUpdatedMessageEvent(): MessageUpdatedEvent {
        val event = MessageUpdatedEvent()
        val channel = Channel()
        channel.watcherCount = 100
        val user1 = User("1")
        val watcher = Watcher("whats this?")
        watcher.user = user1
        channel.watchers = listOf(watcher)
        event.channel = channel
        val message = Message()
        message.text = "my updated message"
        message.id = "message-1"
        message.user = user1
        event.message = message
        return event
    }

    // verify all the events work

    @Test
    fun eventWatcherCountUpdates() {
        val channelRepo = repo.channel("messaging", "test123")
        val event = getTestWatchEvent()
        channelRepo.handleEvent(event)
        Truth.assertThat(channelRepo.watcherCount.getOrAwaitValue()).isEqualTo(100)
        Truth.assertThat(channelRepo.watchers.getOrAwaitValue()).isEqualTo(event.channel?.watchers)
    }

    @Test
    fun eventNewMessage() {
        val channelRepo = repo.channel("messaging", "test123")
        val event = getTestNewMessageEvent()
        channelRepo.handleEvent(event)
        Truth.assertThat(channelRepo.messages.getOrAwaitValue()).isEqualTo(listOf(event.message))
    }

    @Test
    fun eventUpdatedMessage() {
        val channelRepo = repo.channel("messaging", "test123")
        channelRepo.handleEvent(getTestNewMessageEvent())
        val event = getTestUpdatedMessageEvent()
        channelRepo.handleEvent(event)

        val messages = channelRepo.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages).isEqualTo(listOf(event.message))
    }

    // delete message
    @Test
    fun eventDeletedMessage() {

    }

    @Test
    fun eventUpdatedChannel() {

    }

    @Test
    fun eventReaction() {

    }
}