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

package io.getstream.chat.android.ai.compose.util.internal

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
internal class SharedLoadsTest {

    @Test
    fun `callers that ask for a key while it loads share one load`() = runTest {
        val loads = SharedLoads<String>(backgroundScope)
        val release = CompletableDeferred<Unit>()
        var fetches = 0
        val fetch: suspend () -> String = {
            fetches++
            release.await()
            "image"
        }

        val first = async { loads.load("a", fetch) }
        val second = async { loads.load("a", fetch) }
        runCurrent()
        release.complete(Unit)

        assertEquals("image", first.await())
        assertEquals("image", second.await())
        assertEquals(1, fetches)
        assertEquals(0, loads.size)
    }

    @Test
    fun `another key loads on its own`() = runTest {
        val loads = SharedLoads<String>(backgroundScope)
        var fetches = 0

        assertEquals(
            "a",
            loads.load("a") {
                fetches++
                "a"
            },
        )
        assertEquals(
            "b",
            loads.load("b") {
                fetches++
                "b"
            },
        )
        // Once done, a key loads again: what is kept is up to the caller.
        assertEquals(
            "a",
            loads.load("a") {
                fetches++
                "a"
            },
        )
        assertEquals(3, fetches)
    }

    @Test
    fun `a load goes on while one of its callers still waits`() = runTest {
        val loads = SharedLoads<String>(backgroundScope)
        val release = CompletableDeferred<Unit>()
        val fetch: suspend () -> String = {
            release.await()
            "image"
        }

        val leaving = launch { loads.load("a", fetch) }
        val staying = async { loads.load("a", fetch) }
        runCurrent()
        leaving.cancel()
        runCurrent()
        release.complete(Unit)

        assertEquals("image", staying.await())
    }

    @Test
    fun `a load stops once none of its callers waits`() = runTest {
        val loads = SharedLoads<String>(backgroundScope)
        var stopped = false
        val fetch: suspend () -> String = {
            try {
                awaitCancellation()
            } finally {
                stopped = true
            }
        }

        val first = launch { loads.load("a", fetch) }
        val second = launch { loads.load("a", fetch) }
        runCurrent()
        first.cancel()
        second.cancel()
        runCurrent()

        assertTrue(stopped)
        assertEquals(0, loads.size)
    }
}
