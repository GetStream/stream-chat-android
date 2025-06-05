/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.sample.feature.reminders

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Date
import kotlin.math.abs
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Renders a stylized text label that indicates the status of a message reminder.
 * The label displays the status of the reminder, indicating whether it is overdue or due in a certain duration.
 *
 * @param remindAt The date and time when the reminder is set. If null, it indicates that the reminder is saved for
 * later.
 */
@Composable
fun MessageReminderStatusLabel(remindAt: Date?) {
    val (text, color) = if (remindAt == null) {
        stringResource(R.string.reminders_status_save_for_later) to ChatTheme.colors.primaryAccent
    } else {
        val delta = (remindAt.time - Date().time) / MillisInMinute
        val isOverdue = delta < 0
        val duration = abs(delta).toDuration(DurationUnit.MINUTES)
        if (isOverdue) {
            stringResource(R.string.reminders_status_overdue_by, duration) to ChatTheme.colors.errorAccent
        } else {
            stringResource(R.string.reminders_status_due_in, duration) to ChatTheme.colors.primaryAccent
        }
    }
    Text(
        text = text,
        fontSize = 14.sp,
        color = color,
    )
}

private const val MillisInMinute = 60 * 1000
