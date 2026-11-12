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

package io.getstream.chat.android.compose.ui.messages.composer.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_4A_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerSendButton
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageSheetParams
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageTimeSheetParams
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.feature.messages.composer.scheduled.scheduleMessageQuickDates
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageError
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import io.getstream.result.Error
import org.junit.Rule
import org.junit.Test
import java.util.Date
import java.util.TimeZone

internal class ScheduleMessageSheetsTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_4A_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `schedule sheet`() {
        snapshotWithDarkMode {
            QuickSheet(state = ScheduleMessageState(canSchedule = true, pendingInChannel = 2, maxPerChannel = 10))
        }
    }

    @Test
    fun `schedule sheet with the channel limit reached`() {
        snapshotWithDarkMode {
            QuickSheet(state = ScheduleMessageState(canSchedule = true, pendingInChannel = 10, maxPerChannel = 10))
        }
    }

    @Test
    fun `custom time sheet`() {
        snapshotWithDarkMode {
            TimeSheet(state = ScheduleMessageState(canSchedule = true, pendingInChannel = 2, maxPerChannel = 10))
        }
    }

    @Test
    fun `custom time sheet without scheduled messages in the channel`() {
        snapshotWithDarkMode {
            TimeSheet(state = ScheduleMessageState(canSchedule = true, maxPerChannel = 10))
        }
    }

    @Test
    fun `custom time sheet with a date too soon`() {
        snapshotWithDarkMode {
            TimeSheet(state = ScheduleMessageState(canSchedule = true), date = Date(Now.time + 30_000))
        }
    }

    @Test
    fun `custom time sheet with a date too far`() {
        snapshotWithDarkMode {
            TimeSheet(state = ScheduleMessageState(canSchedule = true, maxLeadTimeSeconds = 3600))
        }
    }

    @Test
    fun `custom time sheet with the user limit reached`() {
        snapshotWithDarkMode {
            TimeSheet(
                state = ScheduleMessageState(canSchedule = true, pendingForUser = 100, maxPerUser = 100),
                error = ScheduleMessageError.UserLimitReached(100),
            )
        }
    }

    @Test
    fun `custom time sheet with a command`() {
        snapshotWithDarkMode {
            TimeSheet(
                state = ScheduleMessageState(canSchedule = true, hasCommands = true),
                message = Message(text = "/giphy launch day"),
                error = ScheduleMessageError.CommandNotAllowed,
            )
        }
    }

    @Test
    fun `custom time sheet with a failed request`() {
        snapshotWithDarkMode {
            TimeSheet(
                state = ScheduleMessageState(canSchedule = true),
                error = ScheduleMessageError.Failed(Error.GenericError("Failed")),
            )
        }
    }

    @Test
    fun `custom time sheet for a reply`() {
        snapshotWithDarkMode {
            TimeSheet(
                state = ScheduleMessageState(canSchedule = true),
                message = Message(
                    text = "@Maya Ross, can you check the final plan?",
                    replyTo = Message(user = User(id = "alex", name = "Alex Ross")),
                ),
            )
        }
    }

    @Test
    fun `reschedule sheet`() {
        snapshotWithDarkMode {
            TimeSheet(state = ScheduleMessageState(canSchedule = true, pendingInChannel = 2), scheduledAt = Tomorrow)
        }
    }

    @Test
    fun `reschedule sheet with a date too soon`() {
        snapshotWithDarkMode {
            TimeSheet(
                state = ScheduleMessageState(canSchedule = true),
                date = Date(Now.time + 30_000),
                scheduledAt = Tomorrow,
            )
        }
    }

    @Test
    fun `send button with a long click`() {
        snapshotWithDarkModeRow {
            MessageComposerSendButton(onClick = {}, onLongClick = {}, onLongClickLabel = "Schedule message")
        }
    }

    @Composable
    private fun QuickSheet(state: ScheduleMessageState) {
        Box(modifier = Modifier.background(ChatTheme.colors.backgroundCoreElevation1)) {
            ScheduleMessageSheetContent(
                params = ScheduleMessageSheetParams(
                    dates = scheduleMessageQuickDates(Now, Utc),
                    state = state,
                    onDateClick = {},
                    onCustomTimeClick = {},
                    onDismiss = {},
                ),
                now = Now,
                timeZone = Utc,
            )
        }
    }

    @Composable
    private fun TimeSheet(
        state: ScheduleMessageState,
        message: Message = Message(text = "Heads up: the launch moved to Monday. I'll post the final plan there."),
        date: Date = Tomorrow,
        error: ScheduleMessageError? = null,
        scheduledAt: Date? = null,
    ) {
        Box(modifier = Modifier.background(ChatTheme.colors.backgroundCoreElevation1)) {
            ScheduleMessageTimeSheetContent(
                params = ScheduleMessageTimeSheetParams(
                    message = message,
                    channelName = "Maya Ross",
                    initialDate = date,
                    state = state,
                    error = error,
                    onConfirm = {},
                    onDismiss = {},
                    scheduledAt = scheduledAt,
                ),
                date = date,
                now = Now,
                onDateClick = {},
                onTimeClick = {},
                timeZone = Utc,
            )
        }
    }

    private companion object {
        val Utc: TimeZone = TimeZone.getTimeZone("UTC")

        /** Saturday, January 16, 2027 at 10:00 UTC. */
        val Now = Date(1_800_093_600_000L)

        /** Sunday, January 17, 2027 at 09:00 UTC. */
        val Tomorrow = Date(1_800_176_400_000L)
    }
}
