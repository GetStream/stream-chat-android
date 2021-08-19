package io.getstream.chat.android.compose.ui.common

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.getstream.sdk.chat.utils.formatDate
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import java.util.Date

/**
 * Represents a timestamp in the app, that's used primarily for channels and messages.
 *
 * @param date - The date to show in the timestamp.
 * @param modifier - Modifier for styling.
 * */
@Composable
public fun Timestamp(
    date: Date?,
    modifier: Modifier = Modifier,
) {
    val formatter = ChatTheme.configuration.dateFormatter

    Text(
        modifier = modifier,
        text = formatter.formatDate(date),
        style = ChatTheme.typography.footnote,
        color = ChatTheme.colors.textLowEmphasis
    )
}
