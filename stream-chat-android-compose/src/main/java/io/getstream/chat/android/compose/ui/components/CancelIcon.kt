package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents a simple cancel icon that is used primarily for attachments.
 *
 * @param modifier Modifier for styling.
 * @param onClick Handler when the user clicks on the icon.
 */
@Composable
public fun CancelIcon(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Icon(
        modifier = modifier
            .background(shape = CircleShape, color = ChatTheme.colors.overlayDark)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            ),
        painter = painterResource(id = R.drawable.stream_compose_ic_close),
        contentDescription = stringResource(id = R.string.stream_compose_cancel),
        tint = ChatTheme.colors.appBackground
    )
}
