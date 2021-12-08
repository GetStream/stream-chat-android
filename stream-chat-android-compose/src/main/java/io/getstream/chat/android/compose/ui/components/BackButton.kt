package io.getstream.chat.android.compose.ui.components

import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Basic back button, that shows an icon and calls [onBackPressed] when tapped.
 *
 * @param imageVector The icon to show.
 * @param onBackPressed Handler for the back action.
 * @param modifier Modifier for styling.
 */
@Composable
public fun BackButton(
    imageVector: ImageVector,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {

    IconButton(
        modifier = modifier,
        onClick = onBackPressed
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = ChatTheme.colors.textHighEmphasis,
        )
    }
}
