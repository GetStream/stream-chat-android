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

package io.getstream.chat.android.compose.sample.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.PushPreference
import io.getstream.chat.android.models.PushPreferenceLevel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfilePushPreferencesScreen(
    preferences: PushPreference,
    onSavePreferences: (PushPreferenceLevel) -> Unit,
    onSnoozeNotifications: (Date) -> Unit,
) {
    var selectedLevel by remember { mutableStateOf(preferences.level ?: PushPreferenceLevel.all) }
    var isTemporaryDisabled by remember { mutableStateOf(preferences.disabledUntil != null) }

    // Initialize disable until date when temporary disable is enabled
    var disableUntilDate by remember {
        val fallback = Calendar.getInstance().apply {
            add(Calendar.HOUR_OF_DAY, 1)
        }.time
        mutableStateOf(preferences.disabledUntil ?: fallback)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(ChatTheme.colors.appBackground)
            .padding(16.dp),
    ) {
        // Notification Level Section
        NotificationLevelSection(
            selectedLevel = selectedLevel,
            isEnabled = !isTemporaryDisabled,
            onLevelSelected = { level -> if (!isTemporaryDisabled) selectedLevel = level },
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Temporary Disable Section
        TemporaryDisableSection(
            isTemporaryDisabled = isTemporaryDisabled,
            disableUntilDate = disableUntilDate,
            onTemporaryDisableToggled = { isTemporaryDisabled = !isTemporaryDisabled },
            onDateTimeSelected = { newDate -> disableUntilDate = newDate },
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Action Buttons
        ActionButtons(
            isTemporaryDisabled = isTemporaryDisabled,
            onSavePreferences = { onSavePreferences(selectedLevel) },
            onSnoozeNotifications = { onSnoozeNotifications(disableUntilDate) },
        )
    }
}

@Composable
private fun NotificationLevelSection(
    selectedLevel: PushPreferenceLevel,
    isEnabled: Boolean,
    onLevelSelected: (PushPreferenceLevel) -> Unit,
) {
    Text(
        text = "NOTIFICATION LEVEL",
        style = ChatTheme.typography.footnote.copy(
            color = if (isEnabled) ChatTheme.colors.textLowEmphasis else ChatTheme.colors.disabled,
            fontWeight = FontWeight.Medium,
        ),
        modifier = Modifier.padding(bottom = 16.dp),
    )

    // All Notifications Option
    NotificationLevelOption(
        title = "All Notifications",
        subtitle = "Receive notifications for all messages",
        isSelected = selectedLevel == PushPreferenceLevel.all,
        isEnabled = isEnabled,
        onSelect = { onLevelSelected(PushPreferenceLevel.all) },
    )

    // Mentions Only Option
    NotificationLevelOption(
        title = "Mentions Only",
        subtitle = "Only receive notifications when mentioned",
        isSelected = selectedLevel == PushPreferenceLevel.mentions,
        isEnabled = isEnabled,
        onSelect = { onLevelSelected(PushPreferenceLevel.mentions) },
    )

    // No Notifications Option
    NotificationLevelOption(
        title = "No Notifications",
        subtitle = "Disable all push notifications",
        isSelected = selectedLevel == PushPreferenceLevel.none,
        isEnabled = isEnabled,
        onSelect = { onLevelSelected(PushPreferenceLevel.none) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TemporaryDisableSection(
    isTemporaryDisabled: Boolean,
    disableUntilDate: Date,
    onTemporaryDisableToggled: () -> Unit,
    onDateTimeSelected: (Date) -> Unit,
) {
    Text(
        text = "TEMPORARY DISABLE",
        style = ChatTheme.typography.footnote.copy(
            color = ChatTheme.colors.textLowEmphasis,
            fontWeight = FontWeight.Medium,
        ),
        modifier = Modifier.padding(bottom = 16.dp),
    )

    // Temporary Disable Toggle
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clickable { onTemporaryDisableToggled() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "Disable notifications temporarily",
            style = ChatTheme.typography.bodyBold.copy(
                color = ChatTheme.colors.textHighEmphasis,
            ),
        )

        Switch(
            checked = isTemporaryDisabled,
            onCheckedChange = null, // Handled by row click
            colors = SwitchDefaults.colors(
                checkedTrackColor = ChatTheme.colors.infoAccent,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = ChatTheme.colors.disabled,
                uncheckedThumbColor = Color.White,
            ),
        )
    }

    // Disable Until Time Display
    if (isTemporaryDisabled) {
        DateTimeSelector(
            selectedDate = disableUntilDate,
            onDateTimeSelected = onDateTimeSelected,
        )
    }
}

@Composable
private fun ActionButtons(
    isTemporaryDisabled: Boolean,
    onSavePreferences: () -> Unit,
    onSnoozeNotifications: () -> Unit,
) {
    Button(
        onClick = if (isTemporaryDisabled) onSnoozeNotifications else onSavePreferences,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isTemporaryDisabled) {
                Color(color = 0xFFFF8A65) // Orange color when temporary disable is ON
            } else {
                ChatTheme.colors.primaryAccent // Blue color when temporary disable is OFF
            },
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        if (isTemporaryDisabled) {
            Text(
                text = "🔔 Snooze Notifications",
                style = ChatTheme.typography.bodyBold.copy(
                    color = Color.White,
                    fontSize = 16.sp,
                ),
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.stream_compose_ic_checkmark),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Save Preferences",
                    style = ChatTheme.typography.bodyBold.copy(
                        color = Color.White,
                        fontSize = 16.sp,
                    ),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("LongMethod")
@Composable
private fun DateTimeSelector(
    selectedDate: Date,
    onDateTimeSelected: (Date) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val formatter = SimpleDateFormat("d.M.yyyy 'at' HH:mm", Locale.getDefault())
    val displayTime = formatter.format(selectedDate)

    // Display Row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Disable until:",
            style = ChatTheme.typography.bodyBold.copy(
                color = ChatTheme.colors.textHighEmphasis,
            ),
        )

        Text(
            text = displayTime,
            style = ChatTheme.typography.body.copy(
                color = ChatTheme.colors.primaryAccent,
            ),
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.time,
        )

        DatePickerDialog(
            modifier = Modifier.padding(16.dp),
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedMillis ->
                            val selectedCalendar = Calendar.getInstance()
                            selectedCalendar.timeInMillis = selectedMillis

                            // Keep the current time, only update date
                            val currentCalendar = Calendar.getInstance()
                            currentCalendar.time = selectedDate

                            selectedCalendar.set(Calendar.HOUR_OF_DAY, currentCalendar.get(Calendar.HOUR_OF_DAY))
                            selectedCalendar.set(Calendar.MINUTE, currentCalendar.get(Calendar.MINUTE))

                            onDateTimeSelected(selectedCalendar.time)
                        }
                        showDatePicker = false
                        showTimePicker = true
                    },
                ) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(Calendar.MINUTE),
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val selectedCalendar = Calendar.getInstance()
                        selectedCalendar.time = selectedDate
                        selectedCalendar.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        selectedCalendar.set(Calendar.MINUTE, timePickerState.minute)
                        selectedCalendar.set(Calendar.SECOND, 0)

                        onDateTimeSelected(selectedCalendar.time)
                        showTimePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            },
        )
    }
}

