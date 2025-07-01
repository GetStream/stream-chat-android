/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.events

import io.getstream.chat.android.client.plugin.listeners.TypingEventListener
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.User
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.listener.internal.TypingEventListenerState
import io.getstream.chat.android.state.plugin.state.StateRegistry
import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.result.Result
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
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
        val (sut, _) = Fixture(testCoroutines.scope, randomUser())
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
            val (sut, _) = Fixture(testCoroutines.scope, randomUser())
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
            val (sut, _) = Fixture(testCoroutines.scope, randomUser())
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
            val (sut, _) = Fixture(testCoroutines.scope, randomUser())
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
            val (sut, stateRegistry) = Fixture(testCoroutines.scope, randomUser())
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

            stateRegistry.mutableChannel(channelType, channelId).lastStartTypingEvent `should be equal to` eventTime
        }

    private class Fixture(scope: CoroutineScope, user: User) {
        private val stateRegistry = StateRegistry(
            job = mock(),
            scope = scope,
            now = { System.currentTimeMillis() },
            userStateFlow = MutableStateFlow(user),
            latestUsers = MutableStateFlow(emptyMap()),
            activeLiveLocations = MutableStateFlow(emptyList()),
        )

        fun givenTypingEventsDisabled(channelType: String, channelId: String): Fixture {
            val channelState = stateRegistry.mutableChannel(channelType, channelId)
            channelState.setChannelConfig(
                channelState.channelConfig.value.copy(
                    typingEventsEnabled = false,
                ),
            )
            return this
        }

        fun givenTypingEventsEnabled(channelType: String, channelId: String): Fixture {
            val channelState = stateRegistry.mutableChannel(channelType, channelId)
            channelState.setChannelConfig(
                channelState.channelConfig.value.copy(
                    typingEventsEnabled = true,
                ),
            )
            return this
        }

        fun get(): Pair<TypingEventListener, StateRegistry> {
            return TypingEventListenerState(state = stateRegistry) to stateRegistry
        }
    }
}
