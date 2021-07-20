package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows the default loading UI.
 *
 * @param modifier - Modifier for styling.
 * */
@Composable
fun LoadingView(modifier: Modifier = Modifier) {

    Box(
        modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp, color = ChatTheme.colors.primaryAccent)
    }
}