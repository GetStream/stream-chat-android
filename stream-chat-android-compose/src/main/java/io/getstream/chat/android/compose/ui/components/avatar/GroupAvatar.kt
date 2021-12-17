package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents an avatar with a matrix of user images or initials.
 *
 * @param users The users to show.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param onClick The handler when the user clicks on the avatar.
 */
@Composable
public fun GroupAvatar(
    users: List<User>,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    onClick: (() -> Unit)? = null,
) {
    val avatarUsers = users.take(4)
    val imageCount = avatarUsers.size

    val clickableModifier: Modifier = if (onClick != null) {
        modifier.clickable(
            onClick = onClick,
            indication = rememberRipple(bounded = false),
            interactionSource = remember { MutableInteractionSource() }
        )
    } else {
        modifier
    }

    Row(clickableModifier.clip(shape)) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight()
        ) {
            for (imageIndex in 0 until imageCount step 2) {
                if (imageIndex < imageCount) {
                    UserAvatar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        user = avatarUsers[imageIndex],
                        shape = RectangleShape,
                        showOnlineIndicator = false
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight()
        ) {
            for (imageIndex in 1 until imageCount step 2) {
                if (imageIndex < imageCount) {
                    UserAvatar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        user = avatarUsers[imageIndex],
                        shape = RectangleShape,
                        showOnlineIndicator = false
                    )
                }
            }
        }
    }
}
