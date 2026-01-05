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

package io.getstream.chat.android.client.utils.buffer

import io.getstream.chat.android.randomString
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be in`
import org.amshove.kluent.`should not be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(TestCoroutineExtension::class)
internal class StartStopBufferTest {

    @Test
    fun `data should only be propagated when enqueueData and subscribe are done`() {
        StartStopBuffer<String>().run {
            val data = randomString()
            var resultText = ""

            active(src = "test")
            subscribe { result ->
                resultText = result
            }

            enqueueData(data)

            resultText `should be equal to` data
        }
    }

    @Test
    fun `data should not be propagated if hold is called before subscribe`() {
        StartStopBuffer<String>().run {
            val initialValue = randomString()
            val data = randomString()
            var resultText = initialValue

            enqueueData(data)
            hold()
            subscribe { result ->
                resultText = result
            }

            resultText `should be equal to` initialValue
        }
    }

    @Test
    fun `buffer should hold until active is called`() {
        StartStopBuffer<String>().run {
            var lastNumber = "0"
            val data1 = randomString()

            hold()

            subscribe { data ->
                lastNumber = data
            }

            enqueueData(data1)

            lastNumber `should be equal to` "0"

            active(src = "test")

            lastNumber `should be equal to` data1
        }
    }

    @Test
    fun `buffer hold should correctly stop the buffer`() {
        StartStopBuffer<String>().run {
            var lastNumber = "0"
            val data1 = randomString()
            val data2 = randomString()
            val data3 = randomString()

            hold()

            subscribe { data ->
                hold()
                lastNumber = data
            }

            enqueueData(data1)
            enqueueData(data2)
            enqueueData(data3)

            lastNumber `should be equal to` "0"

            active(src = "test")

            lastNumber `should be equal to` data1
        }
    }

    @Test
    fun `buffer should be able to handle many events`() {
        StartStopBuffer<String>().run {
            var lastNumber = "0"
            val data1 = randomString()
            val data2 = randomString()
            val data3 = randomString()

            hold()

            subscribe { data ->
                lastNumber = data
            }

            enqueueData(data1)
            enqueueData(data2)
            enqueueData(data3)

            lastNumber `should be equal to` "0"

            active(src = "test")

            lastNumber `should be equal to` data3
        }
    }

    @Test
    fun `it should be possible use buffer with one item per active`() {
        StartStopBuffer<String>().run {
            var lastNumber = "0"
            val data1 = "data1"
            val data2 = "data2"
            val data3 = "data3"

            hold()

            subscribe { data ->
                hold()
                lastNumber = data
            }

            enqueueData(data1)
            enqueueData(data2)
            enqueueData(data3)

            lastNumber `should be equal to` "0"

            active(src = "test")
            active(src = "test")

            lastNumber `should be equal to` data2
        }
    }

    @Test
    fun `when buffer overflows, it should start emitting its items`() {
        StartStopBuffer<String>(bufferLimit = 2).run {
            val initialData = "0"
            var lastNumber = initialData
            val data1 = "data1"
            val data2 = "data2"
            val data3 = "data3"

            hold()

            subscribe { data ->
                lastNumber = data
            }

            enqueueData(data1)
            enqueueData(data2)
            enqueueData(data3)

            // Verifies that overflow happened and items were emitted. If that happened lastNumber was overwritten.
            lastNumber `should not be equal to` initialData
            lastNumber `should be in` listOf(data1, data2, data3)
        }
    }
}
