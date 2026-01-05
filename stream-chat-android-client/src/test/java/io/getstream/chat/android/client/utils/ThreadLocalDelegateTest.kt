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

package io.getstream.chat.android.client.utils

import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.thread

internal class ThreadLocalDelegateTest {

    private val delegate by threadLocal { Object() }

    @Test
    fun `Should create separate instance for each thread`() {
        val results = ConcurrentHashMap<Thread, Any>()

        (1..100)
            .map { thread { results.put(Thread.currentThread(), delegate) } }
            .forEach(Thread::join)

        val delegateValues = results.values
        delegateValues.size shouldBeEqualTo 100
        delegateValues.distinct().size shouldBeEqualTo 100
    }

    @Test
    fun `Should reuse same instance for a given thread`() {
        val results = mutableListOf<Any>()

        repeat(100) {
            results += delegate
        }

        results.size shouldBeEqualTo 100
        results.distinct().size shouldBeEqualTo 1
    }
}
