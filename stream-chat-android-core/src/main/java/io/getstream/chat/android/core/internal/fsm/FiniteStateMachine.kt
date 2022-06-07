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

package io.getstream.chat.android.core.internal.fsm

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import io.getstream.chat.android.core.internal.fsm.builder.FSMBuilder
import io.getstream.chat.android.core.internal.fsm.builder.FSMBuilderMarker
import io.getstream.chat.android.core.internal.fsm.builder.StateFunction
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * This class represents a Finite State Machine. It can be only in one possible state at a time
 * out of the set of possible states [S]. It can handle events from the set [E].
 *
 * @param initialState The initial state.
 * @property stateFunctions A map of states and possible event handlers for them.
 * @property defaultEventHandler Called when [stateFunctions] has no handler for
 *                               a given state/event combination.
 */
@InternalStreamChatApi
public class FiniteStateMachine<S : Any, E : Any>(
    initialState: S,
    private val stateFunctions: Map<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>>,
    private val defaultEventHandler: (S, E) -> S,
) {
    private val mutex = Mutex()
    private var _state: S = initialState

    private suspend inline fun <T> Mutex.withLockIfNotLocked(action: () -> T): T {
        return if (isLocked.not()) {
            withLock { action() }
        } else {
            action()
        }
    }

    /**
     * The current state.
     */
    public val state: S
        get() = runBlocking {
            mutex.withLockIfNotLocked { _state }
        }

    /**
     * Sends an event to the state machine. The entry point to change state.
     */
    public fun sendEvent(event: E) {
        runBlocking {
            mutex.withLock {
                val currentState = _state
                val handler = stateFunctions[currentState::class]?.get(event::class) ?: defaultEventHandler
                _state = handler(currentState, event)
            }
        }
    }

    /**
     * Keeps the FSM in its current state.
     * Usually used when handling events that don't need to make a transition.
     *
     * ```kotlin
     * onEvent<SomeEvent> { state, event -> stay() }
     * ```
     *
     * @return the current state value
     */
    public fun stay(): S = state

    public companion object {
        @FSMBuilderMarker
        public operator fun <S : Any, E : Any> invoke(builder: FSMBuilder<S, E>.() -> Unit): FiniteStateMachine<S, E> {
            return FSMBuilder<S, E>().apply(builder).build()
        }
    }
}
