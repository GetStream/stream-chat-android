package io.getstream.chat.android.offline.events

import com.nhaarman.mockitokotlin2.mock
import io.getstream.chat.android.client.experimental.plugin.listeners.SendEventListener
import io.getstream.chat.android.client.models.EventType
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.offline.experimental.channel.state.toMutableState
import io.getstream.chat.android.offline.experimental.plugin.listener.SendEventListenerImpl
import io.getstream.chat.android.offline.experimental.plugin.state.StateRegistry
import io.getstream.chat.android.offline.randomUser
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import java.util.Date

@ExperimentalCoroutinesApi
@ExperimentalStreamChatApi
internal class TypingEventsTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val channelType = "test_channel_type"
    private val channelId = "test_channel_id"

    @Test
    fun `When typing events are disabled Should not pass precondition`() =
        runBlockingTest {

            val (sut, _) = Fixture(testCoroutines.scope, randomUser())
                .givenTypingEventsDisabled(channelType, channelId)
                .get()

            sut.onSendEventPrecondition(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                Date()
            ).isSuccess `should be equal to` false
        }

    @Test
    fun `When a user started typing Then subsequent keystroke events within a certain interval should not be sent to the server`() =
        runBlockingTest {
            val (sut, _) = Fixture(testCoroutines.scope, randomUser())
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            Thread.sleep(3001) // Just to cool down because other tests can run before this.
            val eventTime = Date()
            sut.onSendEventPrecondition(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                eventTime
            ).isSuccess `should be equal to` true

            sut.onSendEventRequest(EventType.TYPING_START, channelType, channelId, emptyMap(), eventTime)
            sut.onSendEventResult(mock(), EventType.TYPING_START, channelType, channelId, emptyMap(), eventTime)

            sut.onSendEventPrecondition(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                Date()
            ).isError `should be equal to` true

            Thread.sleep(3001)

            sut.onSendEventPrecondition(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                Date()
            ).isSuccess `should be equal to` true
        }

    @Test
    fun `When stop typing event is sent without sending start typing event before Should not send event to the server`() =
        runBlockingTest {
            val (sut, _) = Fixture(testCoroutines.scope, randomUser())
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            sut.onSendEventPrecondition(
                EventType.TYPING_STOP,
                channelType,
                channelId,
                emptyMap(),
                Date()
            ).isSuccess `should be equal to` false
        }

    @Test
    fun `When stop typing event is sent after sending start typing event before Should send event to the server`() =
        runBlockingTest {
            val (sut, _) = Fixture(testCoroutines.scope, randomUser())
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            sut.onSendEventRequest(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                Date()
            )

            sut.onSendEventPrecondition(
                EventType.TYPING_STOP,
                channelType,
                channelId,
                emptyMap(),
                Date()
            ).isSuccess `should be equal to` true

            sut.onSendEventRequest(
                EventType.TYPING_STOP,
                channelType,
                channelId,
                emptyMap(),
                Date()
            )
        }

    @Test
    fun `When sending start typing event Should update lastStartTypeEvent`() =
        runBlockingTest {
            val (sut, stateRegistry) = Fixture(testCoroutines.scope, randomUser())
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            val eventTime = Date()
            sut.onSendEventRequest(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                eventTime
            )

            stateRegistry.channel(channelType, channelId)
                .toMutableState().lastStartTypingEvent `should be equal to` eventTime
        }

    @ExperimentalStreamChatApi
    private class Fixture(scope: CoroutineScope, user: User) {
        private val stateRegistry = StateRegistry.getOrCreate(
            scope = scope,
            userStateFlow = MutableStateFlow(user),
            messageRepository = mock(),
            latestUsers = MutableStateFlow(emptyMap()),
        )

        fun givenTypingEventsDisabled(channelType: String, channelId: String): Fixture {
            val channelState = stateRegistry.channel(channelType, channelId).toMutableState()
            channelState._channelConfig.value = channelState._channelConfig.value.copy(
                typingEventsEnabled = false
            )
            return this
        }

        fun givenTypingEventsEnabled(channelType: String, channelId: String): Fixture {
            val channelState = stateRegistry.channel(channelType, channelId).toMutableState()
            channelState._channelConfig.value = channelState._channelConfig.value.copy(
                typingEventsEnabled = true
            )
            return this
        }

        fun get(): Pair<SendEventListener, StateRegistry> {
            return SendEventListenerImpl(state = stateRegistry) to stateRegistry
        }
    }
}
