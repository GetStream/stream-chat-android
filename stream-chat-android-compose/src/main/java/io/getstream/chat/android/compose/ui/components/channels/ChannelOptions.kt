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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.client.extensions.isArchive
import io.getstream.chat.android.client.extensions.isPinned
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channels.list.ChannelOptionState
import io.getstream.chat.android.compose.ui.components.StreamHorizontalDivider
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.ui.common.state.channels.actions.ArchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.Cancel
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.LeaveGroup
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.PinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnarchiveChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnpinChannel
import io.getstream.chat.android.ui.common.state.channels.actions.ViewInfo

/**
 * This is the default bottom drawer UI that shows up when the user long taps on a channel item.
 *
 * It sets up different actions that we provide, based on user permissions.
 *
 * @param options The list of options to show in the UI, according to user permissions.
 * @param onChannelOptionClick Handler for when the user selects a channel action.
 * @param modifier Modifier for styling.
 */
@Composable
public fun ChannelOptions(
    options: List<ChannelOptionState>,
    onChannelOptionClick: (ChannelAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        items(options) { option ->
            StreamHorizontalDivider()

            with(ChatTheme.componentFactory) {
                ChannelOptionsItem(
                    modifier = Modifier,
                    option = option,
                    onClick = { onChannelOptionClick(option.action) },
                )
            }
        }
    }
}

/**
 * Builds the default list of channel options, based on the current user and the state of the channel.
 *
 * @param selectedChannel The currently selected channel.
 * @param isMuted If the channel is muted or not.
 * @param ownCapabilities Set of capabilities the user is given for the current channel.
 * @return The list of channel option items to display.
 */
@Suppress("LongMethod")
@Composable
public fun buildDefaultChannelOptionsState(
    selectedChannel: Channel,
    isMuted: Boolean,
    ownCapabilities: Set<String>,
): List<ChannelOptionState> {
    val canLeaveChannel = ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL)
    val canDeleteChannel = ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)
    val canMuteChannel = ownCapabilities.contains(ChannelCapabilities.MUTE_CHANNEL)

    val optionVisibility = ChatTheme.channelOptionsTheme.optionVisibility
    return listOfNotNull(
        if (optionVisibility.isViewInfoVisible) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_selected_channel_menu_view_info),
                titleColor = ChatTheme.colors.textPrimary, // was textHighEmphasis
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_person),
                iconColor = ChatTheme.colors.textSecondary, // was textLowEmphasis
                action = ViewInfo(selectedChannel),
            )
        } else {
            null
        },
        if (optionVisibility.isLeaveChannelVisible && canLeaveChannel) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_selected_channel_menu_leave_group),
                titleColor = ChatTheme.colors.textPrimary, // was textHighEmphasis
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_person_remove),
                iconColor = ChatTheme.colors.textSecondary, // was textLowEmphasis
                action = LeaveGroup(selectedChannel),
            )
        } else {
            null
        },
        buildMuteOption(
            canMuteChannel = optionVisibility.isMuteChannelVisible && canMuteChannel,
            isMuted = isMuted,
            selectedChannel = selectedChannel,
        ),
        buildPinOption(
            canPinChannel = optionVisibility.isPinChannelVisible,
            selectedChannel = selectedChannel,
        ),
        buildArchiveOption(
            canArchiveChannel = optionVisibility.isArchiveChannelVisible,
            selectedChannel = selectedChannel,
        ),
        if (optionVisibility.isDeleteChannelVisible && canDeleteChannel) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_selected_channel_menu_delete_conversation),
                titleColor = ChatTheme.colors.errorAccent,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_delete),
                iconColor = ChatTheme.colors.errorAccent,
                action = DeleteConversation(selectedChannel),
            )
        } else {
            null
        },
        ChannelOptionState(
            title = stringResource(id = R.string.stream_compose_selected_channel_menu_dismiss),
            titleColor = ChatTheme.colors.textPrimary, // was textHighEmphasis
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_clear),
            iconColor = ChatTheme.colors.textSecondary, // was textLowEmphasis
            action = Cancel,
        ),
    )
}

/**
 * Builds the pin option for the channel, based on the current state.
 *
 * @param canPinChannel If the user can pin the channel.
 * @param selectedChannel The currently selected channel.
 */
@Composable
private fun buildPinOption(
    canPinChannel: Boolean,
    selectedChannel: Channel,
) = when (selectedChannel.isPinned().takeIf { canPinChannel }) {
    false -> Triple(
        R.string.stream_compose_selected_channel_menu_pin_channel,
        R.drawable.stream_compose_ic_pin,
        PinChannel(selectedChannel),
    )

    true -> Triple(
        R.string.stream_compose_selected_channel_menu_unpin_channel,
        R.drawable.stream_compose_ic_unpin,
        UnpinChannel(selectedChannel),
    )

    null -> null
}?.let {
    ChannelOptionState(
        title = stringResource(id = it.first),
        titleColor = ChatTheme.colors.textPrimary, // was textHighEmphasis
        iconPainter = painterResource(id = it.second),
        iconColor = ChatTheme.colors.textSecondary, // was textLowEmphasis
        action = it.third,
    )
}

/**
 * Builds the archive option for the channel, based on the current state.
 *
 * @param canArchiveChannel If the user can archive the channel.
 * @param selectedChannel The currently selected channel.
 */
@Composable
private fun buildArchiveOption(
    canArchiveChannel: Boolean,
    selectedChannel: Channel,
) = when (selectedChannel.isArchive().takeIf { canArchiveChannel }) {
    false -> Triple(
        R.string.stream_compose_selected_channel_menu_archive_channel,
        R.drawable.stream_compose_ic_archive,
        ArchiveChannel(selectedChannel),
    )

    true -> Triple(
        R.string.stream_compose_selected_channel_menu_unarchive_channel,
        R.drawable.stream_compose_ic_unarchive,
        UnarchiveChannel(selectedChannel),
    )

    null -> null
}?.let {
    ChannelOptionState(
        title = stringResource(id = it.first),
        titleColor = ChatTheme.colors.textPrimary, // was textHighEmphasis
        iconPainter = painterResource(id = it.second),
        iconColor = ChatTheme.colors.textSecondary, // was textLowEmphasis
        action = it.third,
    )
}

@Composable
private fun buildMuteOption(
    canMuteChannel: Boolean,
    isMuted: Boolean,
    selectedChannel: Channel,
) = if (canMuteChannel) {
    val uiData = when (isMuted) {
        true -> Triple(
            R.string.stream_compose_selected_channel_menu_unmute_channel,
            R.drawable.stream_compose_ic_unmute,
            UnmuteChannel(selectedChannel),
        )

        false -> Triple(
            R.string.stream_compose_selected_channel_menu_mute_channel,
            R.drawable.stream_compose_ic_mute,
            MuteChannel(selectedChannel),
        )
    }

    ChannelOptionState(
        title = stringResource(id = uiData.first),
        titleColor = ChatTheme.colors.textPrimary, // was textHighEmphasis
        iconPainter = painterResource(id = uiData.second),
        iconColor = ChatTheme.colors.textSecondary, // was textLowEmphasis
        action = uiData.third,
    )
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
        ChannelOptions(
            options = buildDefaultChannelOptionsState(
                selectedChannel = PreviewChannelData.channelWithMessages,
                isMuted = false,
                ownCapabilities = ChannelCapabilities.toSet(),
            ),
            onChannelOptionClick = {},
        )
    }
}
