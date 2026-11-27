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

package io.getstream.chat.android.ui.common.feature.messages.composer.scheduled

import app.cash.turbine.test
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ScheduledMessage
import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.randomMessage
import io.getstream.chat.android.randomScheduledMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ScheduledMessagesSentNowTest {

    private val queued = randomScheduledMessage(status = ScheduledMessageStatus.PreparingToSchedule)
    private val scheduled = randomScheduledMessage(status = ScheduledMessageStatus.Scheduled)
    private val sentMessage = randomMessage(id = queued.id)

    @Test
    fun `a queued message that leaves the scheduled messages and is in the channel was sent now`() = runTest {
        val scheduledMessages = MutableStateFlow(listOf(queued, scheduled))

        scheduledMessages.sentNow(findMessage(sentMessage)).test {
            scheduledMessages.value = listOf(scheduled)

            assertEquals(sentMessage, awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun `a queued message deleted before it was sent is not sent now`() = runTest {
        val scheduledMessages = MutableStateFlow(listOf(queued))

        scheduledMessages.sentNow(findMessage()).test {
            scheduledMessages.value = emptyList()

            expectNoEvents()
        }
    }

    @Test
    fun `a message the server had scheduled is sent at its date, not now`() = runTest {
        val scheduledMessages = MutableStateFlow(listOf(scheduled))
        val message = randomMessage(id = scheduled.id)

        scheduledMessages.sentNow(findMessage(message)).test {
            scheduledMessages.value = emptyList()

            expectNoEvents()
        }
    }

    @Test
    fun `a message queued while it is collected is sent now when it leaves the scheduled messages`() = runTest {
        val scheduledMessages = MutableStateFlow<List<ScheduledMessage>>(emptyList())

        scheduledMessages.sentNow(findMessage(sentMessage)).test {
            scheduledMessages.value = listOf(queued)
            scheduledMessages.value = emptyList()

            assertEquals(sentMessage, awaitItem())
            expectNoEvents()
        }
    }

    private fun findMessage(vararg messages: Message): suspend (String) -> Message? =
        { id -> messages.find { it.id == id } }
}
