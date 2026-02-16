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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.common.CountBadge
import io.getstream.chat.android.compose.ui.components.common.CountBadgeSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.applyIf
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
            currentUser = currentUser,
            showIndicator = showIndicator,
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
                currentUser = currentUser,
                showIndicator = showIndicator,
                showBorder = showBorder,
            )
        }
    }
}

@Composable
private fun SimpleGroupAvatar(
    channel: Channel,
    currentUser: User?,
    showIndicator: Boolean,
    showBorder: Boolean,
    modifier: Modifier,
) {
    WithChannelIndicator(
        channel = channel,
        currentUser = currentUser,
        showIndicator = showIndicator,
        modifier = modifier,
    ) {
        Avatar(
            imageUrl = channel.image,
            fallback = { ChannelAvatarPlaceholder(channel, size = this.maxWidth) },
            showBorder = showBorder,
        )
    }
}

@Composable
private fun WithChannelIndicator(
    channel: Channel,
    currentUser: User?,
    showIndicator: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable BoxWithConstraintsScope.() -> Unit,
) {
    BoxWithConstraints(modifier) {
        content()

        if (showIndicator) {
            val isOnline = remember(channel.members, currentUser?.id) {
                channel.members.any { it.user.id != currentUser?.id && it.user.online }
            }
            val dimensions = resolveIndicatorDimensions()
            OnlineIndicator(
                isOnline = isOnline,
                dimensions = dimensions,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = dimensions.offset,
                        y = -dimensions.offset,
                    ),
            )
        }
    }
}

private object StackedGroupAvatarSpecs {
    private val alignments2 = listOf(Alignment.TopStart, Alignment.BottomEnd)
    private val alignments3 = listOf(Alignment.TopCenter, Alignment.BottomStart, Alignment.BottomEnd)
    private val alignments4 = listOf(Alignment.TopStart, Alignment.TopEnd, Alignment.BottomStart, Alignment.BottomEnd)
    private val alignmentsMore = listOf(Alignment.TopStart, Alignment.TopEnd)
    private val alignmentsByIndex = listOf(emptyList(), alignments2, alignments2, alignments3, alignments4)

    fun alignmentsFor(membersCount: Int): List<Alignment> {
        return alignmentsByIndex.getOrElse(membersCount) { alignmentsMore }
    }

    @Composable
    fun baseModifier(avatarSize: Dp): Modifier {
        val borderWidth = StreamTokens.spacing3xs

        return Modifier
            .size(avatarSize + borderWidth)
            .border(BorderStroke(borderWidth, ChatTheme.colors.borderCoreOnDark), CircleShape)
            .padding(borderWidth)
    }
}

@Suppress("MagicNumber")
@Composable
private fun StackedGroupAvatar(
    channel: Channel,
    currentUser: User?,
    showIndicator: Boolean,
    showBorder: Boolean,
    modifier: Modifier,
) {
    WithChannelIndicator(
        channel = channel,
        currentUser = currentUser,
        showIndicator = showIndicator,
        modifier = modifier,
    ) {
        val dimensions = resolveStackedAvatarDimensions()
        val baseModifier = StackedGroupAvatarSpecs.baseModifier(dimensions.avatarSize)
        val membersCount = channel.members.size
        val alignments = StackedGroupAvatarSpecs.alignmentsFor(membersCount)

        when (membersCount) {
            0 -> ChannelAvatarPlaceholder(
                channel = channel,
                size = maxWidth,
                modifier = Modifier
                    .applyIf(showBorder) { border(1.dp, ChatTheme.colors.borderCoreImage, CircleShape) }
                    .clip(CircleShape),
            )

            1 -> {
                val colors = ChatTheme.colors
                UserAvatarIconPlaceholder(
                    background = colors.avatarBgPlaceholder,
                    foreground = colors.avatarTextPlaceholder,
                    modifier = baseModifier
                        .clip(CircleShape)
                        .applyIf(showBorder) { border(1.dp, ChatTheme.colors.borderCoreImage, CircleShape) }
                        .align(alignments[0]),
                )

                UserAvatar(
                    user = channel.members.first().user,
                    showBorder = showBorder,
                    modifier = baseModifier.align(alignments[1]),
                )
            }

            else -> {
                for (i in alignments.indices) {
                    UserAvatar(
                        user = channel.members[i].user,
                        showBorder = showBorder,
                        modifier = baseModifier.align(alignments[i]),
                    )
                }
                if (membersCount > 4) {
                    val count = (membersCount - alignments.size).coerceAtMost(99)
                    CountBadge(
                        text = stringResource(R.string.stream_compose_avatar_overflow_count, count),
                        size = dimensions.badgeSize,
                        fixedFontSize = true,
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }
        }
    }
}

private fun BoxWithConstraintsScope.resolveStackedAvatarDimensions(): StackedGroupAvatarDimensions {
    return when {
        maxWidth >= AvatarSize.ExtraLarge -> StackedGroupAvatarDimensions.ExtraLarge
        maxWidth >= AvatarSize.Large -> StackedGroupAvatarDimensions.Large
        else -> StackedGroupAvatarDimensions.Medium
    }
}

private enum class StackedGroupAvatarDimensions(val avatarSize: Dp, val badgeSize: CountBadgeSize) {
    ExtraLarge(avatarSize = AvatarSize.Large, badgeSize = CountBadgeSize.Large),
    Large(avatarSize = AvatarSize.Small, badgeSize = CountBadgeSize.Medium),
    Medium(avatarSize = AvatarSize.ExtraSmall, badgeSize = CountBadgeSize.Small),
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
            modifier = Modifier
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

@Suppress("MagicNumber")
@Preview
@Composable
private fun ChannelAvatarPreview() {
    val sizes = AvatarSize.run { listOf(ExtraLarge, Large, Medium) }
    val variants = listOf(0, 1, 2, 3, 4, 5, 13, 1000)
    ChatTheme {
        Column(
            modifier = Modifier
                .background(ChatTheme.colors.appBackground)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            variants.forEach { howMany ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    sizes.forEach { size ->
                        ChannelAvatar(
                            channel = PreviewChannelData.makeChannelWithMembers(howMany),
                            currentUser = null,
                            modifier = Modifier.size(size),
                        )
                    }
                }
            }
        }
    }
}
