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

package io.getstream.chat.android.compose.ui.components.channels

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.extensions.isArchive
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.isDistinct
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.ui.common.state.channels.actions.ArchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.BlockUser
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.ConfirmationPopup
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.LeaveGroup
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.MuteUser
import io.getstream.chat.android.ui.common.state.channels.actions.PinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnarchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnblockUser
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteUser
import io.getstream.chat.android.ui.common.state.channels.actions.UnpinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo

/**
 * This is the default bottom drawer UI that shows up when the user long taps on a channel item.
 *
 * It sets up different actions that we provide, based on user permissions.
 *
 * @param actions The list of channel actions to show in the UI.
 * @param onChannelOptionConfirm Handler for when the user selects a channel action.
 * Routes through confirmation dialogs for destructive actions before executing.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ChannelOptions(
    actions: List<ChannelAction>,
    onChannelOptionConfirm: (ChannelAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        contentPadding = PaddingValues(bottom = StreamTokens.spacingXs),
    ) {
        items(actions) { action ->
            with(ChatTheme.componentFactory) {
                ChannelOptionsItem(
                    modifier = Modifier,
                    action = action,
                    onClick = { onChannelOptionConfirm(action) },
                )
            }
        }
    }
}

/**
 * Builds the default list of channel actions, based on the current user permissions and channel state.
 * Each action is self-describing and carries its icon, label, and execution handler.
 *
 * Actions vary by channel type:
 * - **DM:** View Info, Mute/Unmute User, Block/Unblock User, Archive Chat, Delete Chat
 * - **Group (owner):** View Info, Mute/Unmute Group, Archive Group, Delete Group
 * - **Group (member):** View Info, Mute/Unmute Group, Archive Group, Leave Group
 *
 * @param selectedChannel The currently selected channel.
 * @param isMuted If the channel is muted or not.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * @param viewModel The [ChannelListViewModel] to bind action handlers to.
 * @param onViewInfoAction Handler invoked when the user selects the "View Info" action.
 * @return The list of channel actions to display.
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
public fun buildDefaultChannelActions(
    selectedChannel: Channel,
    isMuted: Boolean,
    ownCapabilities: Set<String>,
    viewModel: ChannelListViewModel,
    onViewInfoAction: (Channel) -> Unit,
): List<ChannelAction> {
    val optionVisibility = ChatTheme.channelOptionsTheme.optionVisibility
    val currentUser by viewModel.user.collectAsState()
    val channelName = ChatTheme.channelNameFormatter.formatChannelName(
        selectedChannel,
        currentUser,
    )
    val isDm = selectedChannel.isDistinct() && selectedChannel.memberCount <= 2

    return if (isDm) {
        buildDmChannelActions(
            selectedChannel = selectedChannel,
            currentUser = currentUser,
            ownCapabilities = ownCapabilities,
            optionVisibility = optionVisibility,
            channelName = channelName,
            viewModel = viewModel,
            onViewInfoAction = onViewInfoAction,
        )
    } else {
        buildGroupChannelActions(
            selectedChannel = selectedChannel,
            isMuted = isMuted,
            ownCapabilities = ownCapabilities,
            optionVisibility = optionVisibility,
            channelName = channelName,
            viewModel = viewModel,
            onViewInfoAction = onViewInfoAction,
        )
    }
}

/**
 * Builds channel actions for DM (1-to-1) channels.
 * Shows: View Info, Mute/Unmute User, Block/Unblock User, Archive Chat, Delete Chat.
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
private fun buildDmChannelActions(
    selectedChannel: Channel,
    currentUser: User?,
    ownCapabilities: Set<String>,
    optionVisibility: ChannelOptionItemVisibility,
    channelName: String,
    viewModel: ChannelListViewModel,
    onViewInfoAction: (Channel) -> Unit,
): List<ChannelAction> {
    val otherUserId = selectedChannel.members.firstOrNull { it.user.id != currentUser?.id }?.user?.id
    val isUserMuted = otherUserId?.let { viewModel.isUserMuted(it) } ?: false
    val isUserBlocked = otherUserId?.let { viewModel.isUserBlocked(it) } ?: false
    val canDeleteChannel = ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)

    return listOfNotNull(
        if (optionVisibility.isViewInfoVisible) {
            ViewInfo(
                channel = selectedChannel,
                label = stringResource(id = R.string.stream_compose_selected_channel_menu_view_info),
                onAction = { onViewInfoAction(selectedChannel) },
            )
        } else {
            null
        },
        if (otherUserId != null && optionVisibility.isMuteChannelVisible) {
            if (isUserMuted) {
                UnmuteUser(
                    channel = selectedChannel,
                    label = stringResource(id = R.string.stream_compose_selected_channel_menu_unmute_user),
                    onAction = { viewModel.unmuteUser(otherUserId) },
                )
            } else {
                MuteUser(
                    channel = selectedChannel,
                    label = stringResource(id = R.string.stream_compose_selected_channel_menu_mute_user),
                    onAction = { viewModel.muteUser(otherUserId) },
                )
            }
        } else {
            null
        },
        if (otherUserId == null) {
            null
        } else if (isUserBlocked) {
            UnblockUser(
                channel = selectedChannel,
                label = stringResource(id = R.string.stream_compose_selected_channel_menu_unblock_user),
                onAction = { viewModel.unblockUser(otherUserId) },
            )
        } else {
            BlockUser(
                channel = selectedChannel,
                label = stringResource(id = R.string.stream_compose_selected_channel_menu_block_user),
                onAction = { viewModel.blockUser(otherUserId) },
            )
        },
        buildDmArchiveAction(
            canArchiveChannel = optionVisibility.isArchiveChannelVisible,
            selectedChannel = selectedChannel,
            viewModel = viewModel,
        ),
        if (optionVisibility.isDeleteChannelVisible && canDeleteChannel) {
            DeleteConversation(
                channel = selectedChannel,
                label = stringResource(id = R.string.stream_compose_selected_channel_menu_delete_chat),
                onAction = { viewModel.deleteConversation(selectedChannel) },
                confirmationPopup = ConfirmationPopup(
                    title = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_delete_chat_confirmation_title,
                    ),
                    message = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_delete_chat_confirmation_message,
                        channelName,
                    ),
                    confirmButtonText = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_delete_chat,
                    ),
                ),
            )
        } else {
            null
        },
    )
}

/**
 * Builds channel actions for group channels.
 * - **Owner (has DELETE_CHANNEL):** View Info, Mute/Unmute Group, Archive Group, Delete Group
 * - **Member (no DELETE_CHANNEL):** View Info, Mute/Unmute Group, Archive Group, Leave Group
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
private fun buildGroupChannelActions(
    selectedChannel: Channel,
    isMuted: Boolean,
    ownCapabilities: Set<String>,
    optionVisibility: ChannelOptionItemVisibility,
    channelName: String,
    viewModel: ChannelListViewModel,
    onViewInfoAction: (Channel) -> Unit,
): List<ChannelAction> {
    val canLeaveChannel = ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL)
    val canDeleteChannel = ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)
    val canMuteChannel = ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL)

    return listOfNotNull(
        if (optionVisibility.isViewInfoVisible) {
            ViewInfo(
                channel = selectedChannel,
                label = stringResource(id = R.string.stream_compose_selected_channel_menu_view_info),
                onAction = { onViewInfoAction(selectedChannel) },
            )
        } else {
            null
        },
        buildGroupMuteAction(
            canMuteChannel = optionVisibility.isMuteChannelVisible && canMuteChannel,
            isMuted = isMuted,
            selectedChannel = selectedChannel,
            viewModel = viewModel,
        ),
        buildPinAction(
            canPinChannel = optionVisibility.isPinChannelVisible,
            selectedChannel = selectedChannel,
            viewModel = viewModel,
        ),
        buildGroupArchiveAction(
            canArchiveChannel = optionVisibility.isArchiveChannelVisible,
            selectedChannel = selectedChannel,
            viewModel = viewModel,
        ),
        // Owner pattern: if user can delete, show Delete Group (not Leave)
        // Member pattern: if user can leave but not delete, show Leave Group
        if (optionVisibility.isLeaveChannelVisible && canLeaveChannel && !canDeleteChannel) {
            LeaveGroup(
                channel = selectedChannel,
                label = stringResource(id = R.string.stream_compose_selected_channel_menu_leave_group),
                onAction = { viewModel.leaveGroup(selectedChannel) },
                confirmationPopup = ConfirmationPopup(
                    title = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_leave_group_confirmation_title,
                    ),
                    message = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_leave_group_confirmation_message,
                        channelName,
                    ),
                    confirmButtonText = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_leave_group,
                    ),
                ),
            )
        } else {
            null
        },
        if (optionVisibility.isDeleteChannelVisible && canDeleteChannel) {
            DeleteConversation(
                channel = selectedChannel,
                label = stringResource(id = R.string.stream_compose_selected_channel_menu_delete_group),
                onAction = { viewModel.deleteConversation(selectedChannel) },
                confirmationPopup = ConfirmationPopup(
                    title = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_delete_group_confirmation_title,
                    ),
                    message = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_delete_group_confirmation_message,
                        channelName,
                    ),
                    confirmButtonText = stringResource(
                        id = R.string.stream_compose_selected_channel_menu_delete_group,
                    ),
                ),
            )
        } else {
            null
        },
    )
}

/**
 * Builds the pin action for the channel, based on the current state.
 */
