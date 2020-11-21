package io.getstream.chat.android.core.internal.fsm

internal typealias StateFunction<S, E> = FiniteStateMachine<S, E>.(S, E) -> S
