package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.initials
import io.getstream.chat.android.compose.previewdata.PreviewChannelData
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.components.OnlineIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Represents the [Channel] avatar that's shown when browsing channels or when you open the Messages screen.
 *
 * Based on the state of the [Channel] and the number of members, it shows different types of images.
 *
 * @param channel The channel whose data we need to show.
 * @param currentUser The current user, used to determine avatar data.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param showOnlineIndicator If we show online indicator or not.
 * @param onlineIndicatorAlignment The alignment of online indicator.
 * @param onlineIndicator Custom composable that allows to replace the default online indicator.
 * @param contentDescription The description to use for the avatar.
 * @param onClick The handler when the user clicks on the avatar.
 */
@Composable
public fun ChannelAvatar(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    showOnlineIndicator: Boolean = true,
    onlineIndicatorAlignment: OnlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
    onlineIndicator: @Composable BoxScope.() -> Unit = {
        OnlineIndicator(modifier = Modifier.align(onlineIndicatorAlignment.alignment))
    },
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
                shape = shape,
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
                shape = shape,
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
                shape = shape,
                contentDescription = user.name,
                showOnlineIndicator = showOnlineIndicator,
                onlineIndicatorAlignment = onlineIndicatorAlignment,
                onlineIndicator = onlineIndicator,
                onClick = onClick
            )
        }
        /**
         * If the channel has more than two members - group - we load a matrix of their images or initials.
         */
        else -> {
            val users = members.filter { it.user.id != currentUser?.id }.map { it.user }

            GroupAvatar(
                users = users,
                modifier = modifier,
                shape = shape,
                onClick = onClick,
            )
        }
    }
}

@Preview
@Composable
private fun ChannelWithImageAvatarPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithImage)
}

@Preview
@Composable
private fun ChannelWithOnlineUserAvatarPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithOnlineUser)
}

@Preview
@Composable
private fun ChannelWithFewMembersAvatarPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithFewMembers)
}

@Preview
@Composable
private fun ChannelWithManyMembersAvatarPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithManyMembers)
}

@Composable
private fun ChannelAvatarPreview(channel: Channel) {
    ChatTheme {
        ChannelAvatar(
            channel = channel,
            currentUser = channel.members.first().user,
            modifier = Modifier.size(36.dp)
        )
    }
}
