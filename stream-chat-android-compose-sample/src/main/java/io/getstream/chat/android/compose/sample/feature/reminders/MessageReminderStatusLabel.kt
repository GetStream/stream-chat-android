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

package io.getstream.chat.android.compose.sample.feature.reminders

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
    // Ticker running every minute to update the status label
    var ticker by remember { mutableStateOf(Date()) }
    var ticking by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(remindAt) {
        coroutineScope.launch {
            ticking = true
            while (ticking) {
                ticker = Date()
                delay(timeMillis = 60_000) // Update every minute
            }
        }
        onDispose {
            ticking = false
        }
    }

    val (text, color) = if (remindAt == null) {
        stringResource(R.string.reminders_status_save_for_later) to ChatTheme.colors.accentPrimary
    } else {
        val delta = (remindAt.time - ticker.time)
        val isOverdue = delta < 0
        val duration = abs(delta).toDuration(DurationUnit.MILLISECONDS)
        val days = duration.inWholeDays.toInt()
        val hours = (duration.inWholeHours % HoursInDay).toInt()
        val minutes = (duration.inWholeMinutes % MinutesInHour).toInt()
        val seconds = (duration.inWholeSeconds % SecondsInMinute).toInt()
        val durationString = buildString {
            if (days > 0) append("${days}d ")
            if (hours > 0) append("${hours}h ")
            if (minutes > 0) append("${minutes}m ")
            if (seconds > 0) append("${seconds}s")
        }.ifEmpty { "0s" }
        if (isOverdue) {
            stringResource(R.string.reminders_status_overdue_by, durationString) to ChatTheme.colors.accentError
        } else {
            stringResource(R.string.reminders_status_due_in, durationString) to ChatTheme.colors.accentPrimary
        }
    }
    Text(
        text = text,
        fontSize = 14.sp,
        color = color,
    )
}

private const val HoursInDay = 24
private const val MinutesInHour = 60
private const val SecondsInMinute = 60
