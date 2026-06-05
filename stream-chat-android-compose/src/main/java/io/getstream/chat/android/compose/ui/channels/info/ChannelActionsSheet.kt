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

package io.getstream.chat.android.compose.ui.channels.info

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.extensions.isMutedFor
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.StreamCardBottomSheet
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChannelAvatarParams
import io.getstream.chat.android.compose.ui.theme.ChannelMenuCenterContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelMenuHeaderContentParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.dmCounterpartId
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelMute
import io.getstream.chat.android.models.Member
import io.getstream.chat.android.models.Mute
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo
import java.util.Date

/**
 * Bottom sheet showing the available actions for a channel.
 *
 * Customize the rendered content by overriding [io.getstream.chat.android.compose.ui.theme.ChatComponentFactory.ChannelMenuHeaderContent]
 * or [io.getstream.chat.android.compose.ui.theme.ChatComponentFactory.ChannelMenuCenterContent].
 *
 * @param channel The channel the actions apply to.
 * @param actions The list of actions to show.
 * @param onActionClick Invoked when the user clicks an action. Destructive actions route through
 * a separate confirmation step before executing.
 * @param onDismiss Invoked when the sheet is dismissed.
 * @param modifier Modifier applied to the sheet container.
 * @param currentUser The currently logged-in user. Used by the default header to derive inline
 * state icons (muted, pinned).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
public fun ChannelActionsSheet(
    channel: Channel,
    actions: List<ChannelAction>,
    onActionClick: (ChannelAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: User? = null,
) {
    StreamCardBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        with(ChatTheme.componentFactory) {
            ChannelMenuHeaderContent(
                params = ChannelMenuHeaderContentParams(
                    selectedChannel = channel,
                    currentUser = currentUser,
                ),
            )
            ChannelMenuCenterContent(
                params = ChannelMenuCenterContentParams(
                    onChannelOptionConfirm = onActionClick,
                    channelActions = actions,
                ),
            )
        }
    }
}

/**
 * Default header for the channel actions menu. Wired in by the
 * `ChatComponentFactory.ChannelMenuHeaderContent` factory method and shared by
 * [ChannelActionsSheet] and the deprecated [SelectedChannelMenu].
 *
 * Renders inline muted and pinned icons next to the channel name based on the channel's pin state
 * and the current user's mute settings. When [currentUser] is `null`, no state icons are rendered.
 *
 * @param selectedChannel The channel the user selected.
 * @param currentUser The currently logged-in user data.
 * @param modifier Modifier applied to the header row. The deprecated SelectedChannelMenu uses
 * this to add a top inset (its Card has no drag handle); ChannelActionsSheet leaves it empty
 * since the Material 3 drag handle already provides the top spacing.
 */
@Composable
internal fun DefaultChannelMenuHeaderContent(
    selectedChannel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val showPinnedIcon = selectedChannel.isPinned()
    val showMutedIcon = currentUser != null && isChannelOrCounterpartMuted(selectedChannel, currentUser)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = StreamTokens.spacingMd,
                end = StreamTokens.spacingMd,
                bottom = StreamTokens.spacingSm,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatTheme.componentFactory.ChannelAvatar(
            params = ChannelAvatarParams(
                modifier = Modifier.size(AvatarSize.ExtraLarge),
                channel = selectedChannel,
                currentUser = currentUser,
                showIndicator = true,
            ),
        )

        Column(
            modifier = Modifier
                .padding(start = StreamTokens.spacingSm)
                .weight(1f),
        ) {
            HeaderTitleRow(
                selectedChannel = selectedChannel,
                currentUser = currentUser,
                showMutedIcon = showMutedIcon,
                showPinnedIcon = showPinnedIcon,
            )
            Text(
                text = selectedChannel.getMembersStatusText(
                    context = LocalContext.current,
                    currentUser = currentUser,
                ),
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun isChannelOrCounterpartMuted(channel: Channel, currentUser: User): Boolean {
    if (channel.isMutedFor(currentUser)) return true
    val otherUserId = channel.dmCounterpartId(currentUser) ?: return false
    return currentUser.mutes.any { it.target?.id == otherUserId }
}

@Composable
private fun HeaderTitleRow(
    selectedChannel: Channel,
    currentUser: User?,
    showMutedIcon: Boolean,
    showPinnedIcon: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacing2xs),
    ) {
        Text(
            modifier = Modifier.weight(1f, fill = false),
            text = ChatTheme.channelNameFormatter.formatChannelName(selectedChannel, currentUser),
            style = ChatTheme.typography.headingSmall,
            color = ChatTheme.colors.textPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (showMutedIcon) {
            HeaderStateIcon(
                iconRes = R.drawable.stream_design_ic_mute,
                contentDescriptionRes = R.string.stream_compose_channel_item_muted,
                testTag = "Stream_ChannelMenuHeaderMutedIcon",
            )
        }
        if (showPinnedIcon) {
            HeaderStateIcon(
                iconRes = R.drawable.stream_design_ic_pin,
                contentDescriptionRes = R.string.stream_compose_channel_item_pinned,
                testTag = "Stream_ChannelMenuHeaderPinnedIcon",
            )
        }
    }
}

@Composable
private fun HeaderStateIcon(
    @DrawableRes iconRes: Int,
    @StringRes contentDescriptionRes: Int,
    testTag: String,
) {
    Icon(
        modifier = Modifier
            .testTag(testTag)
            .size(16.dp),
        painter = painterResource(id = iconRes),
        contentDescription = stringResource(contentDescriptionRes),
        tint = ChatTheme.colors.textTertiary,
    )
}

@Preview(showBackground = true)
@Composable
private fun ChannelActionsSheetPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ChannelActionsSheetSample()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelActionsSheetWithoutCurrentUserPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ChannelActionsSheetSampleWithoutCurrentUser()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelActionsSheetMutedPinnedPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ChannelActionsSheetSampleMutedPinned()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChannelActionsSheetDmUserMutedPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            ChannelActionsSheetSampleDmUserMuted()
        }
    }
}

