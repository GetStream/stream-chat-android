/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package com.getstream.sdk.chat.utils

import io.getstream.chat.android.test.TestCoroutineExtension
import io.getstream.chat.android.test.randomString
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

@ExperimentalCoroutinesApi
internal class StartStopBufferTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    @Test
    fun `data should only be propagated when enqueueData and subscribe are done`() {
        StartStopBuffer<String>().run {
            val data = randomString()
            var resultText = ""

            active()
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

            active()

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

            active()

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

            active()

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

            active()
            active()

            lastNumber `should be equal to` data2
        }
    }
}
