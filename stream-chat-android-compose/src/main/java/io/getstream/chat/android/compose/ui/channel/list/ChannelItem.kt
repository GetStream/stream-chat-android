package io.getstream.chat.android.compose.ui.channel.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.state.channel.list.ChannelItemState
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.components.channels.ChannelDetails
import io.getstream.chat.android.compose.ui.components.channels.ChannelLastMessageInfo
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * The basic channel item, that shows the channel in a list and exposes single and long click actions.
 *
 * @param channelItem The channel data to show.
 * @param currentUser The user that's currently logged in.
 * @param onChannelClick Handler for a single tap on an item.
 * @param onChannelLongClick Handler for a long tap on an item.
 * @param modifier Modifier for styling.
 * @param leadingContent Customizable composable function that represents the leading content of a channel item, usually
 * the avatar that holds an image of the channel or its members.
 * @param detailsContent Customizable composable function that represents the center content of a channel item, usually
 * holding information about its name and the last message.
 * @param trailingContent Customizable composable function that represents the trailing content of the a channel item,
 * usually information about the last message and the number of unread messages.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun DefaultChannelItem(
    channelItem: ChannelItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(ChannelItemState) -> Unit = {
        ChannelAvatar(
            modifier = Modifier
                .padding(end = ChatTheme.dimens.channelItemHorizontalPadding)
                .size(ChatTheme.dimens.channelAvatarSize),
            channel = it.channel,
            currentUser = currentUser
        )
    },
    detailsContent: @Composable RowScope.(ChannelItemState) -> Unit = {
        ChannelDetails(
            channel = it.channel,
            isMuted = it.isMuted,
            currentUser = currentUser,
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
        )
    },
    trailingContent: @Composable RowScope.(ChannelItemState) -> Unit = {
        ChannelLastMessageInfo(
            channel = it.channel,
            currentUser = currentUser,
            modifier = Modifier
                .padding(start = ChatTheme.dimens.channelItemHorizontalPadding)
                .wrapContentHeight()
                .align(Alignment.Bottom)
        )
    },
) {
    val channel = channelItem.channel

    Column(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(color = ChatTheme.colors.appBackground)
            .combinedClickable(
                onClick = { onChannelClick(channel) },
                onLongClick = { onChannelLongClick(channel) },
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = ChatTheme.dimens.channelItemVerticalPadding,
                    horizontal = ChatTheme.dimens.channelItemHorizontalPadding
                ),
            verticalAlignment = CenterVertically,
        ) {
            leadingContent(channelItem)

            detailsContent(channelItem)

            trailingContent(channelItem)
        }
    }
}