@Composable
internal fun ChannelActionsSheetSample() {
    val channel = PreviewChannelData.channelWithManyMembers
    ChannelActionsSheet(
        channel = channel,
        actions = listOf(ViewInfo(channel = channel, label = "Channel Info", onAction = {})),
        onActionClick = {},
        onDismiss = {},
        currentUser = PreviewUserData.user1,
    )
}

@Composable
internal fun ChannelActionsSheetSampleWithoutCurrentUser() {
    val channel = PreviewChannelData.channelWithManyMembers
    ChannelActionsSheet(
        channel = channel,
        actions = listOf(ViewInfo(channel = channel, label = "Channel Info", onAction = {})),
        onActionClick = {},
        onDismiss = {},
    )
}

@Composable
internal fun ChannelActionsSheetSampleMutedPinned() {
    val baseChannel = PreviewChannelData.channelWithManyMembers
    val pinnedChannel = baseChannel.copy(
        membership = Member(user = PreviewUserData.user1, pinnedAt = Date()),
    )
    val mutedUser = PreviewUserData.user1.copy(
        channelMutes = listOf(
            ChannelMute(
                user = PreviewUserData.user1,
                channel = pinnedChannel,
                createdAt = Date(),
                updatedAt = Date(),
                expires = null,
            ),
        ),
    )
    ChannelActionsSheet(
        channel = pinnedChannel,
        actions = listOf(ViewInfo(channel = pinnedChannel, label = "Channel Info", onAction = {})),
        onActionClick = {},
        onDismiss = {},
        currentUser = mutedUser,
    )
}

/**
 * Exercises the DM user-mute path in [isChannelOrCounterpartMuted] — the case where the channel
 * itself is not muted but the current user has muted the DM counterpart. Uses a 1-on-1 distinct
 * channel (`cid` containing `!members`) so [io.getstream.chat.android.compose.ui.util.isOneToOne]
 * returns `true`, then derives the muted icon from `currentUser.mutes` rather than
 * `currentUser.channelMutes`.
 */
@Composable
internal fun ChannelActionsSheetSampleDmUserMuted() {
    val counterpart = PreviewUserData.user2
    val dmChannel = Channel(
        type = "messaging",
        id = "!members-test",
        members = listOf(
            Member(user = PreviewUserData.user1),
            Member(user = counterpart),
        ),
        memberCount = 2,
    )
    val userWithMutedCounterpart = PreviewUserData.user1.copy(
        mutes = listOf(
            Mute(
                user = PreviewUserData.user1,
                target = counterpart,
                createdAt = Date(),
                updatedAt = Date(),
                expires = null,
            ),
        ),
    )
    ChannelActionsSheet(
        channel = dmChannel,
        actions = listOf(ViewInfo(channel = dmChannel, label = "Channel Info", onAction = {})),
        onActionClick = {},
        onDismiss = {},
        currentUser = userWithMutedCounterpart,
    )
}
