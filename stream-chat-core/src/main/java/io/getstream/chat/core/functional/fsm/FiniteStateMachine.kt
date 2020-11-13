package io.getstream.chat.core.functional.fsm

import io.getstream.chat.core.functional.fsm.builder.FSMBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

/**
 * This class represents Finite State Machine concept. It can be only in one possible state of the set possible
 * states S. It can handle events from the set E.
 *
 * @param initialState the first initial state of FSM.
 * @property stateFunctions a map of states and possible event handlers for it.
 * @property defaultEventHandler a function of state and event for a case if in [stateFunctions] there is not such handler.
 */
public class FiniteStateMachine<S : State, E : Event>(
    initialState: S,
    private val stateFunctions: Map<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>>,
    private val defaultEventHandler: Function2<S, E, Unit>
) {
    private val mutex = Mutex()
    private var _state: S = initialState

    private suspend fun <T> Mutex.withLockIfNot(action: () -> T): T {
        return if (isLocked.not()) {
            withLock { action() }
        } else {
            action()
        }
    }

    /**
     * Return value of the current state.
     */
    public val state: S
        get() = runBlocking {
            mutex.withLockIfNot { _state }
        }

    /**
     * An entry point to change FSM state. Sends event to state machine
     */
    public fun sendEvent(event: E) {
        runBlocking {
            mutex.withLock {
                val currentState = state
                transitionTo(
                    stateFunctions[currentState::class]?.get(event::class)
                        ?.invoke(this@FiniteStateMachine, currentState, event)
                        ?: currentState.also { defaultEventHandler(it, event) }
                )
            }
        }
    }

    /**
     * Makes transition to some new state.
     *
     * @param state new target state value.
     */
    public fun transitionTo(state: S) {
        runBlocking {
            mutex.withLockIfNot {
                _state = state
            }
        }
    }

    /**
     * Keeps current FSM into the current state.
     * Usually used when for such event handling it's not needed to make transition.
     *
     * onEvent<SomeEvent> { state, event -> stay() }
     *
     * @return the current state value.
     */
    public fun stay(): S = state

   public companion object {
        public operator fun <S : State, E : Event> invoke(builder: FSMBuilder<S, E>.() -> Unit): FiniteStateMachine<S, E> {
            return FSMBuilder<S, E>().apply(builder).build()
        }
    }
}

