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

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.ScheduleMessageTimeSheetParams
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageError
import io.getstream.chat.android.ui.common.state.messages.composer.ScheduleMessageState
import io.getstream.result.Error
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.util.Date
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
@Config(sdk = [33])
internal class ScheduleMessageTimeSheetContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `after a failed request, a date too soon shows its error and can't be scheduled`() {
        setContent(date = Date(now.time - TimeUnit.MINUTES.toMillis(2)), error = failed)

        composeTestRule.onNodeWithText("Pick a time at least 1 minute from now").assertExists()
        composeTestRule.onNodeWithText("Couldn't schedule message. Try again").assertDoesNotExist()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageConfirm").assertIsNotEnabled()
    }

    @Test
    fun `after a failed request, a valid date shows the failure and can be scheduled again`() {
        setContent(date = Date(now.time + TimeUnit.HOURS.toMillis(1)), error = failed)

        composeTestRule.onNodeWithText("Couldn't schedule message. Try again").assertExists()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageConfirm").assertIsEnabled()
    }

    @Test
    fun `an error of the message shows before an error of the date`() {
        setContent(
            date = Date(now.time - TimeUnit.MINUTES.toMillis(2)),
            error = ScheduleMessageError.CommandNotAllowed,
        )

        composeTestRule.onNodeWithText("Pick a time at least 1 minute from now").assertDoesNotExist()
        composeTestRule.onNodeWithTag("Stream_ScheduleMessageConfirm").assertIsNotEnabled()
    }

    private fun setContent(date: Date, error: ScheduleMessageError?) {
        composeTestRule.setContent {
            ChatTheme {
                ScheduleMessageTimeSheetContent(
                    params = ScheduleMessageTimeSheetParams(
                        message = Message(text = "Heads up"),
                        channelName = "General",
                        initialDate = date,
                        state = ScheduleMessageState(canSchedule = true),
                        error = error,
                        onConfirm = {},
                        onDismiss = {},
                    ),
                    date = date,
                    now = now,
                    onDateClick = {},
                    onTimeClick = {},
                )
            }
        }
    }

    private val now = Date()
    private val failed = ScheduleMessageError.Failed(Error.GenericError("Refused"))
}
