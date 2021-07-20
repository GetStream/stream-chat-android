package io.getstream.chat.android.compose.ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The view that's shown when there's no data available.
 *
 * @param modifier - Modifier for styling.
 * */
@Composable
fun EmptyView(modifier: Modifier = Modifier) {
    Text(modifier = modifier, text = "No data")
    // TODO see with Design to build something
}