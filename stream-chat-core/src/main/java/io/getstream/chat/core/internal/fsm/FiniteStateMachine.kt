package io.getstream.chat.core.internal.fsm

import io.getstream.chat.core.internal.InternalStreamChatApi
import io.getstream.chat.core.internal.fsm.builder.FSMBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * This class represents a Finite State Machine. It can be only in one possible state at a time
 * out of the set of possible states [S]. It can handle events from the set [E].
 *
 * @param initialState the initial state
 * @property stateFunctions a map of states and possible event handlers for them
 * @property defaultEventHandler called when [stateFunctions] has no handler for
 *                               a given state/event combination
 */
@InternalStreamChatApi
public class FiniteStateMachine<S : State, E : Event>(
    initialState: S,
    private val stateFunctions: Map<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>>,
    private val defaultEventHandler: (S, E) -> Unit,
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
                val handler = stateFunctions[currentState::class]?.get(event::class)
                if (handler != null) {
                    _state = handler.invoke(this@FiniteStateMachine, currentState, event)
                } else {
                    defaultEventHandler(currentState, event)
                }
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
        public operator fun <S : State, E : Event> invoke(builder: FSMBuilder<S, E>.() -> Unit): FiniteStateMachine<S, E> {
            return FSMBuilder<S, E>().apply(builder).build()
        }
    }
}
