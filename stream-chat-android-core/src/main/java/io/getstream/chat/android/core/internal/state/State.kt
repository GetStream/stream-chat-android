package io.getstream.chat.android.core.internal.state

import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.core.internal.fsm.FiniteStateMachine
import io.getstream.chat.android.core.internal.state.State.StateType.Failure
import io.getstream.chat.android.core.internal.state.State.StateType.Value
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.runBlocking

/**
 * Wrapper on some Type with initial value. It lets to get and mutate value of type T safely in multithreaded context.
 * Biased to a failure. Means if it contains a throwable then any mutate or performing action do nothing on context.
 *
 * @param initialValue The initial value for State.
 */
public class State<T : Any?>(initialValue: T) {
    public companion object {
        public operator fun <T : Any?> invoke(initialValueProvider: () -> T): State<T> =
            initialValueProvider().let(::State)
    }

    private val scope = CoroutineScope(SupervisorJob())

    private val fsm = FiniteStateMachine<StateType<T>, StateChangeEvent<T>> {
        initialState(Value(initialValue))
        state<Value<T>> {
            onEvent<StateChangeEvent.StandardChangeEvent<T>> { currentState, event ->
                runCatching { event.mutateFunc(currentState.value).let(::Value) }.getOrElse(::Failure)
            }
            onEvent<StateChangeEvent.SuspendedChangeEvent<T>> { currentState, event ->
                runBlocking(scope.coroutineContext) {
                    runCatching { event.mutateFunc(currentState.value).let(::Value) }.getOrElse(::Failure)
                }
            }
            onEvent<StateChangeEvent.SideEffectAction<T>> { currentState, event ->
                runCatching { currentState.also { event.sideEffect(it.value) } }.getOrElse(::Failure)
            }
            onEvent<StateChangeEvent.SuspendedSideEffectAction<T>> { currentState, event ->
                runBlocking(scope.coroutineContext) {
                    runCatching { currentState.also { event.sideEffect(it.value) } }.getOrElse(::Failure)
                }
            }
        }
        state<Failure<T>> {
            // Skip all events to keep failure
        }
    }

    /**
     * Returns a value if state contains. Otherwise null.
     */
    public fun get(): T? = (fsm.state as? Value)?.value

    /**
     * Returns a value if state contains. Otherwise throws cause of failure.
     */
    @Throws(Throwable::class)
    public fun getOrThrow(): T {
        return when (val state = fsm.state) {
            is Value -> state.value
            is Failure -> throw state.cause
        }.exhaustive
    }

    /**
     * Performs mutate action on context.
     */
    public fun mutate(mutateAction: (T) -> T) {
        fsm.sendEvent(StateChangeEvent.StandardChangeEvent(mutateAction))
    }

    /**
     * Performs mutate suspended action on context blocking.
     */
    public fun mutate(mutateSuspendedAction: suspend (T) -> T) {
        fsm.sendEvent(StateChangeEvent.SuspendedChangeEvent(mutateSuspendedAction))
    }

    /**
     * Performs side effect action on context. If state contains a failure then does nothing.
     */
    public fun performOnContext(sideEffect: (T) -> Unit) {
        fsm.sendEvent(StateChangeEvent.SideEffectAction(sideEffect))
    }

    /**
     * Performs suspended side effect action on context blocking. If state contains a failure then does nothing.
     */
    public fun performOnContext(sideEffect: suspend (T) -> Unit) {
        fsm.sendEvent(StateChangeEvent.SuspendedSideEffectAction(sideEffect))
    }

    internal sealed class StateChangeEvent<T> {
        internal class StandardChangeEvent<T : Any?>(val mutateFunc: (T) -> T) : StateChangeEvent<T>()
        internal class SuspendedChangeEvent<T : Any?>(val mutateFunc: suspend (T) -> T) : StateChangeEvent<T>()
        internal class SideEffectAction<T : Any?>(val sideEffect: (T) -> Unit) : StateChangeEvent<T>()
        internal class SuspendedSideEffectAction<T : Any?>(val sideEffect: suspend (T) -> Unit) : StateChangeEvent<T>()
    }

    private sealed class StateType<T> {
        data class Value<T : Any?>(val value: T) : StateType<T>()
        class Failure<T : Any?>(val cause: Throwable) : StateType<T>()
    }
}
