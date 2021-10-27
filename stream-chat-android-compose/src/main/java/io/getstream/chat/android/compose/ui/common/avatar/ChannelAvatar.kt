package io.getstream.chat.android.compose.ui.common.avatar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.initials
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
 */
@Composable
public fun ChannelAvatar(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
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
                contentDescription = contentDescription
            )
        }
        /**
         * If the channel has just one member (current user) we show our initials.
         */
        memberCount == 1 -> {
            val channelInitials = channel.initials

            InitialsAvatar(initials = channelInitials, modifier)
        }
        /**
         * If the channel has two members - direct message with another person - we show their image or initials.
         */
        memberCount == 2 -> {
            val user = members.first { it.user.id != currentUser?.id }.user

            UserAvatar(
                modifier = modifier,
                user = user,
                contentDescription = user.name
            )
        }
        /**
         * If the channel has more than two members - group - we load a matrix of their images or initials.
         */
        else -> {
            val activeUsers = members.filter { it.user.id != currentUser?.id }.take(4)
            val imageCount = activeUsers.size

            Row(modifier.clip(ChatTheme.shapes.avatar)) {
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
    @PreviewParameter(ChannelAvatarPreviewParameterProvider::class) data: ChannelAvatarPreviewData,
) {
    ChatTheme {
        ChannelAvatar(
            channel = data.channel,
            currentUser = data.currentUser,
            modifier = Modifier.size(36.dp)
        )
    }
}

/**
 * Provides sample channels that will be used to render channel avatar previews.
 */
private class ChannelAvatarPreviewParameterProvider : PreviewParameterProvider<ChannelAvatarPreviewData> {
    override val values: Sequence<ChannelAvatarPreviewData> = sequenceOf(
        ChannelAvatarPreviewData(
            currentUser = currentUser,
            channel = Channel().apply {
                image = "https://picsum.photos/id/237/128/128"
                members = listOf(
                    Member(user = currentUser),
                    Member(user = user1),
                )
            }
        ),
        ChannelAvatarPreviewData(
            currentUser = currentUser,
            channel = Channel().apply {
                members = listOf(
                    Member(user = currentUser),
                    Member(user = user1),
                )
            }
        ),
        ChannelAvatarPreviewData(
            currentUser = currentUser,
            channel = Channel().apply {
                members = listOf(
                    Member(user = currentUser),
                    Member(user = user1),
                    Member(user = user2),
                )
            }
        ),
        ChannelAvatarPreviewData(
            currentUser = currentUser,
            channel = Channel().apply {
                members = listOf(
                    Member(user = currentUser),
                    Member(user = user1),
                    Member(user = user2),
                    Member(user = user3),
                    Member(user = user4),
                )
            }
        ),
    )

    private companion object {
        private val currentUser: User = User().apply {
            id = "jc"
            name = "Jc Miñarro"
            image = "https://ca.slack-edge.com/T02RM6X6B-U011KEXDPB2-891dbb8df64f-128"
        }
        private val user1: User = User().apply {
            id = "amit"
            name = "Amit Kumar"
            image = "https://ca.slack-edge.com/T02RM6X6B-U027L4AMGQ3-9ca65ea80b60-128"
            online = true
        }
        private val user2: User = User().apply {
            id = "belal"
            name = "Belal Khan"
            image = "https://ca.slack-edge.com/T02RM6X6B-U02DAP0G2AV-2072330222dc-128"
        }
        private val user3: User = User().apply {
            id = "dmitrii"
            name = "Dmitrii Bychkov"
            image = "https://ca.slack-edge.com/T02RM6X6B-U01CDPY6YE8-b74b0739493e-128"
        }
        private val user4: User = User().apply {
            id = "filip"
            name = "Filip Babić"
            image = "https://ca.slack-edge.com/T02RM6X6B-U022AFX9D2S-f7bcb3d56180-128"
        }
    }
}

/**
 * A class that encapsulates test data that will be provided to channel previews.
 */
private data class ChannelAvatarPreviewData(
    val currentUser: User,
    val channel: Channel,
)
