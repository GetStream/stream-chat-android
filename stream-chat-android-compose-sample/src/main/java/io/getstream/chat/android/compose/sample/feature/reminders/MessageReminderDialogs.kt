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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import io.getstream.chat.android.compose.sample.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * Displays a dialog with options for editing or deleting a message reminder.
 *
 * @param onEdit Callback invoked when the edit option is selected.
 * @param onDelete Callback invoked when the delete option is selected.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 */
@Composable
fun ReminderOptionsDialog(
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit,
) {
    ReminderDialog(onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.reminders_options),
                color = ChatTheme.colors.textHighEmphasis,
                style = ChatTheme.typography.title3Bold,
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_edit),
                color = ChatTheme.colors.primaryAccent,
                onClick = onEdit,
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_delete),
                color = ChatTheme.colors.errorAccent,
                onClick = onDelete,
            )
        }
    }
}

/**
 * Displays a dialog for selecting a reminder time.
 *
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param onRemindAtSelected Callback invoked when a reminder time is selected.
 */
@Composable
fun CreateReminderDialog(
    onDismiss: () -> Unit,
    onRemindAtSelected: (Date) -> Unit,
) {
    ReminderDialog(onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.reminders_select_reminder_time_title),
                color = ChatTheme.colors.textHighEmphasis,
                style = ChatTheme.typography.title3Bold,
            )
            Text(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
                text = stringResource(R.string.reminders_select_reminder_time_description),
                color = ChatTheme.colors.textLowEmphasis,
                style = ChatTheme.typography.body,
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_2_minutes),
                onClick = { onRemindAtSelected(Date().apply { time += 2.minutes.inWholeMilliseconds }) },
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_5_minutes),
                onClick = { onRemindAtSelected(Date().apply { time += 5.minutes.inWholeMilliseconds }) },
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_1_hour),
                onClick = { onRemindAtSelected(Date().apply { time += 1.hours.inWholeMilliseconds }) },
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_1_day),
                onClick = { onRemindAtSelected(Date().apply { time += 1.days.inWholeMilliseconds }) },
            )
        }
    }
}

/**
 * Displays a dialog with options for editing the reminder time.
 *
 * @param remindAt The current reminder time.
 * @param onRemindAtSelected Callback invoked when the reminder time is changed.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 */
@Composable
fun EditReminderDialog(
    remindAt: Date?,
    onRemindAtSelected: (Date?) -> Unit,
    onDismiss: () -> Unit,
) {
    ReminderDialog(onDismiss) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = stringResource(R.string.reminders_edit_due_date),
                color = ChatTheme.colors.textHighEmphasis,
                style = ChatTheme.typography.title3Bold,
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_2_minutes),
                onClick = { onRemindAtSelected(Date().apply { time += 2.minutes.inWholeMilliseconds }) },
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_5_minutes),
                onClick = { onRemindAtSelected(Date().apply { time += 5.minutes.inWholeMilliseconds }) },
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_1_hour),
                onClick = { onRemindAtSelected(Date().apply { time += 1.hours.inWholeMilliseconds }) },
            )
            HorizontalDivider(color = ChatTheme.colors.borders)
            ReminderOptionItem(
                text = stringResource(R.string.reminders_remind_in_1_day),
                onClick = { onRemindAtSelected(Date().apply { time += 1.days.inWholeMilliseconds }) },
            )
            if (remindAt != null) {
                HorizontalDivider(color = ChatTheme.colors.borders)
                ReminderOptionItem(
                    text = stringResource(R.string.reminders_clear_due_date),
                    onClick = { onRemindAtSelected(null) },
                )
            }
        }
    }
}

@Composable
private fun ReminderDialog(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = ChatTheme.colors.barsBackground)) {
            content()
        }
    }
}

/**
 * Displays a single option item in the dialog.
 *
 * @param text The text to be displayed.
 * @param onClick Callback invoked when the item is clicked.
 * @param color The color of the text.
 */
@Composable
private fun ReminderOptionItem(
    text: String,
    onClick: () -> Unit,
    color: Color = ChatTheme.colors.primaryAccent,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = null,
                indication = ripple(),
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = text,
            color = color,
        )
    }
}
