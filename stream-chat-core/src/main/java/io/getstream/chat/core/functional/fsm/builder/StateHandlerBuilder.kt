package io.getstream.chat.core.functional.fsm.builder

import io.getstream.chat.core.functional.fsm.Event
import io.getstream.chat.core.functional.fsm.FiniteStateMachine
import io.getstream.chat.core.functional.fsm.State
import io.getstream.chat.core.functional.fsm.StateFunction
import kotlin.reflect.KClass

@FSMBuilderMarker
public class StateHandlerBuilder<S : State, E : Event, S1 : S> {
    @PublishedApi
    internal val eventHandlers: MutableMap<KClass<out E>, FiniteStateMachine<S, E>.(S1, E) -> S> = mutableMapOf()

    public inline fun <reified E1 : E> onEvent(noinline func: FiniteStateMachine<S, E>.(S1, E1) -> S) {
        @Suppress("UNCHECKED_CAST")
        eventHandlers[E1::class] = func as FiniteStateMachine<S, E>.(S1, E) -> S
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun get(): Map<KClass<out E>, StateFunction<S, E>> = eventHandlers as Map<KClass<out E>, StateFunction<S, E>>
}
