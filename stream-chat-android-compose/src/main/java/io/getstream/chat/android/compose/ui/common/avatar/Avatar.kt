package io.getstream.chat.android.compose.ui.common.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Avatar/image that renders whatever image the [painter] provides. It allows for customization,
 * uses the 'avatar' shape from [ChatTheme.shapes] for the clipping and exposes an [onClick] action.
 *
 * @param painter - The painter for the image.
 * @param modifier - Modifier for styling.
 * @param shape - The shape of the avatar.
 * @param contentDescription - Description of the image.
 * @param onClick - OnClick action, that can be nullable.
 * */
@Composable
public fun Avatar(
    painter: Painter,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier: Modifier = if (onClick != null) {
        modifier.clickable(
            onClick = onClick,
            indication = rememberRipple(bounded = false, radius = 24.dp),
            interactionSource = remember { MutableInteractionSource() }
        )
    } else {
        modifier
    }

    Image(
        modifier = clickableModifier.clip(shape),
        contentScale = ContentScale.Crop,
        painter = painter,
        contentDescription = contentDescription
    )
}
