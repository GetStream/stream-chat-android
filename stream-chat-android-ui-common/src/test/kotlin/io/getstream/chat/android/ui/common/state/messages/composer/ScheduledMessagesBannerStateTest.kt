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

package io.getstream.chat.android.ui.common.state.messages.composer

import io.getstream.chat.android.models.ScheduledMessageStatus
import io.getstream.chat.android.randomScheduledMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Date

internal class ScheduledMessagesBannerStateTest {

    private val canSchedule = ScheduleMessageState(canSchedule = true, pendingInChannel = 2, maxPerChannel = 10)
    private val limitReached = canSchedule.copy(pendingInChannel = 10)
    private val now = Date(1_799_999_940_000L)
    private val earlier = Date(1_800_000_000_000L)
    private val later = Date(1_800_000_060_000L)
    private val threadId = "thread-id"

    @Test
    fun `failed messages come first, then the limit reached, then the scheduled messages`() {
        val scheduled = randomScheduledMessage(scheduledAt = later)
        val failed = randomScheduledMessage(status = ScheduledMessageStatus.Failed)

        assertEquals(
            ScheduledMessagesBannerState.Failed(1),
            scheduledMessagesBannerState(listOf(scheduled, failed), limitReached, parentMessageId = null, now = now),
        )
        assertEquals(
            ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10),
            scheduledMessagesBannerState(listOf(scheduled), limitReached, parentMessageId = null, now = now),
        )
        assertEquals(
            ScheduledMessagesBannerState.Scheduled(count = 1, nextScheduledAt = later),
            scheduledMessagesBannerState(listOf(scheduled), canSchedule, parentMessageId = null, now = now),
        )
    }

    @Test
    fun `the scheduled messages tell their count and the date of the next one, in any status but failed`() {
        val scheduledMessages = listOf(
            randomScheduledMessage(scheduledAt = later),
            randomScheduledMessage(scheduledAt = earlier, status = ScheduledMessageStatus.PreparingToSchedule),
            randomScheduledMessage(scheduledAt = later, status = ScheduledMessageStatus.Sending),
        )

        assertEquals(
            ScheduledMessagesBannerState.Scheduled(count = 3, nextScheduledAt = earlier, parentMessageId = threadId),
            scheduledMessagesBannerState(scheduledMessages, canSchedule, parentMessageId = threadId, now = now),
        )
    }

    @Test
    fun `scheduled messages whose date has passed are not shown as scheduled`() {
        val pastDue = randomScheduledMessage(scheduledAt = earlier, status = ScheduledMessageStatus.PreparingToSchedule)
        val upcoming = randomScheduledMessage(scheduledAt = later)

        assertEquals(
            ScheduledMessagesBannerState.Scheduled(count = 1, nextScheduledAt = later),
            scheduledMessagesBannerState(listOf(pastDue, upcoming), canSchedule, parentMessageId = null, now = earlier),
        )
        assertNull(scheduledMessagesBannerState(listOf(pastDue), canSchedule, parentMessageId = null, now = earlier))
    }

    @Test
    fun `the limit reached shows even without scheduled messages, as it is for the whole channel`() {
        assertEquals(
            ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10),
            scheduledMessagesBannerState(emptyList(), limitReached, parentMessageId = threadId, now = now),
        )
    }

    @Test
    fun `the banner of a thread is about the thread, except the limit reached, which is about the whole channel`() {
        val failed = randomScheduledMessage(status = ScheduledMessageStatus.Failed)
        val scheduled = randomScheduledMessage(scheduledAt = later)

        assertEquals(
            threadId,
            scheduledMessagesBannerState(listOf(failed), canSchedule, parentMessageId = threadId, now = now)
                ?.parentMessageId,
        )
        assertEquals(
            threadId,
            scheduledMessagesBannerState(listOf(scheduled), canSchedule, parentMessageId = threadId, now = now)
                ?.parentMessageId,
        )
        val limit = scheduledMessagesBannerState(listOf(scheduled), limitReached, parentMessageId = threadId, now = now)
        assertEquals(ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10), limit)
        assertNull(limit?.parentMessageId)
    }

    @Test
    fun `nothing shows without scheduled messages, or without the capability`() {
        assertNull(scheduledMessagesBannerState(emptyList(), canSchedule, parentMessageId = null, now = now))
        assertNull(
            scheduledMessagesBannerState(
                listOf(randomScheduledMessage(status = ScheduledMessageStatus.Failed)),
                limitReached.copy(canSchedule = false),
                parentMessageId = null,
                now = now,
            ),
        )
    }
}
