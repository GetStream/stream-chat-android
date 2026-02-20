/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.client.internal.state.events

import io.getstream.chat.android.client.internal.state.plugin.listener.internal.TypingEventListenerState
import io.getstream.chat.android.client.internal.state.plugin.logic.channel.internal.ChannelLogic
import io.getstream.chat.android.client.internal.state.plugin.logic.internal.LogicRegistry
import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

@ExperimentalCoroutinesApi
internal class TypingEventsTest {
    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private val channelType = "test_channel_type"
    private val channelId = "test_channel_id"

    @Test
    fun `When typing events are disabled Should not pass precondition`() = runTest {
        val (sut, _) = Fixture()
            .givenTypingEventsDisabled(channelType, channelId)
            .get()

        sut.onTypingEventPrecondition(
            EventType.TYPING_START,
            channelType,
            channelId,
            emptyMap(),
            Date(),
        ) shouldBeInstanceOf Result.Failure::class
    }

    @Test
    fun `When a user started typing Then subsequent keystroke events within a certain interval should not be sent to the server`() =
        runTest {
            val (sut, _) = Fixture()
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            Thread.sleep(3001) // Just to cool down because other tests can run before this.
            val eventTime = Date()
            sut.onTypingEventPrecondition(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                eventTime,
            ) shouldBeInstanceOf Result.Success::class

            sut.onTypingEventRequest(EventType.TYPING_START, channelType, channelId, emptyMap(), eventTime)
            sut.onTypingEventResult(mock(), EventType.TYPING_START, channelType, channelId, emptyMap(), eventTime)

            sut.onTypingEventPrecondition(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                Date(),
            ) shouldBeInstanceOf Result.Failure::class

            Thread.sleep(3001)

            sut.onTypingEventPrecondition(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                Date(),
            ) shouldBeInstanceOf Result.Success::class
        }

    @Test
    fun `When stop typing event is sent without sending start typing event before Should not send event to the server`() =
        runTest {
            val (sut, _) = Fixture()
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            sut.onTypingEventPrecondition(
                EventType.TYPING_STOP,
                channelType,
                channelId,
                emptyMap(),
                Date(),
            ) shouldBeInstanceOf Result.Failure::class
        }

    @Test
    fun `When stop typing event is sent after sending start typing event before Should send event to the server`() =
        runTest {
            val (sut, _) = Fixture()
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            sut.onTypingEventRequest(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                Date(),
            )

            sut.onTypingEventPrecondition(
                EventType.TYPING_STOP,
                channelType,
                channelId,
                emptyMap(),
                Date(),
            ) shouldBeInstanceOf Result.Success::class

            sut.onTypingEventRequest(
                EventType.TYPING_STOP,
                channelType,
                channelId,
                emptyMap(),
                Date(),
            )
        }

    @Test
    fun `When sending start typing event Should update lastStartTypeEvent`() =
        runTest {
            val (sut, logicRegistry) = Fixture()
                .givenTypingEventsEnabled(channelType, channelId)
                .get()

            val eventTime = Date()
            sut.onTypingEventRequest(
                EventType.TYPING_START,
                channelType,
                channelId,
                emptyMap(),
                eventTime,
            )

            logicRegistry.channel(channelType, channelId).getLastStartTypingEvent() `should be equal to` eventTime
        }

    private class Fixture {

        private var lastStartTypingEvent: Date? = null
        private val channelLogic: ChannelLogic = mock()
        private val logicRegistry: LogicRegistry = mock()

        init {
            whenever(channelLogic.setLastStartTypingEvent(any())).thenAnswer { answer ->
                lastStartTypingEvent = answer.getArgument(0)
            }
            whenever(channelLogic.getLastStartTypingEvent()).thenAnswer {
                lastStartTypingEvent
            }
        }

        fun givenTypingEventsDisabled(channelType: String, channelId: String): Fixture {
            whenever(channelLogic.typingEventsEnabled()).doReturn(false)
            whenever(logicRegistry.channel(channelType, channelId)).doReturn(channelLogic)
            return this
        }

        fun givenTypingEventsEnabled(channelType: String, channelId: String): Fixture {
            whenever(channelLogic.typingEventsEnabled()).doReturn(true)
            whenever(logicRegistry.channel(channelType, channelId)).doReturn(channelLogic)
            return this
        }

        fun get(): Pair<TypingEventListener, LogicRegistry> {
            return TypingEventListenerState(logicRegistry) to logicRegistry
        }
    }
}
