/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewChannelData
import io.getstream.chat.android.compose.state.channels.list.ChannelOptionState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.util.extensions.toSet
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ChannelCapabilities
import io.getstream.chat.android.ui.common.state.channels.actions.Cancel
import io.getstream.chat.android.ui.common.state.channels.actions.ChannelAction
import io.getstream.chat.android.ui.common.state.channels.actions.DeleteConversation
import io.getstream.chat.android.ui.common.state.channels.actions.LeaveGroup
import io.getstream.chat.android.ui.common.state.channels.actions.MuteChannel
import io.getstream.chat.android.ui.common.state.channels.actions.UnmuteChannel
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
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(color = ChatTheme.colors.borders),
            )

            ChannelOptionsItem(
                title = option.title,
                titleColor = option.titleColor,
                leadingIcon = {
                    Icon(
                        modifier = Modifier
                            .size(56.dp)
                            .padding(16.dp),
                        painter = option.iconPainter,
                        tint = option.iconColor,
                        contentDescription = null,
                    )
                },
                onClick = { onChannelOptionClick(option.action) },
            )
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
@Composable
public fun buildDefaultChannelOptionsState(
    selectedChannel: Channel,
    isMuted: Boolean,
    ownCapabilities: Set<String>,
): List<ChannelOptionState> {
    val canLeaveChannel = ownCapabilities.contains(ChannelCapabilities.LEAVE_CHANNEL)
    val canDeleteChannel = ownCapabilities.contains(ChannelCapabilities.DELETE_CHANNEL)

    return listOfNotNull(
        ChannelOptionState(
            title = stringResource(id = R.string.stream_compose_selected_channel_menu_view_info),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_person),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = ViewInfo(selectedChannel),
        ),
        if (canLeaveChannel) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_selected_channel_menu_leave_group),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_person_remove),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = LeaveGroup(selectedChannel),
            )
        } else {
            null
        },
        if (isMuted) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_selected_channel_menu_unmute_channel),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_unmute),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = UnmuteChannel(selectedChannel),
            )
        } else {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_selected_channel_menu_mute_channel),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_mute),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = MuteChannel(selectedChannel),
            )
        },
        if (canDeleteChannel) {
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
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_clear),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = Cancel,
        ),
    )
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
