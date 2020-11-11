package io.getstream.chat.android.client.utils

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.reflect.KClass

internal class FiniteStateMachine<S : State, E : Event>(
    initialState: S,
    private val stateFunctions: Map<KClass<in S>, Map<KClass<in E>, Function2<S, E, S>>>
) {
    private val mutex = Mutex()
    private var _state: S = initialState

    public val state: S
        get() = runBlocking {
            mutex.withLock { _state }
        }

    fun onEvent(event: E) = runBlocking {
        mutex.withLock {
            val currentState = state
            transitionTo(
                stateFunctions.get(currentState::class)?.get(event::class)?.invoke(currentState, event) ?: currentState
            )
        }
    }

    fun transitionTo(state: S) = runBlocking {
        mutex.withLock {
            _state = state
        }
    }

    companion object {
        operator fun <S : State, E : Event> invoke(builder: FSMBuilder<S, E>.() -> Unit): FiniteStateMachine<S, E> {
            return FSMBuilder<S, E>().apply(builder).build()
        }
    }
}

internal class FSMBuilder<S : State, E : Event> {
    private lateinit var _initialState: S
    private var stateFunctions: Map<KClass<in S>, Map<KClass<in E>, Function2<S, E, S>>> = emptyMap()
    internal fun initialState(state: S) {
        _initialState = state
    }

    internal inline fun <reified S1 : S> state(stateHandlerBuilder: StateHandler<S, E>.() -> Unit) {
        stateFunctions =
            stateFunctions + ((S1::class as KClass<S>) to StateHandler<S, E>().apply(stateHandlerBuilder).get())
    }

    internal fun build(): FiniteStateMachine<S, E> {
        if (this::_initialState.isInitialized.not()) {
            error("Initial state must be set!")
        }
        return FiniteStateMachine(_initialState, stateFunctions)
    }
}

internal class StateHandler<S : State, E : Event> {
    private var eventHandlers: Map<KClass<in E>, Function2<S, E, S>> = mapOf()

    inline fun <reified E1 : E> onEvent(noinline func: (S, E) -> S) {
        eventHandlers = eventHandlers + ((E1::class as KClass<E>) to func)
    }

    internal fun get() = eventHandlers
}

internal interface State
internal interface Event