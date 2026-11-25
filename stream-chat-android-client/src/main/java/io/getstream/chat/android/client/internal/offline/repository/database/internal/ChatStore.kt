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

package io.getstream.chat.android.client.internal.offline.repository.database.internal

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Runs the writes to a user's database one at a time, each in one transaction.
 *
 * A [write] is not cancelled with its caller, so a cancelled request still stores what it started to store. A [write]
 * called inside another one joins it. If any of them throws, also one whose exception was caught, the outer [write]
 * throws at its end and nothing is stored.
 *
 * A [write] must not wait for work that writes from another coroutine: that write waits for this one to end.
 *
 * @param runInTransaction Runs a block in one database transaction, committed unless the block throws.
 */
internal class ChatStore(
    private val runInTransaction: suspend (block: suspend () -> Unit) -> Unit,
) {

    private val mutex = Mutex()
    private val rollbackListeners = CopyOnWriteArrayList<() -> Unit>()

    /**
     * Calls [listener] when a write fails and nothing of it is stored, so memory that ran ahead of the database
     * can be dropped.
     */
    fun onRollback(listener: () -> Unit) {
        rollbackListeners += listener
    }

    suspend fun <T> write(block: suspend () -> T): T {
        val openWrite = currentCoroutineContext()[OpenWrite]?.takeIf { it.store === this && it.isOpen }
        if (openWrite != null) return openWrite.join(block)
        return withContext(NonCancellable) { mutex.withLock { writeInTransaction(block) } }
    }

    @Suppress("TooGenericExceptionCaught")
    private suspend fun <T> writeInTransaction(block: suspend () -> T): T {
        val write = OpenWrite(this)
        var result: Result<T>? = null
        try {
            runInTransaction {
                result = Result.success(withContext(write) { block() })
                write.failure?.let { throw it }
            }
        } catch (e: Throwable) {
            rollbackListeners.forEach { it() }
            throw e
        } finally {
            write.isOpen = false
        }
        return checkNotNull(result) { "The transaction ended without running the write" }.getOrThrow()
    }

    /** Marks the coroutines of a running [write], so the writes they call join it. */
    private class OpenWrite(val store: ChatStore) : AbstractCoroutineContextElement(Key) {

        @Volatile
        var isOpen = true

        /** The first failure of a joined write. It fails the whole write, as the transaction cannot commit a part. */
        @Volatile
        var failure: Throwable? = null

        @Suppress("TooGenericExceptionCaught")
        suspend fun <T> join(block: suspend () -> T): T =
            try {
                block()
            } catch (e: Throwable) {
                if (failure == null) failure = e
                throw e
            }

        companion object Key : CoroutineContext.Key<OpenWrite>
    }
}

/**
 * Runs [block] as one write of the store, or directly when the repositories have no database and so no store.
 */
internal suspend fun <T> ChatStore?.writeOrRun(block: suspend () -> T): T =
    if (this != null) write(block) else block()
