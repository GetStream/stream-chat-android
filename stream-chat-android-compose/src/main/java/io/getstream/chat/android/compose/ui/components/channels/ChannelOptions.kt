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
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channel.list.Cancel
import io.getstream.chat.android.compose.state.channel.list.ChannelAction
import io.getstream.chat.android.compose.state.channel.list.ChannelOptionState
import io.getstream.chat.android.compose.state.channel.list.DeleteConversation
import io.getstream.chat.android.compose.state.channel.list.LeaveGroup
import io.getstream.chat.android.compose.state.channel.list.MuteChannel
import io.getstream.chat.android.compose.state.channel.list.UnmuteChannel
import io.getstream.chat.android.compose.state.channel.list.ViewInfo
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.isDistinct

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
            .wrapContentHeight()
    ) {
        items(options) { option ->
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(color = ChatTheme.colors.borders)
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
                        contentDescription = null
                    )
                },
                onClick = { onChannelOptionClick(option.action) }
            )
        }
    }
}

/**
 * Builds the default list of channel options, based on the current user and the state of the channel.
 *
 * @param selectedChannel The currently selected channel.
 * @param currentUser The currently logged in user.
 * @param isMuted If the channel is muted or not.
 * @param channelMembers The members of the channel.
 */
@Composable
public fun buildDefaultChannelOptionsState(
    selectedChannel: Channel,
    currentUser: User?,
    isMuted: Boolean,
    channelMembers: List<Member>,
): List<ChannelOptionState> {
    val canLeaveChannel = !selectedChannel.isDistinct()
    val canDeleteChannel = channelMembers.firstOrNull { it.user.id == currentUser?.id }
        ?.role
        ?.let { it == "admin" || it == "owner" }
        ?: false

    return listOfNotNull(
        ChannelOptionState(
            title = stringResource(id = R.string.stream_compose_channel_info_view_info),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_person),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = ViewInfo(selectedChannel)
        ),
        if (canLeaveChannel) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_channel_info_leave_group),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_person_remove),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = LeaveGroup(selectedChannel)
            )
        } else null,
        if (isMuted) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_channel_info_unmute_channel),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_unmute),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = UnmuteChannel(selectedChannel)
            )
        } else {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_channel_info_mute_channel),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_mute),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = MuteChannel(selectedChannel)
            )
        },
        if (canDeleteChannel) {
            ChannelOptionState(
                title = stringResource(id = R.string.stream_compose_channel_info_delete_conversation),
                titleColor = ChatTheme.colors.errorAccent,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_delete),
                iconColor = ChatTheme.colors.errorAccent,
                action = DeleteConversation(selectedChannel)
            )
        } else null,
        ChannelOptionState(
            title = stringResource(id = R.string.stream_compose_channel_info_dismiss),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_clear),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = Cancel,
        )
    )
}
