package io.getstream.chat.android.compose.ui.common.avatar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.initialsGradient

/**
 * Represents a special avatar case when we need to show the initials instead of an image. Usually happens when there
 * are no images to show in the avatar.
 *
 * @param initials - The initials to show.
 * @param modifier - Modifier for styling.
 * @param shape - The shape of the avatar.
 * */
@Composable
public fun InitialsAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
) {
    val initialsGradient = initialsGradient(initials = initials)

    Box(
        modifier = modifier
            .clip(shape)
            .background(brush = initialsGradient)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = initials,
            style = ChatTheme.typography.title1,
            color = ChatTheme.colors.textHighEmphasis
        )
    }
}
