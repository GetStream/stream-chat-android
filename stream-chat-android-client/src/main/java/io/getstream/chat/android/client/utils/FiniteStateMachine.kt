package io.getstream.chat.android.client.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

private typealias StateFunction<S, E> = FiniteStateMachine<S, E>.(S, E) -> S

/**
 * This class represents Finite State Machine concept. It can be only in one possible state of the set possible
 * states S. It can handle events from the set E.
 *
 * @param initialState the first initial state of FSM.
 * @property stateFunctions a map of states and possible event handlers for it.
 * @property defaultEventHandler a function of state and event for a case if in [stateFunctions] there is not such handler.
 */
internal class FiniteStateMachine<S : State, E : Event>(
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
    fun sendEvent(event: E) = runBlocking {
        mutex.withLock {
            val currentState = state
            transitionTo(
                stateFunctions[currentState::class]?.get(event::class)
                    ?.invoke(this@FiniteStateMachine, currentState, event)
                    ?: currentState.also { defaultEventHandler(it, event) }
            )
        }
    }

    /**
     * Makes transition to some new state.
     *
     * @param state new target state value.
     */
    fun transitionTo(state: S) = runBlocking {
        mutex.withLockIfNot {
            _state = state
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
    fun stay(): S = state

    companion object {
        operator fun <S : State, E : Event> invoke(builder: FSMBuilder<S, E>.() -> Unit): FiniteStateMachine<S, E> {
            return FSMBuilder<S, E>().apply(builder).build()
        }
    }
}

internal class FSMBuilder<S : State, E : Event> {
    private lateinit var _initialState: S
    private var stateFunctions: Map<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>> = emptyMap()
    private var _defaultHandler: (S, E) -> Unit = { _, _ -> Unit }
    internal fun initialState(state: S) {
        _initialState = state
    }

    internal fun defaultHandler(defaultHandler: (S, E) -> Unit) {
        _defaultHandler = defaultHandler
    }

    internal inline fun <reified S1 : S> state(stateHandlerBuilder: StateHandler<S, S1, E>.() -> Unit) {
        stateFunctions =
            stateFunctions + (S1::class to StateHandler<S, S1, E>().apply(stateHandlerBuilder).get())
    }

    internal fun build(): FiniteStateMachine<S, E> {
        if (this::_initialState.isInitialized.not()) {
            error("Initial state must be set!")
        }
        return FiniteStateMachine(_initialState, stateFunctions, _defaultHandler)
    }
}

internal class StateHandler<S : State, S1 : S, E : Event> {
    private var eventHandlers: Map<KClass<out E>, FiniteStateMachine<S, E>.(S1, E) -> S> = mapOf()

    inline fun <reified E1 : E> onEvent(noinline func: FiniteStateMachine<S, E>.(S1, E1) -> S) {
        eventHandlers = eventHandlers + (E1::class to func as FiniteStateMachine<S, E>.(S1, E) -> S)
    }

    internal fun get() = eventHandlers as Map<KClass<out E>, StateFunction<S, E>>
}

internal interface State
internal interface Event
