package io.getstream.chat.core.internal.fsm

internal typealias StateFunction<S, E> = FiniteStateMachine<S, E>.(S, E) -> S
