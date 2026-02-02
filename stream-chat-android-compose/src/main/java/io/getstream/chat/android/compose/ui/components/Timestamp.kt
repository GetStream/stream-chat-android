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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import io.getstream.chat.android.compose.state.DateFormatType
import io.getstream.chat.android.compose.state.DateFormatType.DATE
import io.getstream.chat.android.compose.state.DateFormatType.RELATIVE
import io.getstream.chat.android.compose.state.DateFormatType.TIME
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.helper.DateFormatter
import java.util.Date

/**
 * Represents a timestamp in the app, that's used primarily for channels and messages.
 *
 * @param date The date to show in the timestamp.
 * @param modifier Modifier for styling.
 * @param formatter The formatter that's used to format the date to a String.
 * @param formatType The type of formatting to provide - either a timestamp or a full date. We format the information
 * using [DATE] by default, as it's the most common behavior.
 */
@Composable
public fun Timestamp(
    date: Date?,
    modifier: Modifier = Modifier,
    formatter: DateFormatter = ChatTheme.dateFormatter,
    formatType: DateFormatType = DATE,
    textStyle: TextStyle = ChatTheme.typography.footnote.copy(ChatTheme.colors.textLowEmphasis),
) {
    val timestamp = if (LocalInspectionMode.current) {
        "13:49"
    } else {
        when (formatType) {
            TIME -> formatter.formatTime(date)
            DATE -> formatter.formatDate(date)
            RELATIVE -> formatter.formatRelativeTime(date)
        }
    }

    Text(
        modifier = modifier.testTag("Stream_Timestamp"),
        text = timestamp,
        style = textStyle,
    )
}
