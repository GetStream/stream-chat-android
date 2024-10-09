/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.utils.extensions.initials

/**
 * Represents the [Channel] avatar that's shown when browsing channels or when you open the Messages screen.
 *
 * Based on the state of the [Channel] and the number of members, it shows different types of images.
 *
 * @param channel The channel whose data we need to show.
 * @param currentUser The current user, used to determine avatar data.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param textStyle The [TextStyle] that will be used for the initials.
 * @param groupAvatarTextStyle The [TextStyle] that will be used for the initials in sectioned avatar.
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
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    groupAvatarTextStyle: TextStyle = ChatTheme.typography.captionBold,
    showOnlineIndicator: Boolean = true,
    onlineIndicatorAlignment: OnlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
    onlineIndicator: @Composable BoxScope.() -> Unit = {
        DefaultOnlineIndicator(onlineIndicatorAlignment)
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
            Avatar(
                modifier = modifier.testTag("Stream_ChannelAvatar"),
                imageUrl = channel.image,
                initials = channel.initials,
                textStyle = textStyle,
                shape = shape,
                contentDescription = contentDescription,
                onClick = onClick,
            )
        }

        /**
         * If the channel has one member we show the member's image or initials.
         */
        memberCount == 1 -> {
            val user = members.first().user

            UserAvatar(
                modifier = modifier.testTag("Stream_ChannelAvatar"),
                user = user,
                shape = shape,
                contentDescription = user.name,
                showOnlineIndicator = showOnlineIndicator,
                onlineIndicatorAlignment = onlineIndicatorAlignment,
                onlineIndicator = onlineIndicator,
                onClick = onClick,
            )
        }
        /**
         * If the channel has two members and one of the is the current user - we show the other
         * member's image or initials.
         */
        memberCount == 2 && members.any { it.user.id == currentUser?.id } -> {
            val user = members.first { it.user.id != currentUser?.id }.user

            UserAvatar(
                modifier = modifier.testTag("Stream_ChannelAvatar"),
                user = user,
                shape = shape,
                contentDescription = user.name,
                showOnlineIndicator = showOnlineIndicator,
                onlineIndicatorAlignment = onlineIndicatorAlignment,
                onlineIndicator = onlineIndicator,
                onClick = onClick,
            )
        }
        /**
         * If the channel has more than two members - we load a matrix of their images or initials.
         */
        else -> {
            val users = members.filter { it.user.id != currentUser?.id }.map { it.user }

            GroupAvatar(
                users = users,
                modifier = modifier.testTag("Stream_ChannelAvatar"),
                shape = shape,
                textStyle = groupAvatarTextStyle,
                onClick = onClick,
            )
        }
    }
}

/**
 * Preview of [ChannelAvatar] for a channel with an avatar image.
 *
 * Should show a channel image.
 */
@Preview(showBackground = true, name = "ChannelAvatar Preview (With image)")
@Composable
private fun ChannelWithImageAvatarPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithImage)
}

/**
 * Preview of [ChannelAvatar] for a direct conversation with an online user.
 *
 * Should show a user avatar with an online indicator.
 */
@Preview(showBackground = true, name = "ChannelAvatar Preview (Online user)")
@Composable
private fun ChannelAvatarForDirectChannelWithOnlineUserPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithOnlineUser)
}

/**
 * Preview of [ChannelAvatar] for a direct conversation with only one user.
 *
 * Should show a user avatar with an online indicator.
 */
@Preview(showBackground = true, name = "ChannelAvatar Preview (Only one user)")
@Composable
private fun ChannelAvatarForDirectChannelWithOneUserPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithOneUser)
}

/**
 * Preview of [ChannelAvatar] for a channel without image and with few members.
 *
 * Should show an avatar with 2 sections that represent the avatars of the first
 * 2 members of the channel.
 */
@Preview(showBackground = true, name = "ChannelAvatar Preview (Few members)")
@Composable
private fun ChannelAvatarForChannelWithFewMembersPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithFewMembers)
}

/**
 * Preview of [ChannelAvatar] for a channel without image and with many members.
 *
 * Should show an avatar with 4 sections that represent the avatars of the first
 * 4 members of the channel.
 */
@Preview(showBackground = true, name = "ChannelAvatar Preview (Many members)")
@Composable
private fun ChannelAvatarForChannelWithManyMembersPreview() {
    ChannelAvatarPreview(PreviewChannelData.channelWithManyMembers)
}

/**
 * Shows [ChannelAvatar] preview for the provided parameters.
 *
 * @param channel The channel used to show the preview.
 */
@Composable
private fun ChannelAvatarPreview(channel: Channel) {
    ChatTheme {
        ChannelAvatar(
            channel = channel,
            currentUser = PreviewUserData.user1,
            modifier = Modifier.size(36.dp),
        )
    }
}
