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

package io.getstream.chat.android.client.internal.state.event.handler.internal.batch

import io.getstream.chat.android.client.events.ChatEvent
import io.getstream.chat.android.client.test.randomNewMessageEvent
import io.getstream.chat.android.test.TestCoroutineExtension
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be greater than`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension

internal class SocketEventCollectorTest {

    companion object {
        @JvmField
        @RegisterExtension
        val testCoroutines = TestCoroutineExtension()
    }

    private lateinit var eventCollector: SocketEventCollector
    private lateinit var collected: MutableStateFlow<List<BatchEvent>>

    @BeforeEach
    fun setUp() {
        collected = MutableStateFlow(emptyList())
        eventCollector = SocketEventCollector(
            scope = testCoroutines.scope,
            timeout = 150,
            timeLimit = 300,
            itemCountLimit = 10,
            now = { testCoroutines.scope.testScheduler.currentTime },
        ) {
            collected.value += it
        }
    }

    @Test
    fun `test item count limit`() = runTest {
        repeat(20) {
            val event = randomNewMessageEvent()
            eventCollector.collect(event)
            delay(10)
        }

        collected.value.size `should be equal to` 2
        val first = collected.value.first()
        val second = collected.value.last()
        first.sortedEvents.size `should be equal to` 10
        second.sortedEvents.size `should be equal to` 10

        val validateBatch: (BatchEvent) -> Unit = { batch ->
            var prevEvent: ChatEvent? = null
            for (event in batch.sortedEvents) {
                if (prevEvent == null) {
                    prevEvent = event
                    continue
                }
                event.createdAt `should be greater than` prevEvent.createdAt
            }
        }

        validateBatch(first)
        validateBatch(second)
    }

    @Test
    fun `test timeout`() = runTest {
        val event = randomNewMessageEvent()
        eventCollector.collect(event)

        delay(200)

        collected.value.size `should be equal to` 1
        collected.value.first().sortedEvents.size `should be equal to` 1
        collected.value.first().sortedEvents.first() `should be equal to` event
    }

    @Test
    fun `test time limit`() = runTest {
        // <1> - 140ms - <2> - 140ms - <3> - 140ms - <4>
        // 420ms > 300ms => time limit exceeds
        // <5> - 150ms
        // after 150ms timeout is triggered
        delay(100)
        repeat(5) {
            eventCollector.collect(randomNewMessageEvent())
            delay(140)
        }

        delay(200)

        collected.value.size `should be equal to` 2
        collected.value.first().size `should be equal to` 4
        collected.value.last().size `should be equal to` 1
    }
}
