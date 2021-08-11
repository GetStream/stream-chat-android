package io.getstream.chat.android.compose.ui.common.avatar

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the [User] avatar that's shown on the Messages screen or in headers of DMs.
 *
 * Based on the state within the [User], we either show an image or their initials.
 *
 * @param user - The user whose avatars we want to show.
 * @param modifier - Modifier for styling.
 * @param shape - The shape of the avatar.
 * @param contentDescription - The content description of the avatar.
 * @param onClick - The handler when the user clicks on the avatar.
 * */
@Composable
public fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    if (user.image.isNotBlank()) {
        val authorImage = rememberImagePainter(data = user.image)

        Avatar(
            modifier = modifier,
            shape = shape,
            painter = authorImage,
            contentDescription = contentDescription,
            onClick = onClick
        )
    } else {
        InitialsAvatar(user.initials, modifier, shape)
    }
}
