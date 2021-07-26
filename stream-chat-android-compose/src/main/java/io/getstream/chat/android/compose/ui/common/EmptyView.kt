package io.getstream.chat.android.compose.ui.common

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The view that's shown when there's no data available.
 *
 * @param modifier - Modifier for styling.
 * */
@Composable
fun EmptyView(modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = "No data",
        color = ChatTheme.colors.textHighEmphasis,
    )
    // TODO see with Design to build something
}
