package io.getstream.chat.android.compose.ui.common.avatar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.compose.preview.ChannelAvatarPreviewParameterProvider
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the [Channel] avatar that's shown when browsing channels or when you open the Messages screen.
 *
 * Based on the state of the [Channel] and the number of members, it shows different types of images.
 *
 * @param channel The channel whose data we need to show.
 * @param currentUser The current user, used to determine avatar data.
 * @param modifier Modifier for styling.
 * @param contentDescription The description to use for the avatar.
 * @param onClick The handler when the user clicks on the avatar.
 */
@Composable
public fun ChannelAvatar(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val members = channel.members
    val memberCount = members.size

    when {
        /**
         * If the channel has an image we load that as a priority.
         */
        channel.image.isNotEmpty() -> {
            val painter = rememberImagePainter(data = channel.image)

            Avatar(
                modifier = modifier,
                painter = painter,
                contentDescription = contentDescription,
                onClick = onClick
            )
        }
        /**
         * If the channel has just one member (current user) we show our initials.
         */
        memberCount == 1 -> {
            val channelInitials = channel.initials

            InitialsAvatar(
                modifier = modifier,
                initials = channelInitials,
                onClick = onClick
            )
        }
        /**
         * If the channel has two members - direct message with another person - we show their image or initials.
         */
        memberCount == 2 -> {
            val user = members.first { it.user.id != currentUser?.id }.user

            UserAvatar(
                modifier = modifier,
                user = user,
                contentDescription = user.name,
                onClick = onClick
            )
        }
        /**
         * If the channel has more than two members - group - we load a matrix of their images or initials.
         */
        else -> {
            val activeUsers = members.filter { it.user.id != currentUser?.id }.take(4)
            val imageCount = activeUsers.size

            val clickableModifier: Modifier = if (onClick != null) {
                modifier.clickable(
                    onClick = onClick,
                    indication = rememberRipple(bounded = false, radius = 24.dp),
                    interactionSource = remember { MutableInteractionSource() }
                )
            } else {
                modifier
            }

            Row(clickableModifier.clip(ChatTheme.shapes.avatar)) {
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
                                user = activeUsers[imageIndex].user,
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
                                user = activeUsers[imageIndex].user,
                                shape = RectangleShape,
                                showOnlineIndicator = false
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Channel avatar")
@Preview
@Composable
private fun ChannelAvatarPreview(
    @PreviewParameter(ChannelAvatarPreviewParameterProvider::class) data: Pair<User, Channel>,
) {
    ChatTheme {
        val (currentUser, channel) = data
        ChannelAvatar(
            channel = channel,
            currentUser = currentUser,
            modifier = Modifier.size(36.dp)
        )
    }
}
