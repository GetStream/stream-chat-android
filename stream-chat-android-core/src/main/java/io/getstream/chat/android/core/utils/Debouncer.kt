/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.core.utils

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
public class Debouncer(
    private val debounceMs: Long,
    private val scope: CoroutineScope = CoroutineScope(DispatcherProvider.Main),
) {

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
