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

package io.getstream.chat.android.compose.sample.ui.location

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Calendar
import java.util.Date

@Composable
fun DurationDropdownMenu(
    expanded: Boolean,
    onSelect: (duration: Duration) -> Unit,
    onDismiss: () -> Unit,
) {
    DropdownMenu(
        expanded = expanded,
        containerColor = ChatTheme.colors.backgroundElevationElevation1,
        onDismissRequest = onDismiss,
    ) {
        Text(
            modifier = Modifier.padding(16.dp),
            text = "Select a duration for sharing",
            style = ChatTheme.typography.title3Bold,
            color = ChatTheme.colors.textPrimary,
        )
        Duration.entries.forEach { duration ->
            val label = when (duration) {
                Duration.OneMinute -> "1 minute"
                Duration.TenMinutes -> "10 minutes"
                Duration.OneHour -> "1 hour"
            }
            DropdownMenuItem(
                text = { Text(text = label) },
                colors = MenuDefaults.itemColors(textColor = ChatTheme.colors.textSecondary),
                onClick = { onSelect(duration) },
            )
        }
    }
}

@Suppress("MagicNumber")
enum class Duration(val minutes: Int) {
    OneMinute(1),
    TenMinutes(10),
    OneHour(60),
}

fun Duration.asDate(): Date =
    Calendar.getInstance().apply {
        add(Calendar.MINUTE, minutes)
        // Add 1 second to ensure the location is shared for the full duration
        add(Calendar.SECOND, 1)
    }.time

@Preview
@Composable
private fun DurationDropdownMenuPreview() {
    ChatTheme {
        DurationDropdownMenu(
            expanded = true,
            onSelect = {},
            onDismiss = {},
        )
    }
}
