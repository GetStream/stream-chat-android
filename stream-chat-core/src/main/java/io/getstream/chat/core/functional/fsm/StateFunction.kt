package io.getstream.chat.core.functional.fsm

internal typealias StateFunction<S, E> = FiniteStateMachine<S, E>.(S, E) -> S
