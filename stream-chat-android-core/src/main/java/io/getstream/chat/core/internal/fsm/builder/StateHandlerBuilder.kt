package io.getstream.chat.core.internal.fsm.builder

import io.getstream.chat.core.internal.InternalStreamChatApi
import io.getstream.chat.core.internal.fsm.FiniteStateMachine
import io.getstream.chat.core.internal.fsm.StateFunction
import kotlin.reflect.KClass

@InternalStreamChatApi
@FSMBuilderMarker
public class StateHandlerBuilder<S : Any, E : Any, S1 : S> {
    @PublishedApi
    internal val eventHandlers: MutableMap<KClass<out E>, FiniteStateMachine<S, E>.(S1, E) -> S> = mutableMapOf()

    @FSMBuilderMarker
    public inline fun <reified E1 : E> onEvent(noinline func: FiniteStateMachine<S, E>.(S1, E1) -> S) {
        @Suppress("UNCHECKED_CAST")
        eventHandlers[E1::class] = func as FiniteStateMachine<S, E>.(S1, E) -> S
    }

    @PublishedApi
    @Suppress("UNCHECKED_CAST")
    internal fun get(): Map<KClass<out E>, StateFunction<S, E>> =
        eventHandlers as Map<KClass<out E>, StateFunction<S, E>>
}
