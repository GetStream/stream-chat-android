package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Shows the loading footer UI in lists.
 *
 * @param modifier - Modifier for styling.
 * */
@Composable
fun LoadingFooter(modifier: Modifier = Modifier) {

    Box(modifier = modifier.padding(top = 8.dp, bottom = 48.dp)) {
        LoadingView(modifier = Modifier.size(16.dp))
    }
}