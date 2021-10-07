package io.getstream.chat.android.compose.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the loading footer UI in lists.
 *
 * @param modifier Modifier for styling.
 */
@Composable
public fun LoadingFooter(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(color = ChatTheme.colors.appBackground)
            .padding(top = 8.dp, bottom = 48.dp)
    ) {
        LoadingView(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.Center)
        )
    }
}
