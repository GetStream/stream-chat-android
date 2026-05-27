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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
import io.getstream.chat.android.compose.ui.components.SimpleMenu
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
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo
import java.util.Date

/**
 * Shows special UI when an item is selected.
 * It also prepares the available options for the channel, based on if we're an admin or not.
 *
 * @param selectedChannel The channel the user selected.
 * @param currentUser The currently logged-in user data.
 * @param channelActions The list of actions to show in the menu.
 * @param onChannelOptionConfirm Handler for when the user selects a channel option.
 * Routes through confirmation dialogs for destructive actions before executing.
 * @param onDismiss Handler called when the dialog is dismissed.
 * @param modifier Modifier for styling.
 * @param shape The shape of the component.
 * @param overlayColor The color applied to the overlay.
 * @param headerContent The content shown at the top of the dialog.
 * @param centerContent The content shown at the center of the dialog.
 */
@Composable
public fun SelectedChannelMenu(
    selectedChannel: Channel,
    currentUser: User?,
    channelActions: List<ChannelAction>,
    onChannelOptionConfirm: (ChannelAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    overlayColor: Color = ChatTheme.colors.backgroundCoreScrim,
    headerContent: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMenuHeaderContent(
                params = ChannelMenuHeaderContentParams(
                    selectedChannel = selectedChannel,
                    currentUser = currentUser,
                ),
            )
        }
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMenuCenterContent(
                params = ChannelMenuCenterContentParams(
                    onChannelOptionConfirm = onChannelOptionConfirm,
                    channelActions = channelActions,
                ),
            )
        }
    },
) {
    SimpleMenu(
        modifier = modifier,
        shape = shape,
        overlayColor = overlayColor,
        onDismiss = onDismiss,
        headerContent = headerContent,
        centerContent = centerContent,
    )
}

/**
 * Represents the default content shown at the top of [SelectedChannelMenu] dialog.
 *
 * Renders inline muted and pinned icons next to the channel name based on the channel's pin state
 * and the current user's mute settings. When [currentUser] is `null`, no state icons are rendered.
 *
 * @param selectedChannel The channel the user selected.
 * @param currentUser The currently logged-in user data.
 */
@Composable
internal fun DefaultSelectedChannelMenuHeaderContent(
    selectedChannel: Channel,
    currentUser: User?,
) {
    val showPinnedIcon = selectedChannel.isPinned()
    val showMutedIcon = currentUser != null && isChannelOrCounterpartMuted(selectedChannel, currentUser)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = StreamTokens.spacingMd,
                end = StreamTokens.spacingMd,
                top = StreamTokens.spacingMd,
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
private fun SelectedChannelMenuCenteredDialogPreview() {
    ChatTheme {
        SelectedChannelMenuCenteredDialog()
    }
}

@Composable
internal fun SelectedChannelMenuCenteredDialog() {
    SelectedChannelMenuSample(
        alignment = Alignment.Center,
        modifier = Modifier.padding(16.dp),
        shape = RoundedCornerShape(16.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectedChannelMenuBottomSheetDialogPreview() {
    ChatTheme {
        SelectedChannelMenuBottomSheetDialog()
    }
}

@Composable
internal fun SelectedChannelMenuBottomSheetDialog() {
    SelectedChannelMenuSample(
        alignment = Alignment.BottomCenter,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectedChannelMenuMutedPinnedPreview() {
    ChatTheme {
        SelectedChannelMenuMutedPinned()
    }
}

@Composable
internal fun SelectedChannelMenuMutedPinned() {
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
    SelectedChannelMenuSample(
        alignment = Alignment.BottomCenter,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        channel = pinnedChannel,
        currentUser = mutedUser,
    )
}

/**
 * Renders a [SelectedChannelMenu] over a full-size [Box] using preview sample data.
 *
 * @param alignment Vertical alignment of the menu inside the parent [Box].
 * @param shape The shape of the menu surface.
 * @param modifier Modifier applied to the menu, before `fillMaxWidth`, `wrapContentHeight` and
 * `align` are chained on.
 * @param channel The channel rendered in the menu.
 * @param currentUser The user used to resolve member status text and to derive the inline state
 * icons (muted, pinned) in the default header.
 */
@Composable
private fun SelectedChannelMenuSample(
    alignment: Alignment,
    shape: Shape,
    modifier: Modifier = Modifier,
    channel: Channel = PreviewChannelData.channelWithManyMembers,
    currentUser: User? = PreviewUserData.user1,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        SelectedChannelMenu(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .align(alignment),
            shape = shape,
            selectedChannel = channel,
            currentUser = currentUser,
            channelActions = listOf(
                ViewInfo(channel = channel, label = "Channel Info", onAction = {}),
            ),
            onChannelOptionConfirm = {},
            onDismiss = {},
        )
    }
}
