package io.getstream.chat.android.compose.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Basic back button, that shows an icon and calls [onBackPressed] when tapped.
 *
 * @param painter The icon or image to show.
 * @param onBackPressed Handler for the back action.
 * @param modifier Modifier for styling.
 */
@Composable
public fun BackButton(
    painter: Painter,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    IconButton(
        modifier = modifier,
        onClick = onBackPressed
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = ChatTheme.colors.textHighEmphasis,
        )
    }
}
