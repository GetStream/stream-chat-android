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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo

/**
 * Shows special UI when an item is selected.
 * It also prepares the available options for the channel, based on if we're an admin or not.
 *
 * @param selectedChannel The channel the user selected.
 * @param currentUser The currently logged-in user data.
 * @param channelActions The list of actions to show in the menu.
 * @param onChannelOptionClick Handler for when the user selects a channel option.
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
    onChannelOptionClick: (ChannelAction) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
    overlayColor: Color = ChatTheme.colors.backgroundCoreScrim,
    headerContent: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMenuHeaderContent(
                modifier = Modifier,
                selectedChannel = selectedChannel,
                currentUser = currentUser,
            )
        }
    },
    centerContent: @Composable ColumnScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelMenuCenterContent(
                modifier = Modifier,
                onChannelOptionClick = onChannelOptionClick,
                channelActions = channelActions,
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
 * @param selectedChannel The channel the user selected.
 * @param currentUser The currently logged-in user data.
 */
@Composable
internal fun DefaultSelectedChannelMenuHeaderContent(
    selectedChannel: Channel,
    currentUser: User?,
) {
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
            modifier = Modifier.size(AvatarSize.ExtraLarge),
            channel = selectedChannel,
            currentUser = currentUser,
            showIndicator = true,
            showBorder = false,
        )

        Column(
            modifier = Modifier
                .padding(start = StreamTokens.spacingSm)
                .weight(1f),
        ) {
            Text(
                text = ChatTheme.channelNameFormatter.formatChannelName(
                    selectedChannel,
                    currentUser,
                ),
                style = ChatTheme.typography.headingSmall,
                color = ChatTheme.colors.textPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = selectedChannel.getMembersStatusText(
                    context = LocalContext.current,
                    currentUser = currentUser,
                    userPresence = ChatTheme.userPresence,
                ),
                style = ChatTheme.typography.captionDefault,
                color = ChatTheme.colors.textSecondary,
                maxLines = 1,
            )
        }
    }
}

/**
 * Preview of [SelectedChannelMenu] styled as a centered modal dialog.
 *
 * Should show a centered dialog with channel members and channel options.
 */
@Preview(showBackground = true, name = "SelectedChannelMenu Preview (Centered dialog)")
@Composable
private fun SelectedChannelMenuCenteredDialogPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            val channel = PreviewChannelData.channelWithManyMembers
            SelectedChannelMenu(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                selectedChannel = channel,
                currentUser = PreviewUserData.user1,
                channelActions = listOf(
                    ViewInfo(channel = channel, label = "Channel Info", onAction = {}),
                ),
                onChannelOptionClick = {},
                onDismiss = {},
            )
        }
    }
}

/**
 * Preview of [SelectedChannelMenu] styled as a bottom sheet dialog.
 *
 * Should show a bottom sheet dialog with channel members and channel options.
 */
@Preview(showBackground = true, name = "SelectedChannelMenu Preview (Bottom sheet dialog)")
@Composable
private fun SelectedChannelMenuBottomSheetDialogPreview() {
    ChatTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            val channel = PreviewChannelData.channelWithManyMembers
            SelectedChannelMenu(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                selectedChannel = channel,
                currentUser = PreviewUserData.user1,
                channelActions = listOf(
                    ViewInfo(channel = channel, label = "Channel Info", onAction = {}),
                ),
                onChannelOptionClick = {},
                onDismiss = {},
            )
        }
    }
}
