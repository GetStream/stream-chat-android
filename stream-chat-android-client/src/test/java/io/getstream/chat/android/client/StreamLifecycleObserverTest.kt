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

package io.getstream.chat.android.client

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.testing.TestLifecycleOwner
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verifyBlocking

internal class StreamLifecycleObserverTest {

    private companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    /**
     * Regression test. Subscribing from a process that is not resumed gets no replayed ON_RESUME,
     * so the next one is a real foregrounding and has to reach the handlers. Before the fix it was
     * swallowed together with the replay, and a socket waiting on a resume was never woken.
     */
    @Test
    fun `when subscribed while not resumed, the following resume is delivered`() = runTest(testCoroutines.dispatcher) {
        val owner = TestLifecycleOwner(Lifecycle.State.CREATED, testCoroutines.dispatcher)
        val observer = StreamLifecycleObserver(testCoroutines.scope, owner.lifecycle)
        val handler: LifecycleHandler = mock()

        observer.observe(handler)
        owner.setCurrentState(Lifecycle.State.RESUMED)

        verifyBlocking(handler) { resume() }
    }

    @Test
    fun `when subscribed while already resumed, the replayed resume is ignored`() = runTest(testCoroutines.dispatcher) {
        val owner = TestLifecycleOwner(Lifecycle.State.RESUMED, testCoroutines.dispatcher)
        val observer = StreamLifecycleObserver(testCoroutines.scope, owner.lifecycle)
        val handler: LifecycleHandler = mock()

        observer.observe(handler)

        verifyBlocking(handler, never()) { resume() }
    }

    @Test
    fun `when resumed again after the first delivery, the resume is still delivered`() = runTest(testCoroutines.dispatcher) {
        val owner = TestLifecycleOwner(Lifecycle.State.RESUMED, testCoroutines.dispatcher)
        val observer = StreamLifecycleObserver(testCoroutines.scope, owner.lifecycle)
        val handler: LifecycleHandler = mock()
        observer.observe(handler)

        owner.setCurrentState(Lifecycle.State.CREATED)
        owner.setCurrentState(Lifecycle.State.RESUMED)

        verifyBlocking(handler) { resume() }
    }
}
