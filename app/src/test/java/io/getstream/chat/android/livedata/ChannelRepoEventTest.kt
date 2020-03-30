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
import io.getstream.chat.android.client.models.*
import org.junit.*
import org.junit.runner.RunWith

/**
 * This test suite verifies that ChatChannelRepo implements event handling correctly and updates it's local state
 * Offline storage for these events is handled at the ChannelRepo level, so this is only testing local state changes
 * Note that we don't rely on Room's livedata mechanism as this library needs to work without room enabled as well
 */
@RunWith(AndroidJUnit4::class)
class ChatChannelRepoEventTest {

    lateinit var database: ChatDatabase
    lateinit var repo: StreamChatRepository
    lateinit var client: ChatClient
    lateinit var data: TestDataHelper
    lateinit var channelRepo: ChatChannelRepo

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

        waitForSetUser(client, user, token)
        repo = StreamChatRepository(ApplicationProvider.getApplicationContext(), user.id, client)
        repo.errorEvents.observeForever(io.getstream.chat.android.livedata.EventObserver {
            System.out.println("error event$it")
        })
        data = TestDataHelper()
        channelRepo = repo.channel("messaging", "test123")
    }

    @After
    fun teardown() {
        client.disconnect()
    }

    @Test
    fun eventWatcherCountUpdates() {
        val event = data.userStartWatchingEvent
        channelRepo.handleEvent(event)
        Truth.assertThat(channelRepo.watcherCount.getOrAwaitValue()).isEqualTo(100)
        Truth.assertThat(channelRepo.watchers.getOrAwaitValue()).isEqualTo(event.channel?.watchers)
    }

    @Test
    fun eventNewMessage() {
        channelRepo.handleEvent(data.newMessageEvent)
        Truth.assertThat(channelRepo.messages.getOrAwaitValue()).isEqualTo(listOf(data.newMessageEvent.message))
    }

    @Test
    fun eventUpdatedMessage() {

        channelRepo.handleEvent(data.newMessageEvent)
        val event = data.messageUpdatedEvent
        channelRepo.handleEvent(event)

        val messages = channelRepo.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages).isEqualTo(listOf(event.message))
    }

    @Test
    fun userUpdatedEvent() {
        channelRepo.handleEvent(data.newMessageEvent)
        // TODO this event is missing in LLC
    }

    // TODO: Member updates

    @Test
    fun typingEvents() {
        channelRepo.handleEvent(data.user1TypingStarted)
        channelRepo.handleEvent(data.user2TypingStarted)
        channelRepo.handleEvent(data.user1TypingStop)
        val typing = channelRepo.typing.getOrAwaitValue()
        Truth.assertThat(typing).isEqualTo(listOf(data.user2))
    }

    @Test
    fun readEvents() {
        channelRepo.handleEvent(data.user1Read)
        val reads = channelRepo.reads.getOrAwaitValue()
        Truth.assertThat(reads.size).isEqualTo(1)
        Truth.assertThat(reads[0].user.id).isEqualTo(data.user1.id)
    }

    @Test
    fun eventMessageWithThread() {
        // TODO: improve thread handling
        channelRepo.handleEvent(data.newMessageEvent)
        channelRepo.handleEvent(data.newMessageWithThreadEvent)
        val parentId = data.newMessageWithThreadEvent.message.parentId
        val threads = channelRepo.threads.getOrAwaitValue()
        val messages = threads[data.newMessageWithThreadEvent.message.parentId]
        Truth.assertThat(messages?.size).isEqualTo(2)
    }

    @Test
    fun eventUpdatedChannel() {
        channelRepo.handleEvent(data.channelUpdatedEvent)
        val channel = channelRepo.channel.getOrAwaitValue()
        Truth.assertThat(channel.extraData.get("color")).isEqualTo("green")
    }

    @Test
    fun eventReaction() {
        channelRepo.handleEvent(data.reactionEvent)
        val messages = channelRepo.messages.getOrAwaitValue()
        Truth.assertThat(messages.size).isEqualTo(1)
        Truth.assertThat(messages[0]).isEqualTo(data.reactionEvent.message)
    }
}