@Composable
private fun NotificationLevelOption(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    onSelect: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .clickable(enabled = isEnabled, onClick = onSelect)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(
                selected = isSelected,
                onClick = null, // Handled by row click
                enabled = isEnabled,
                colors = RadioButtonDefaults.colors(
                    selectedColor = if (isEnabled) ChatTheme.colors.primaryAccent else ChatTheme.colors.disabled,
                    unselectedColor = ChatTheme.colors.disabled,
                    disabledSelectedColor = ChatTheme.colors.disabled,
                    disabledUnselectedColor = ChatTheme.colors.disabled,
                ),
                modifier = Modifier.padding(end = 12.dp),
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = ChatTheme.typography.bodyBold.copy(
                        color = if (isEnabled) ChatTheme.colors.textHighEmphasis else ChatTheme.colors.disabled,
                    ),
                )
                Text(
                    text = subtitle,
                    style = ChatTheme.typography.footnote.copy(
                        color = if (isEnabled) ChatTheme.colors.textLowEmphasis else ChatTheme.colors.disabled,
                    ),
                )
            }

            if (isSelected && isEnabled) {
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp),
                ) {
                    Text(
                        text = "✓",
                        style = ChatTheme.typography.body.copy(
                            color = ChatTheme.colors.primaryAccent,
                            fontSize = 18.sp,
                        ),
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfilePushPreferencesScreenEnabledPreview() {
    ChatTheme {
        UserProfilePushPreferencesScreen(
            preferences = PushPreference(
                level = PushPreferenceLevel.mentions,
                disabledUntil = null,
            ),
            onSavePreferences = { },
            onSnoozeNotifications = { },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfilePushPreferencesScreenDisabledPreview() {
    ChatTheme {
        UserProfilePushPreferencesScreen(
            preferences = PushPreference(
                level = PushPreferenceLevel.mentions,
                disabledUntil = Date().apply { time += 1.hours.inWholeMilliseconds },
            ),
            onSavePreferences = { },
            onSnoozeNotifications = { },
        )
    }
}
