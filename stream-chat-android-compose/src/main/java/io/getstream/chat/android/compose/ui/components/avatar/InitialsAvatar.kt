package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.initialsGradient

/**
 * Represents a special avatar case when we need to show the initials instead of an image. Usually happens when there
 * are no images to show in the avatar.
 *
 * @param initials The initials to show.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param onClick The handler when the user clicks on the avatar.
 */
@Composable
public fun InitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier: Modifier = if (onClick != null) {
        modifier.clickable(
            onClick = onClick,
            indication = rememberRipple(bounded = false),
            interactionSource = remember { MutableInteractionSource() }
        )
    } else {
        modifier
    }

    val initialsGradient = initialsGradient(initials = initials)

    Box(
        modifier = clickableModifier
            .clip(shape)
            .background(brush = initialsGradient)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = initials,
            style = ChatTheme.typography.title3Bold,
            color = Color.White
        )
    }
}
