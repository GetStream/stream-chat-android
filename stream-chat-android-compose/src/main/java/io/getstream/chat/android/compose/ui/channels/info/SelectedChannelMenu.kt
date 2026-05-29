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
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.SimpleMenu
import io.getstream.chat.android.compose.ui.theme.ChannelMenuCenterContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelMenuHeaderContentParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
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
@Deprecated(
    message = "Use ChannelActionsSheet. Will be removed in v8.",
    replaceWith = ReplaceWith(
        expression = "ChannelActionsSheet(selectedChannel, channelActions, onChannelOptionConfirm, " +
            "onDismiss, modifier, currentUser)",
    ),
)
@Suppress("DEPRECATION")
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
                    modifier = Modifier.padding(top = StreamTokens.spacingMd),
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
    SelectedChannelMenuSample(alignment = Alignment.BottomCenter)
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
@Suppress("DEPRECATION")
@Composable
private fun SelectedChannelMenuSample(
    alignment: Alignment,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
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
