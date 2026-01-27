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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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

private object StackedGroupAvatarSpecs {
    private val borderSize = 2.dp // TODO [G.] tokens?
    val borderStroke = BorderStroke(2.dp, Color.White) // TODO [G.] color
    fun baseModifier(avatarSize: Dp) = Modifier
        .size(avatarSize + borderSize)
        .border(borderStroke, CircleShape)

    val alignments2 = listOf(Alignment.TopStart, Alignment.BottomEnd)
    val alignments3 = listOf(Alignment.TopCenter, Alignment.BottomStart, Alignment.BottomEnd)
    val alignments4 = listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd)
    val alignmentsMore = listOf(Alignment.TopStart, Alignment.TopEnd)
}

// TODO [G.] indicator?
@Composable
private fun StackedGroupAvatar(
    channel: Channel,
    showBorder: Boolean,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier) {
        val dimensions = resolveStackedAvatarDimensions()
        val borderSize = StackedGroupAvatarSpecs.borderStroke.width
        val borderStroke = StackedGroupAvatarSpecs.borderStroke
        val baseModifier = StackedGroupAvatarSpecs.baseModifier(dimensions.avatarSize)
        when (channel.members.size) {
            // TODO [G.] 0?
            0 -> Spacer(modifier)

            1 -> {
                val alignments = StackedGroupAvatarSpecs.alignments2
                val colors = ChatTheme.colors
                UserAvatarIconPlaceholder(
                    background = colors.avatarBgPlaceholder,
                    foreground = colors.avatarTextPlaceholder,
                    size = dimensions.avatarSize + borderSize,
                    modifier = Modifier
                        .border(borderStroke, CircleShape)
                        .align(alignments[0]),
                )

                UserAvatar(
                    user = channel.members.first().user,
                    modifier = baseModifier.align(alignments[1]),
                )
            }

            2 -> {
                val alignments = StackedGroupAvatarSpecs.alignments2
                for (i in alignments.indices) {
                    UserAvatar(
                        user = channel.members[i].user,
                        modifier = baseModifier.align(alignments[i]),
                    )
                }
            }

            3 -> {
                val alignments = StackedGroupAvatarSpecs.alignments3
                for (i in alignments.indices) {
                    UserAvatar(
                        user = channel.members[i].user,
                        modifier = baseModifier.align(alignments[i]),
                    )
                }
            }

            4 -> {
                val alignments = StackedGroupAvatarSpecs.alignments4
                for (i in alignments.indices) {
                    UserAvatar(
                        user = channel.members[i].user,
                        modifier = baseModifier.align(alignments[i]),
                    )
                }
            }

            else -> {
                val alignments = StackedGroupAvatarSpecs.alignmentsMore
                for (i in alignments.indices) {
                    UserAvatar(
                        user = channel.members[i].user,
                        modifier = baseModifier.align(alignments[i]),
                    )
                }
                CountBadge(
                    // TODO [G.] resource?
                    text = "+" + (channel.members.size - alignments.size),
                    modifier = Modifier
                        .requiredWidth(IntrinsicSize.Max)
                        .defaultMinSize(minWidth = dimensions.badgeSize, minHeight = dimensions.badgeSize)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

// TODO [G.] public?
@Composable
private fun CountBadge(text: String, modifier: Modifier = Modifier) {
    // TODO [G.] tokens & the like
    Text(
        text = text,
        modifier = modifier
            .shadow(2.dp, CircleShape)
            .background(Color.White, CircleShape)
            .padding(horizontal = StreamTokens.spacingXs)
            .wrapContentSize(),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = ChatTheme.typography.numericLarge.copy(color = ChatTheme.colors.badgeTextInverse)
    )
}

private fun BoxWithConstraintsScope.resolveStackedAvatarDimensions(): StackedGroupAvatarDimensions {
    // TODO [G.] badge dimens
    return when {
        maxWidth >= AvatarSize.ExtraLarge -> StackedGroupAvatarDimensions.ExtraLarge
        else -> StackedGroupAvatarDimensions.Large
    }
}

private enum class StackedGroupAvatarDimensions(val avatarSize: Dp, val badgeSize: Dp) {
    ExtraLarge(avatarSize = AvatarSize.Large, badgeSize = 32.dp),
    Large(avatarSize = AvatarSize.Small, badgeSize = 24.dp),
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
    val variants = listOf(1, 2, 3, 4, 5, 13, 1003)
    ChatTheme {
        Column(
            // TODO [G.] color
            modifier = Modifier.background(Color.White),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            variants.forEach { n ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    sizes.forEach { size ->
                        ChannelAvatar(
                            channel = PreviewChannelData.makeChannelWithMembers(n),
                            currentUser = null,
                            modifier = Modifier.size(size),
                        )
                    }
                }
            }
        }
    }
}
