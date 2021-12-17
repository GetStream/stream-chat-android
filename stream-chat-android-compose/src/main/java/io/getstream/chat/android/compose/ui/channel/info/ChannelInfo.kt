package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.channel.list.Cancel
import io.getstream.chat.android.compose.state.channel.list.ChannelAction
import io.getstream.chat.android.compose.ui.components.channels.ChannelMembers
import io.getstream.chat.android.compose.ui.components.channels.ChannelOptions
import io.getstream.chat.android.compose.ui.components.channels.buildDefaultChannelOptionsState
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.compose.ui.util.isOneToOne

/**
 * Shows special UI when an item is selected.
 * It also prepares the available options for the channel, based on if we're an admin or not.
 *
 * @param selectedChannel The channel the user selected.
 * @param isMuted If the channel is muted for the current user.
 * @param currentUser The currently logged-in user data.
 * @param onChannelOptionClick Handler for when the user selects a channel option.
 * @param modifier Modifier for styling.
 * @param shape The shape of the component.
 */
@Composable
public fun ChannelInfo(
    selectedChannel: Channel,
    isMuted: Boolean,
    currentUser: User?,
    onChannelOptionClick: (ChannelAction) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
) {
    val channelMembers = selectedChannel.members
    val membersToDisplay = if (selectedChannel.isOneToOne(currentUser)) {
        channelMembers.filter { it.user.id != currentUser?.id }
    } else {
        channelMembers
    }

    val channelOptions = buildDefaultChannelOptionsState(
        selectedChannel = selectedChannel,
        currentUser = currentUser,
        isMuted = isMuted,
        channelMembers = channelMembers
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ChatTheme.colors.overlay)
            .clickable(
                indication = null,
                interactionSource = MutableInteractionSource(),
                onClick = { onChannelOptionClick(Cancel) }
            )
    ) {
        Card(
            modifier
                .clickable(
                    indication = null,
                    interactionSource = MutableInteractionSource(),
                    onClick = { }
                ),
            elevation = 8.dp,
            shape = shape,
            backgroundColor = ChatTheme.colors.barsBackground,
        ) {
            Column(
                modifier = Modifier.padding(top = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = ChatTheme.channelNameFormatter.formatChannelName(selectedChannel),
                    style = ChatTheme.typography.title3Bold,
                    color = ChatTheme.colors.textHighEmphasis,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = selectedChannel.getMembersStatusText(LocalContext.current, currentUser),
                    style = ChatTheme.typography.footnoteBold,
                    color = ChatTheme.colors.textLowEmphasis,
                )

                ChannelMembers(membersToDisplay)

                ChannelOptions(channelOptions, onChannelOptionClick)
            }
        }
    }
}