@Composable
private fun buildPinAction(
    canPinChannel: Boolean,
    selectedChannel: Channel,
    viewModel: ChannelListViewModel,
): ChannelAction? = when (selectedChannel.isPinned().takeIf { canPinChannel }) {
    false -> PinChannel(
        channel = selectedChannel,
        label = stringResource(id = R.string.stream_compose_selected_channel_menu_pin_channel),
        onAction = { viewModel.pinChannel(selectedChannel) },
    )

    true -> UnpinChannel(
        channel = selectedChannel,
        label = stringResource(id = R.string.stream_compose_selected_channel_menu_unpin_channel),
        onAction = { viewModel.unpinChannel(selectedChannel) },
    )

    null -> null
}

/**
 * Builds the archive action for DM channels, using "Archive Chat" / "Unarchive Chat" labels.
 */
@Composable
private fun buildDmArchiveAction(
    canArchiveChannel: Boolean,
    selectedChannel: Channel,
    viewModel: ChannelListViewModel,
): ChannelAction? = when (selectedChannel.isArchive().takeIf { canArchiveChannel }) {
    false -> ArchiveChannel(
        channel = selectedChannel,
        label = stringResource(id = R.string.stream_compose_selected_channel_menu_archive_chat),
        onAction = { viewModel.archiveChannel(selectedChannel) },
    )

    true -> UnarchiveChannel(
        channel = selectedChannel,
        label = stringResource(id = R.string.stream_compose_selected_channel_menu_unarchive_chat),
        onAction = { viewModel.unarchiveChannel(selectedChannel) },
    )

    null -> null
}

