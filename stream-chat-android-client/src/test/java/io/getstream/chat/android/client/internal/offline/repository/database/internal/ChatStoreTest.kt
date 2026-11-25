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

import androidx.room.withTransaction
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.client.internal.offline.createRoomDB
import io.getstream.chat.android.client.internal.offline.repository.domain.syncState.internal.SyncStateEntity
import io.getstream.chat.android.randomString
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeNull
import org.junit.After
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** A [ChatStore] with no database behind it: its writes run one at a time, but in no transaction. */
internal fun testChatStore(): ChatStore = ChatStore { block -> block() }

@RunWith(AndroidJUnit4::class)
internal class ChatStoreTest {

    private lateinit var database: ChatDatabase
    private lateinit var store: ChatStore
    private val firstId = randomString()
    private val secondId = randomString()

    @Before
    fun setUp() {
        database = createRoomDB()
        store = ChatStore { block -> database.withTransaction { block() } }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `a write stores its rows and returns its result`() = runStoreTest {
        val result = store.write {
            insert(firstId)
            firstId
        }

        result shouldBeEqualTo firstId
        select(firstId).shouldNotBeNull()
    }

    @Test
    fun `a write that throws stores nothing`() = runStoreTest {
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                store.write {
                    insert(firstId)
                    error("failed")
                }
            }
        }

        select(firstId) shouldBeEqualTo null
    }

    @Test
    fun `a write inside another one is stored with it`() = runStoreTest {
        store.write {
            insert(firstId)
            store.write { insert(secondId) }
        }

        select(firstId).shouldNotBeNull()
        select(secondId).shouldNotBeNull()
    }

    @Test
    fun `a write inside another one runs once when it returns null`() = runStoreTest {
        var runs = 0

        val result = store.write {
            store.write<String?> {
                runs++
                null
            }
        }

        result shouldBeEqualTo null
        runs shouldBeEqualTo 1
    }

    @Test
    fun `a write called from a child coroutine of another one joins it`() = runStoreTest {
        store.write {
            coroutineScope { async { store.write { insert(firstId) } }.await() }
        }

        select(firstId).shouldNotBeNull()
    }

    @Test
    fun `a caught failure of a write inside another one fails the whole write`() = runStoreTest {
        assertThrows(IllegalStateException::class.java) {
            runBlocking {
                store.write {
                    insert(firstId)
                    runCatching {
                        store.write {
                            insert(secondId)
                            error("failed")
                        }
                    }
                }
            }
        }

        select(firstId) shouldBeEqualTo null
        select(secondId) shouldBeEqualTo null
    }

    @Test
    fun `a write after a failed one is stored`() = runStoreTest {
        runCatching { store.write { error("failed") } }

        store.write { insert(firstId) }

        select(firstId).shouldNotBeNull()
    }

    @Test
    fun `a write that throws calls the rollback listeners`() = runStoreTest {
        var rollbacks = 0
        store.onRollback { rollbacks++ }
        store.write { insert(firstId) }
        rollbacks shouldBeEqualTo 0

        runCatching { store.write { error("failed") } }

        rollbacks shouldBeEqualTo 1
    }

    @Test
    fun `a write whose caller is cancelled is still stored`() = runStoreTest {
        val started = CompletableDeferred<Unit>()
        val resumed = CompletableDeferred<Unit>()
        val caller = launch {
            store.write {
                started.complete(Unit)
                resumed.await()
                insert(firstId)
            }
        }
        started.await()

        caller.cancel()
        resumed.complete(Unit)
        caller.join()

        select(firstId).shouldNotBeNull()
    }

    @Test
    fun `writes run one at a time`() = runStoreTest {
        // No database, whose transactions would run one at a time on their own.
        val store = testChatStore()
        val steps = mutableListOf<String>()
        val firstStarted = CompletableDeferred<Unit>()
        val firstResumed = CompletableDeferred<Unit>()
        val first = launch {
            store.write {
                steps += "first started"
                firstStarted.complete(Unit)
                firstResumed.await()
                steps += "first ended"
            }
        }
        firstStarted.await()
        val second = launch { store.write { steps += "second" } }

        firstResumed.complete(Unit)
        first.join()
        second.join()

        steps shouldBeEqualTo listOf("first started", "first ended", "second")
    }

    private fun runStoreTest(testBody: suspend kotlinx.coroutines.CoroutineScope.() -> Unit) {
        // A deadlock fails the test instead of hanging it.
        runBlocking { withTimeout(TIMEOUT_MILLIS) { testBody() } }
    }

    private suspend fun insert(id: String) = database.syncStateDao().insert(SyncStateEntity(userId = id))

    private suspend fun select(id: String) = database.syncStateDao().select(id)

    private companion object {
        private const val TIMEOUT_MILLIS = 10_000L
    }
}
