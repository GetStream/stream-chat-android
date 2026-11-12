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

import app.cash.paparazzi.Paparazzi
import com.android.ide.common.rendering.api.SessionParams
import io.getstream.chat.android.compose.ui.PIXEL_4A_HDPI
import io.getstream.chat.android.compose.ui.PaparazziComposeTest
import io.getstream.chat.android.compose.ui.messages.composer.internal.MessageComposerSendButtonWithSchedule
import org.junit.Rule
import org.junit.Test

internal class ScheduleMessageSheetsTest : PaparazziComposeTest {

    @get:Rule
    override val paparazzi = Paparazzi(
        deviceConfig = PIXEL_4A_HDPI,
        renderingMode = SessionParams.RenderingMode.SHRINK,
    )

    @Test
    fun `schedule sheet`() {
        snapshotWithDarkMode { ScheduleMessageSheetContent() }
    }

    @Test
    fun `schedule sheet with the channel limit reached`() {
        snapshotWithDarkMode { ScheduleMessageSheetLimitReachedContent() }
    }

    @Test
    fun `custom time sheet`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetContent() }
    }

    @Test
    fun `custom time sheet without scheduled messages in the channel`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetWithoutCountContent() }
    }

    @Test
    fun `custom time sheet with a date too soon`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetTooSoonContent() }
    }

    @Test
    fun `custom time sheet with a date too far`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetTooFarContent() }
    }

    @Test
    fun `custom time sheet with the user limit reached`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetUserLimitContent() }
    }

    @Test
    fun `custom time sheet with a command`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetCommandContent() }
    }

    @Test
    fun `custom time sheet with a failed request`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetFailedContent() }
    }

    @Test
    fun `custom time sheet for a reply`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetReplyContent() }
    }

    @Test
    fun `reschedule sheet`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetRescheduleContent() }
    }

    @Test
    fun `reschedule sheet with a date too soon`() {
        snapshotWithDarkMode { ScheduleMessageTimeSheetRescheduleTooSoonContent() }
    }

    @Test
    fun `send button with a long click`() {
        snapshotWithDarkModeRow { MessageComposerSendButtonWithSchedule() }
    }
}
