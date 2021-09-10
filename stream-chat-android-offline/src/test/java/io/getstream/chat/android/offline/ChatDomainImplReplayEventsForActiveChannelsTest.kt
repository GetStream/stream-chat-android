package io.getstream.chat.android.offline

import android.content.Context
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.offline.event.EventHandlerImpl
import io.getstream.chat.android.test.TestCall
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class ChatDomainImplReplayEventsForActiveChannelsTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `when replaying events for active channels should add channel to active channels`() =
        testCoroutines.scope.runBlockingTest {
            val cid = "ChannelType:ChannelId"
            val sut = Fixture(testCoroutines.scope).givenSyncHistoryResult(Result(emptyList())).get()

            sut.replayEvents(cid)

            sut.isActiveChannel(cid).shouldBeTrue()
        }

    @Test
    fun `when replaying events for active channels should get sync history for active channels`() =
        testCoroutines.scope.runBlockingTest {
            val cid = "ChannelType:ChannelId"
            val chatClient: ChatClient = mock {
                on(it.channel(any())) doReturn mock()
            }
            val sut = Fixture(testCoroutines.scope)
                .givenChatClient(chatClient)
                .givenSyncHistoryResult(Result(emptyList()))
                .get()

            sut.replayEvents(cid)

            verify(chatClient).getSyncHistory(argThat { contains(cid) }, any())
        }

    @Test
    fun `when replaying events for active channels should update offline storage from events`() =
        testCoroutines.scope.runBlockingTest {
            val cid = "ChannelType:ChannelId"
            val events = listOf<ChatEvent>(
                randomChannelUpdatedEvent(),
                randomNewMessageEvent(),
                randomNewMessageEvent(),
                randomChannelUpdatedEvent(),
                randomNewMessageEvent(),
            )
            val eventHandlerImpl: EventHandlerImpl = mock()
            val sut = Fixture(testCoroutines.scope)
                .givenEventHandlerImpl(eventHandlerImpl)
                .givenSyncHistoryResult(Result(events))
                .get()

            sut.replayEvents(cid)

            verify(eventHandlerImpl).handleEventsInternal(events)
        }

    private class Fixture(private val coroutineScope: CoroutineScope) {
        private val context: Context = mock()
        private var chatClient: ChatClient = mock {
            on(it.channel(any())) doReturn mock()
        }
        private var eventHandlerImpl: EventHandlerImpl = mock()

        fun givenChatClient(chatClient: ChatClient): Fixture {
            this.chatClient = chatClient

            return this
        }

        fun givenSyncHistoryResult(result: Result<List<ChatEvent>>): Fixture {
            whenever(chatClient.getSyncHistory(any(), any())).doAnswer {
                TestCall(result)
            }
            return this
        }

        fun givenEventHandlerImpl(eventHandlerImpl: EventHandlerImpl): Fixture {
            this.eventHandlerImpl = eventHandlerImpl
            return this
        }

        fun get(): ChatDomainImpl {
            return ChatDomain.Builder(context, chatClient).buildImpl().apply {
                scope = coroutineScope
                eventHandler = eventHandlerImpl
            }
        }
    }
}
