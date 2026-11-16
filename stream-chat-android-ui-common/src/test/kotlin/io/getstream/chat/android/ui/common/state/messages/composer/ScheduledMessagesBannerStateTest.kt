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
    private val earlier = Date(1_800_000_000_000L)
    private val later = Date(1_800_000_060_000L)

    @Test
    fun `failed messages come first, then the limit reached, then the scheduled messages`() {
        val scheduled = randomScheduledMessage(scheduledAt = later)
        val failed = randomScheduledMessage(status = ScheduledMessageStatus.Failed)

        assertEquals(
            ScheduledMessagesBannerState.Failed(1),
            scheduledMessagesBannerState(listOf(scheduled, failed), limitReached, isInThread = false),
        )
        assertEquals(
            ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10),
            scheduledMessagesBannerState(listOf(scheduled), limitReached, isInThread = false),
        )
        assertEquals(
            ScheduledMessagesBannerState.Scheduled(count = 1, nextScheduledAt = later, isInThread = false),
            scheduledMessagesBannerState(listOf(scheduled), canSchedule, isInThread = false),
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
            ScheduledMessagesBannerState.Scheduled(count = 3, nextScheduledAt = earlier, isInThread = true),
            scheduledMessagesBannerState(scheduledMessages, canSchedule, isInThread = true),
        )
    }

    @Test
    fun `the limit reached shows even without scheduled messages, as it is for the whole channel`() {
        assertEquals(
            ScheduledMessagesBannerState.LimitReached(count = 10, limit = 10),
            scheduledMessagesBannerState(emptyList(), limitReached, isInThread = true),
        )
    }

    @Test
    fun `nothing shows without scheduled messages, or without the capability`() {
        assertNull(scheduledMessagesBannerState(emptyList(), canSchedule, isInThread = false))
        assertNull(
            scheduledMessagesBannerState(
                listOf(randomScheduledMessage(status = ScheduledMessageStatus.Failed)),
                limitReached.copy(canSchedule = false),
                isInThread = false,
            ),
        )
    }
}
