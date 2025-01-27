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

package io.getstream.chat.android.core.internal.lazy

import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger

internal class ParameterizedLazyTest {

    @Test
    fun `test parameterized lazy`() = runBlocking {
        val counter = AtomicInteger()
        val initializer: suspend (Int) -> String = {
            counter.incrementAndGet()
            "test$it"
        }
        val loadStringForNumber = parameterizedLazy(initializer)
        val string1a = loadStringForNumber(1)
        val string1b = loadStringForNumber(1)
        val string2a = loadStringForNumber(2)
        val string2b = loadStringForNumber(2)

        counter.get() `should be equal to` 2
        Assertions.assertSame(string1a, string1b)
        Assertions.assertSame(string2a, string2b)
        Assertions.assertNotSame(string1a, string2b)
    }
}
