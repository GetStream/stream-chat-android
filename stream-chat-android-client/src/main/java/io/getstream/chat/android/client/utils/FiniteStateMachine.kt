package io.getstream.chat.android.client.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

internal typealias StateFunction<S, E> = FiniteStateMachine<S, E>.(S, E) -> S

internal class FiniteStateMachine<S : State, E : Event>(
    initialState: S,
    private val stateFunctions: Map<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>>,
    private val defaultEventHandler: Function2<S, E, Unit>
) {
    private val mutex = Mutex()
    private var _state: S = initialState

    public val state: S
        get() = runBlocking {
            mutex.withLock { _state }
        }

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
     * Keeps current FSM into the current state.
     */
    fun transitionTo(state: S) = runBlocking {
        mutex.withLock {
            _state = state
        }
    }

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