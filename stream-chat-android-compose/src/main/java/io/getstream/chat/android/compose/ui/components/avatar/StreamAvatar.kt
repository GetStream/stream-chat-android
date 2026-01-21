/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.StreamAsyncImage
import io.getstream.chat.android.compose.ui.util.applyIf
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.images.resizing.applyStreamCdnImageResizingIfEnabled
import io.getstream.chat.android.ui.common.utils.extensions.initials

@Composable
internal fun Avatar(
    imageUrl: String?,
    fallback: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
) {
    val resizing = ChatTheme.streamCdnImageResizing
    val data = remember(imageUrl, resizing) { imageUrl?.applyStreamCdnImageResizingIfEnabled(resizing) }

    StreamAsyncImage(
        data = data,
        modifier = modifier
            .clip(CircleShape)
            .applyIf(showBorder) { border(1.dp, ChatTheme.colors.borderCoreImage, CircleShape) },
        contentScale = ContentScale.Crop,
        content = { state ->
            val painter = (state as? AsyncImagePainter.State.Success)?.painter

            Crossfade(targetState = painter) { painter ->
                if (painter == null) {
                    fallback()
                } else {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        painter = painter,
                        contentDescription = null,
                    )
                }
            }
        },
    )
}

public object AvatarSize {
    public val ExtraSmall: Dp = 20.dp
    public val Small: Dp = 24.dp
    public val Medium: Dp = 32.dp
    public val Large: Dp = 40.dp
}

@Composable
internal fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    showOnlineIndicator: Boolean = false,
    showBorder: Boolean = false,
) {
    BoxWithConstraints(modifier) {
        Avatar(
            imageUrl = user.image,
            fallback = { UserAvatarPlaceholder(user, maxWidth) },
            showBorder = showBorder,
            modifier = Modifier.size(maxWidth),
        )

        if (showOnlineIndicator) {
            val indicatorSize = resolveIndicatorSize()
            OnlineIndicator(
                isOnline = user.online,
                size = indicatorSize,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = indicatorSize.borderWidth,
                        y = -indicatorSize.borderWidth,
                    ),
            )
        }
    }
}

private fun BoxWithConstraintsScope.resolveIndicatorSize(): OnlineIndicatorSize = when {
    maxWidth >= AvatarSize.Large -> OnlineIndicatorSize.Large
    maxWidth >= AvatarSize.Medium -> OnlineIndicatorSize.Medium
    else -> OnlineIndicatorSize.Small
}

@Composable
internal fun ChannelAvatar(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    showOnlineIndicator: Boolean = false,
    showBorder: Boolean = false,
) {
    if (channel.image.isNotEmpty()) {
        GroupAvatar(
            channel = channel,
            showBorder = showBorder,
            modifier = modifier,
        )
    } else if (channel.members.size == 1) {
        UserAvatar(
            modifier = modifier,
            user = channel.members.first().user,
            showOnlineIndicator = showOnlineIndicator,
            showBorder = showBorder,
        )
    } else {
        val directMessageRecipient = directMessageRecipient(channel, currentUser)

        if (directMessageRecipient != null) {
            UserAvatar(
                modifier = modifier,
                user = directMessageRecipient,
                showOnlineIndicator = showOnlineIndicator,
                showBorder = showBorder,
            )
        } else {
            GroupAvatar(
                channel = channel,
                showBorder = showBorder,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun GroupAvatar(
    channel: Channel,
    showBorder: Boolean,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier) {
        Avatar(
            imageUrl = channel.image,
            fallback = { ChannelAvatarPlaceholder(channel, size = this.maxWidth) },
            showBorder = showBorder,
        )
    }
}

/** Returns the other participant if this is a 1-to-1 direct message involving the current user. */
@Composable
private fun directMessageRecipient(channel: Channel, currentUser: User?): User? {
    val currentUserId = currentUser?.id ?: return null

    return remember(channel, currentUserId) {
        if (channel.memberCount == 2 && channel.members.any { it.user.id == currentUserId }) {
            channel.members.first { it.user.id != currentUserId }.user
        } else {
            null
        }
    }
}

@Composable
internal fun UserAvatarPlaceholder(user: User, size: Dp, modifier: Modifier = Modifier) {
    val (background, foreground) = rememberAvatarPlaceholderColors(user.id)
    val initials = rememberPlaceholderInitials(user, size)

    Box(
        modifier
            .background(background)
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (initials.isNotEmpty()) {
            Text(
                text = initials,
                style = size.toPlaceholderTextStyle(),
                color = foreground,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.stream_compose_ic_user),
                contentDescription = null,
                tint = foreground,
                modifier = modifier
                    .background(background)
                    .size(size.toPlaceholderIconSize()),
            )
        }
    }
}

@Composable
internal fun ChannelAvatarPlaceholder(channel: Channel, size: Dp, modifier: Modifier = Modifier) {
    val (background, foreground) = rememberAvatarPlaceholderColors(channel.cid)

    Box(
        modifier
            .background(background)
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_team),
            contentDescription = null,
            tint = foreground,
            modifier = modifier
                .background(background)
                .size(size.toPlaceholderIconSize()),
        )
    }
}

@Composable
private fun rememberPlaceholderInitials(user: User, availableWidth: Dp): String = remember(user.name, availableWidth) {
    val initials = user.initials
    if (availableWidth >= AvatarSize.Medium) {
        initials
    } else {
        initials.take(1)
    }
}

@Preview
@Composable
private fun StreamAvatarPreview() {
    val sizes = listOf(AvatarSize.Large, AvatarSize.Medium, AvatarSize.Small, AvatarSize.ExtraSmall)

    ChatTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sizes.forEach { size ->
                    UserAvatar(
                        user = PreviewUserData.userWithOnlineStatus,
                        showOnlineIndicator = true,
                        modifier = Modifier.size(size),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sizes.forEach { size ->
                    UserAvatar(
                        user = PreviewUserData.userWithoutImage,
                        showOnlineIndicator = true,
                        modifier = Modifier.size(size),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sizes.forEach { size ->
                    ChannelAvatar(
                        PreviewChannelData.channelWithMessages,
                        currentUser = null,
                        modifier = Modifier.size(size),
                    )
                }
            }
        }
    }
}
