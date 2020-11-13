package io.getstream.chat.core.internal.fsm.builder

import io.getstream.chat.core.internal.InternalStreamChatApi
import io.getstream.chat.core.internal.fsm.Event
import io.getstream.chat.core.internal.fsm.FiniteStateMachine
import io.getstream.chat.core.internal.fsm.State
import io.getstream.chat.core.internal.fsm.StateFunction
import kotlin.reflect.KClass

@InternalStreamChatApi
@FSMBuilderMarker
public class FSMBuilder<S : State, E : Event> {
    private lateinit var _initialState: S
    public val stateFunctions: MutableMap<KClass<out S>, Map<KClass<out E>, StateFunction<S, E>>> = mutableMapOf()
    private var _defaultHandler: (S, E) -> Unit = { _, _ -> Unit }

    public fun initialState(state: S) {
        _initialState = state
    }

    public fun defaultHandler(defaultHandler: (S, E) -> Unit) {
        _defaultHandler = defaultHandler
    }

    public inline fun <reified S1 : S> state(stateHandlerBuilder: StateHandlerBuilder<S, E, S1>.() -> Unit) {
        stateFunctions[S1::class] = StateHandlerBuilder<S, E, S1>().apply(stateHandlerBuilder).get()
    }

    internal fun build(): FiniteStateMachine<S, E> {
        check(this::_initialState.isInitialized) { "Initial state must be set!" }
        return FiniteStateMachine(_initialState, stateFunctions, _defaultHandler)
    }
}
