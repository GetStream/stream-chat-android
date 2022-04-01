package io.getstream.chat.android.ui.common

import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Utility class for debouncing high frequency events.
 *
 * [submit]ting a new piece of work to run within the debounce window
 * will cancel the previously submitted pending work.
 */
public class Debouncer(private val debounceMs: Long) {

    private val scope = CoroutineScope(DispatcherProvider.Main)
    private var job: Job? = null

    /**
     * Cancels the previous work and launches a new coroutine
     * containing the new work.
     */
    public fun submit(work: () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(debounceMs)
            work()
        }
    }

    /**
     * Cancels the previous work and launches a new coroutine
     * containing the new suspendable work.
     */
    public fun submitSuspendable(work: suspend () -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(debounceMs)
            work()
        }
    }

    /**
     * Cancels the current work without shutting down the Coroutine scope.
     */
    public fun cancelLastDebounce() {
        job?.cancel()
    }

    /**
     * Cleans up any pending work.
     *
     * Note that a shut down Debouncer will never execute work again.
     */
    public fun shutdown() {
        scope.cancel()
    }
}
