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

package io.getstream.chat.android.state.internal

import io.getstream.chat.android.client.events.TypingStartEvent
import io.getstream.chat.android.client.test.randomTypingStartEvent
import io.getstream.chat.android.models.TypingEvent
import io.getstream.chat.android.randomUser
import io.getstream.chat.android.state.plugin.logic.channel.internal.TypingEventPruner
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.Date

@OptIn(ExperimentalCoroutinesApi::class)
internal class TypingEventPrunerTest {

    private val channelId = "channelId"

    @Test
    fun `Given null TypingStartEvent with a userId Should remove existing one with the same userId`() = runTest {
        val onTypingUpdateListenerMock: (
            rawTypingEvents: Map<String, TypingStartEvent>,
            typingEvent: TypingEvent,
        ) -> Unit = mock()

        val typingEventPruner =
            TypingEventPruner(channelId = channelId, coroutineScope = this, onUpdated = onTypingUpdateListenerMock)

        val typingStartEvent = randomTypingStartEvent()

        // Should add the typing event
        typingEventPruner.processEvent(typingStartEvent.user.id, typingStartEvent)
        // Should remove the previously added typing event
        typingEventPruner.processEvent(typingStartEvent.user.id, null)

        verify(onTypingUpdateListenerMock, times(1)).invoke(mapOf(), TypingEvent(channelId, listOf()))
    }

    @Test
    fun `Given typing events are added or removed Should notify listener`() = runTest {
        val onTypingUpdateListenerMock: (
            rawTypingEvents: Map<String, TypingStartEvent>,
            typingEvent: TypingEvent,
        ) -> Unit = mock()

        val typingEventPruner =
            TypingEventPruner(
                channelId = channelId,
                coroutineScope = this,
                onUpdated = onTypingUpdateListenerMock,
            )

        val typingStartEvent = randomTypingStartEvent()

        // Should add the typing event
        typingEventPruner.processEvent(typingStartEvent.user.id, typingStartEvent)
        // Should remove the previously added typing event
        typingEventPruner.processEvent(typingStartEvent.user.id, null)

        // Called once after event addition, once after removal
        verify(onTypingUpdateListenerMock, times(2)).invoke(any(), any())
    }

    @Test
    fun `Given multiple typing event from the same user Should output a collection with a single latest typing event`() =
        runTest {
            val onTypingUpdateListenerMock: (
                rawTypingEvents: Map<String, TypingStartEvent>,
                typingEvent: TypingEvent,
            ) -> Unit = mock()

            val typingEventPruner =
                TypingEventPruner(channelId = channelId, coroutineScope = this, onUpdated = onTypingUpdateListenerMock)

            val firstTypingStartEvent = randomTypingStartEvent()

            // Create a copy of the first event by simulating an event from the same user at a later date
            val advancedStartEventCreationDate = Date(firstTypingStartEvent.createdAt.time + 100)
            val secondTypingStartEvent = firstTypingStartEvent.copy(createdAt = advancedStartEventCreationDate)

            // Set both events
            typingEventPruner.processEvent(firstTypingStartEvent.user.id, firstTypingStartEvent)
            typingEventPruner.processEvent(secondTypingStartEvent.user.id, secondTypingStartEvent)

            verify(onTypingUpdateListenerMock).invoke(
                mapOf(
                    Pair(
                        secondTypingStartEvent.user.id,
                        secondTypingStartEvent,
                    ),
                ),
                TypingEvent(
                    channelId,
                    listOf(secondTypingStartEvent.user),
                ),
            )
        }

    @Test
    fun `Given typing events that are not manually cancelled Should be automatically cleaned by TypingEventPruner`() =
        runTest {
            val onTypingUpdateListenerMock: (
                rawTypingEvents: Map<String, TypingStartEvent>,
                typingEvent: TypingEvent,
            ) -> Unit = mock()

            val prunerDelay = TypingEventPruner.DEFAULT_DELAY_TIME_MS
            val typingEventPruner =
                TypingEventPruner(
                    channelId = channelId,
                    delayTimeMs = prunerDelay,
                    coroutineScope = this,
                    onUpdated = onTypingUpdateListenerMock,
                )

            val firstTypingStartEvent = randomTypingStartEvent(user = randomUser(id = "User1"))
            val secondTypingStartEvent = randomTypingStartEvent(user = randomUser(id = "User2"))

            typingEventPruner.processEvent(firstTypingStartEvent.user.id, firstTypingStartEvent)
            typingEventPruner.processEvent(secondTypingStartEvent.user.id, secondTypingStartEvent)

            // Time is advanced by slightly more than it takes for the pruner to remove stale events
            advanceTimeBy(prunerDelay + 1)

            verify(onTypingUpdateListenerMock, times(1)).invoke(mapOf(), TypingEvent(channelId, listOf()))
        }

    @Test
    fun `Given multiple typing start events from the same user Should restart the delay timer`() {
        runTest {
            val onTypingUpdateListenerMock: (
                rawTypingEvents: Map<String, TypingStartEvent>,
                typingEvent: TypingEvent,
            ) -> Unit = mock()

            val typingEventPruner =
                TypingEventPruner(channelId = channelId, coroutineScope = this, onUpdated = onTypingUpdateListenerMock)
            val timeBetweenTwoMessages = 2500L

            val firstTypingStartEvent = randomTypingStartEvent()

            // Create a copy of the first event by simulating an event from the same user at a later date
            val advancedStartEventCreationDate = Date(firstTypingStartEvent.createdAt.time + timeBetweenTwoMessages)
            val secondTypingStartEvent = firstTypingStartEvent.copy(createdAt = advancedStartEventCreationDate)

            // Send two typing events by the same user with a delay between them
            typingEventPruner.processEvent(firstTypingStartEvent.user.id, firstTypingStartEvent)
            advanceTimeBy(timeBetweenTwoMessages)
            typingEventPruner.processEvent(secondTypingStartEvent.user.id, secondTypingStartEvent)

            // Advance the virtual clock by slightly less than the added time
            advanceTimeBy(TypingEventPruner.DEFAULT_DELAY_TIME_MS - 1)

            // Output should still contain the event
            verify(onTypingUpdateListenerMock, times(1)).invoke(
                mapOf(
                    Pair(
                        secondTypingStartEvent.user.id,
                        secondTypingStartEvent,
                    ),
                ),
                TypingEvent(
                    channelId,
                    listOf(secondTypingStartEvent.user),
                ),
            )

            // Advance the virtual clock by slightly more than the added time
            advanceTimeBy(TypingEventPruner.DEFAULT_DELAY_TIME_MS + 1)

            // Output should contain no events
            verify(onTypingUpdateListenerMock, times(1)).invoke(mapOf(), TypingEvent(channelId, listOf()))
        }
    }
}
