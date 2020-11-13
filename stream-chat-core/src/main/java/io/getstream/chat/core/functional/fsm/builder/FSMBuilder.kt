package io.getstream.chat.core.functional.fsm.builder

import io.getstream.chat.core.functional.fsm.Event
import io.getstream.chat.core.functional.fsm.FiniteStateMachine
import io.getstream.chat.core.functional.fsm.State
import io.getstream.chat.core.functional.fsm.StateFunction
import kotlin.reflect.KClass

public class FSMBuilder<S : State, E : Event> {
    private lateinit var _initialState: S
    public var stateFunctions: Map<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>> = emptyMap()
    private var _defaultHandler: (S, E) -> Unit = { _, _ -> Unit }

    public fun initialState(state: S) {
        _initialState = state
    }

    public fun defaultHandler(defaultHandler: (S, E) -> Unit) {
        _defaultHandler = defaultHandler
    }

    public inline fun <reified S1 : S> state(stateHandlerBuilder: StateHandler<S, S1, E>.() -> Unit) {
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