/**
 * Builds the archive action for group channels, using "Archive Group" / "Unarchive Group" labels.
 */
@Composable
private fun buildGroupArchiveAction(
    canArchiveChannel: Boolean,
    selectedChannel: Channel,
    viewModel: ChannelListViewModel,
): ChannelAction? = when (selectedChannel.isArchive().takeIf { canArchiveChannel }) {
    false -> ArchiveChannel(
        channel = selectedChannel,
        label = stringResource(id = R.string.stream_compose_selected_channel_menu_archive_group),
        onAction = { viewModel.archiveChannel(selectedChannel) },
    )

    true -> UnarchiveChannel(
        channel = selectedChannel,
        label = stringResource(id = R.string.stream_compose_selected_channel_menu_unarchive_group),
        onAction = { viewModel.unarchiveChannel(selectedChannel) },
    )

    null -> null
}

/**
 * Builds the mute action for group channels, using "Mute Group" / "Unmute Group" labels.
 */
@Composable
private fun buildGroupMuteAction(
    canMuteChannel: Boolean,
    isMuted: Boolean,
    selectedChannel: Channel,
    viewModel: ChannelListViewModel,
): ChannelAction? = if (canMuteChannel) {
    when (isMuted) {
        true -> UnmuteChannel(
            channel = selectedChannel,
            label = stringResource(id = R.string.stream_compose_selected_channel_menu_unmute_group),
            onAction = { viewModel.unmuteChannel(selectedChannel) },
        )

        false -> MuteChannel(
            channel = selectedChannel,
            label = stringResource(id = R.string.stream_compose_selected_channel_menu_mute_group),
            onAction = { viewModel.muteChannel(selectedChannel) },
        )
    }
} else {
    null
}

/**
 * Preview of [ChannelOptions].
 *
 * Should show a list of available actions for the channel.
 */
@Preview(showBackground = true, name = "ChannelOptions Preview")
@Composable
private fun ChannelOptionsPreview() {
    ChatTheme {
        val channel = PreviewChannelData.channelWithMessages
        ChannelOptions(
            actions = listOf(
                ViewInfo(
                    channel = channel,
                    label = "Channel Info",
                    onAction = {},
                ),
                MuteChannel(
                    channel = channel,
                    label = "Mute Channel",
                    onAction = {},
                ),
                DeleteConversation(
                    channel = channel,
                    label = "Delete Conversation",
                    onAction = {},
                ),
            ),
            onChannelOptionConfirm = {},
        )
    }
}
