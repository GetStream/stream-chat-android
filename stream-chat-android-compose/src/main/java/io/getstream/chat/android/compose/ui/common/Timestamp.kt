package io.getstream.chat.android.compose.ui.common

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.getstream.sdk.chat.utils.DateFormatter
import com.getstream.sdk.chat.utils.formatDate
import com.getstream.sdk.chat.utils.formatTime
import io.getstream.chat.android.compose.state.DateFormatType
import io.getstream.chat.android.compose.state.DateFormatType.DATE
import io.getstream.chat.android.compose.state.DateFormatType.TIME
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Date

/**
 * Represents a timestamp in the app, that's used primarily for channels and messages.
 *
 * @param date - The date to show in the timestamp.
 * @param modifier - Modifier for styling.
 * @param formatter - The formatter that's used to format the date to a String.
 * @param formatType - The type of formatting to provide - either a timestamp or a full date. We format the information
 * using [DATE] by default, as it's the most common behavior.
 * */
@Composable
public fun Timestamp(
    date: Date?,
    modifier: Modifier = Modifier,
    formatter: DateFormatter = ChatTheme.dateFormatter,
    formatType: DateFormatType = DATE,
) {
    val timestamp = when (formatType) {
        TIME -> formatter.formatTime(date)
        DATE -> formatter.formatDate(date)
    }

    Text(
        modifier = modifier,
        text = timestamp,
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textLowEmphasis
    )
}
