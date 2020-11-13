package io.getstream.chat.core.functional.fsm.builder

import io.getstream.chat.core.functional.fsm.Event
import io.getstream.chat.core.functional.fsm.FiniteStateMachine
import io.getstream.chat.core.functional.fsm.State
import io.getstream.chat.core.functional.fsm.StateFunction
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
public class StateHandler<S : State, S1 : S, E : Event> {
    public var eventHandlers: Map<KClass<out E>, FiniteStateMachine<S, E>.(S1, E) -> S> = mapOf()

    public inline fun <reified E1 : E> onEvent(noinline func: FiniteStateMachine<S, E>.(S1, E1) -> S) {
        eventHandlers = eventHandlers + (E1::class to func as FiniteStateMachine<S, E>.(S1, E) -> S)
    }

    public fun get(): Map<KClass<out E>, StateFunction<S, E>> = eventHandlers as Map<KClass<out E>, StateFunction<S, E>>
}