/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.offline.extensions

import io.getstream.log.StreamLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

private val logger by lazy { StreamLog.getLogger("Chat:CoroutineScope") }

/**
 * Launches a coroutine that executes the given [block] within a mutex lock.
 *
 * Used to protect against concurrent writes to a single database table.
 *
 * This extension function ensures thread-safe execution by acquiring the provided [mutex]
 * before executing the [block].
 *
 * Any [Exception] thrown during execution is caught and logged. We assume such exceptions
 * can happen only in the edge-case where the database is corrupted/closed, so we can safely ignore them.
 *
 * @param mutex The [Mutex] to acquire before executing the block.
 * @param block The suspend function to execute within the mutex lock.
 * @return A [kotlinx.coroutines.Job] representing the launched coroutine.
 */
internal fun CoroutineScope.launchWithMutex(mutex: Mutex, block: suspend () -> Unit) = launch {
    try {
        mutex.withLock { block() }
    } catch (e: IllegalStateException) {
        logger.e(e) { "Exception in launchWithMutex: ${e.message}" }
    }
}
