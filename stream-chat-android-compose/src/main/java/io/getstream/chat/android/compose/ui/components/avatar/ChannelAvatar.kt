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

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.ui.common.utils.extensions.isOneToOne

/**
 * The default avatar for a channel.
 *
 * This component displays the channel image, the user avatar for direct messages, or a placeholder.
 *
 * @param channel The channel whose avatar will be displayed.
 * @param currentUser The user currently logged in.
 * @param showIndicator Whether to overlay a status indicator to show whether the user is online for 1:1 channels.
 * @param showBorder Whether to draw a border around the avatar to provide contrast against the background.
 */
@Composable
public fun ChannelAvatar(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = false,
    showBorder: Boolean = false,
) {
    val testTagModifier = modifier.testTag("Stream_ChannelAvatar")

    if (channel.image.isNotEmpty()) {
        SimpleGroupAvatar(
            modifier = testTagModifier,
            channel = channel,
            showBorder = showBorder,
        )
    } else {
        val directMessageRecipient = directMessageRecipient(channel, currentUser)

        if (directMessageRecipient != null) {
            UserAvatar(
                modifier = testTagModifier,
                user = directMessageRecipient,
                showIndicator = showIndicator,
                showBorder = showBorder,
            )
        } else {
            StackedGroupAvatar(
                modifier = testTagModifier,
                channel = channel,
                showBorder = showBorder,
            )
        }
    }
}

@Composable
private fun SimpleGroupAvatar(
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

// TODO [G.] indicator?
@Composable
private fun StackedGroupAvatar(
    channel: Channel,
    showBorder: Boolean,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier) {
        val avatarSize = resolveStackedAvatarSize()
        val borderSize = 2.dp // TODO [G.] tokens?

        when (channel.members.size) {
            // TODO [G.] 0?
            0 -> Spacer(modifier)
            1 -> {
                val colors = ChatTheme.colors
                UserAvatarIconPlaceholder(
                    background = colors.avatarBgPlaceholder,
                    foreground = colors.avatarTextPlaceholder,
                    size = avatarSize,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(StreamTokens.spacing3xs),
                )

                UserAvatar(
                    user = channel.members.first().user,
                    modifier = Modifier
                        .padding(StreamTokens.spacing3xs)
                        .size(avatarSize + borderSize)
                        .border(borderSize, Color.White, CircleShape)
                        .align(Alignment.BottomEnd),
                )
            }

            2 -> {
                UserAvatar(
                    user = channel.members[0].user,
                    modifier = Modifier
                        .padding(StreamTokens.spacing3xs)
                        .size(avatarSize)
                        .align(Alignment.TopStart),
                )
                UserAvatar(
                    user = channel.members[1].user,
                    modifier = Modifier
                        .padding(StreamTokens.spacing3xs)
                        .size(avatarSize + borderSize)
                        .border(borderSize, Color.White, CircleShape)
                        .align(Alignment.BottomEnd),
                )
            }

            3 -> {
                UserAvatar(
                    user = channel.members[0].user,
                    modifier = Modifier
                        .size(avatarSize)
                        .align(Alignment.TopCenter),
                )
                UserAvatar(
                    user = channel.members[1].user,
                    modifier = Modifier
                        .size(avatarSize + borderSize)
                        .border(borderSize, Color.White, CircleShape)
                        .align(Alignment.BottomStart),
                )
                UserAvatar(
                    user = channel.members[2].user,
                    modifier = Modifier
                        .size(avatarSize + borderSize)
                        .border(borderSize, Color.White, CircleShape)
                        .align(Alignment.BottomEnd),
                )
            }

            4 -> {

            }

            else -> {}
        }
    }
}

private fun BoxWithConstraintsScope.resolveStackedAvatarSize(): Dp {
    return when {
        maxWidth >= AvatarSize.ExtraLarge -> AvatarSize.Large
        else -> AvatarSize.Small
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

/** Returns the other participant if this is a 1-to-1 direct message involving the current user. */
@Composable
private fun directMessageRecipient(channel: Channel, currentUser: User?): User? {
    val currentUserId = currentUser?.id ?: return null

    return remember(channel, currentUserId) {
        if (channel.isOneToOne(currentUser)) {
            channel.members.first { it.user.id != currentUserId }.user
        } else {
            null
        }
    }
}

@Preview
@Composable
private fun ChannelAvatarPreview() {
    val sizes = AvatarSize.run { listOf(ExtraLarge, Large) }
    ChatTheme {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sizes.forEach { size ->
                    ChannelAvatar(
                        PreviewChannelData.channelWithOneUser,
                        currentUser = null,
                        modifier = Modifier.size(size),
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sizes.forEach { size ->
                    ChannelAvatar(
                        PreviewChannelData.channelWithOnlineUser,
                        currentUser = null,
                        modifier = Modifier.size(size),
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                sizes.forEach { size ->
                    ChannelAvatar(
                        PreviewChannelData.channelWithFewMembers,
                        currentUser = null,
                        modifier = Modifier.size(size),
                    )
                }
            }
        }
    }
}
