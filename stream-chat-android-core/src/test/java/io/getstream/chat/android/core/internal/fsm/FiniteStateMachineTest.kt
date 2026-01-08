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

package io.getstream.chat.android.core.internal.fsm

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class FiniteStateMachineTest {

    @Test
    fun testInitialState() {
        val subject = createFsm()
        subject.state `should be equal to` State.NotInitialized
    }

    @Test
    fun testUnsupportedEvent() {
        val subject = createFsm()
        runBlocking {
            subject.sendEvent(Event.Update(1))
        }
        subject.state `should be equal to` State.NotInitialized
    }

    @Test
    fun testSupportedEvents() {
        val subject = createFsm()
        runBlocking {
            subject.sendEvent(Event.Initialize(1))
        }
        subject.state `should be equal to` State.Initialized(1)
        runBlocking {
            subject.sendEvent(Event.Update(2))
        }
        subject.state `should be equal to` State.Initialized(2)
        runBlocking {
            subject.sendEvent(Event.Reset)
        }
        subject.state `should be equal to` State.NotInitialized
    }

    @Test
    fun testStay() {
        val subject = createFsm()
        runBlocking {
            subject.sendEvent(Event.Initialize(1))
        }
        subject.state `should be equal to` State.Initialized(1)
        subject.stay()
        subject.state `should be equal to` State.Initialized(1)
    }

    private fun createFsm() = FiniteStateMachine<State, Event> {
        defaultHandler { state, _ -> state }
        initialState(State.NotInitialized)
        state<State.NotInitialized> {
            onEvent<Event.Initialize> { event -> State.Initialized(event.value) }
        }
        state<State.Initialized> {
            onEvent<Event.Update> { event -> State.Initialized(event.value) }
            onEvent<Event.Reset> { State.NotInitialized }
        }
    }

    sealed interface State {
        data object NotInitialized : State
        data class Initialized(val value: Int) : State
    }

    sealed interface Event {
        data class Initialize(val value: Int) : Event
        data class Update(val value: Int) : Event
        data object Reset : Event
    }
}
