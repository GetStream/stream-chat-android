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

import io.getstream.chat.android.models.Message
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Date
import java.util.concurrent.TimeUnit

internal class ScheduleMessageStateTest {

    private val now = Date(1_800_000_000_000L)

    @Test
    fun `a date less than the min lead time from now is too soon`() {
        val state = ScheduleMessageState(minLeadTimeSeconds = 60)

        assertEquals(ScheduleMessageError.TooSoon(60), state.dateError(now.plusSeconds(59), now))
        assertEquals(ScheduleMessageError.TooSoon(60), state.dateError(now.plusSeconds(-60), now))
        assertNull(state.dateError(now.plusSeconds(60), now))
    }

    @Test
    fun `a date more than the max lead time from now is too far`() {
        val maxLeadTime = TimeUnit.DAYS.toSeconds(30).toInt()
        val state = ScheduleMessageState(maxLeadTimeSeconds = maxLeadTime)

        assertNull(state.dateError(now.plusSeconds(maxLeadTime.toLong()), now))
        assertEquals(
            ScheduleMessageError.TooFar(maxLeadTime),
            state.dateError(now.plusSeconds(maxLeadTime + 1L), now),
        )
    }

    @Test
    fun `the default lead times are one minute and 120 days`() {
        val state = ScheduleMessageState()

        assertEquals(60, state.minLeadTimeSeconds)
        assertEquals(TimeUnit.DAYS.toSeconds(120).toInt(), state.maxLeadTimeSeconds)
    }

    @Test
    fun `a message that starts with a command can't be scheduled in a channel with commands`() {
        val withCommands = ScheduleMessageState(hasCommands = true)
        val withoutCommands = ScheduleMessageState(hasCommands = false)
        val command = Message(text = "/giphy launch day")

        assertEquals(ScheduleMessageError.CommandNotAllowed, withCommands.messageError(command))
        assertNull(withoutCommands.messageError(command))
        assertNull(withCommands.messageError(Message(text = "launch day /giphy")))
    }

    @Test
    fun `the channel limit is reached with as many pending messages as the max per channel`() {
        val below = ScheduleMessageState(pendingInChannel = 9, maxPerChannel = 10)
        val reached = ScheduleMessageState(pendingInChannel = 10, maxPerChannel = 10)
        val unknown = ScheduleMessageState(pendingInChannel = 10, maxPerChannel = null)

        assertFalse(below.isChannelLimitReached)
        assertTrue(reached.isChannelLimitReached)
        assertFalse(unknown.isChannelLimitReached)
        assertNull(below.messageError(Message(text = "Hi")))
        assertEquals(ScheduleMessageError.ChannelLimitReached(10), reached.messageError(Message(text = "Hi")))
    }

    @Test
    fun `the user limit is reached with as many pending messages as the max per user`() {
        val reached = ScheduleMessageState(pendingForUser = 100, maxPerUser = 100)

        assertTrue(reached.isUserLimitReached)
        assertEquals(ScheduleMessageError.UserLimitReached(100), reached.messageError(Message(text = "Hi")))
        assertFalse(ScheduleMessageState(pendingForUser = 99, maxPerUser = 100).isUserLimitReached)
    }

    @Test
    fun `a command is reported before a limit`() {
        val state = ScheduleMessageState(
            hasCommands = true,
            pendingInChannel = 10,
            maxPerChannel = 10,
            pendingForUser = 100,
            maxPerUser = 100,
        )

        assertEquals(ScheduleMessageError.CommandNotAllowed, state.messageError(Message(text = "/mute")))
        assertEquals(ScheduleMessageError.ChannelLimitReached(10), state.messageError(Message(text = "mute")))
    }

    private fun Date.plusSeconds(seconds: Long): Date = Date(time + TimeUnit.SECONDS.toMillis(seconds))
}
