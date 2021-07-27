package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.client.models.image
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channel.list.Cancel
import io.getstream.chat.android.compose.state.channel.list.ChannelListAction
import io.getstream.chat.android.compose.state.channel.list.ChannelOption
import io.getstream.chat.android.compose.state.channel.list.DeleteConversation
import io.getstream.chat.android.compose.state.channel.list.LeaveGroup
import io.getstream.chat.android.compose.state.channel.list.ViewInfo
import io.getstream.chat.android.compose.ui.theme.ChatTheme

/**
 * Shows special UI when an item is selected.
 * It also prepares the available options for the channel, based on if we're an admin or not.
 *
 * @param selectedChannel - The channel the user selected.
 * @param user - Currently logged-in user data.
 * @param onChannelOptionClick - Handler for when the user selects a channel option.
 * @param modifier - Modifier for styling.
 * */
@Composable
public fun ChannelInfo(
    selectedChannel: Channel,
    user: User?,
    onChannelOptionClick: (ChannelListAction) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
) {
    val isAdmin = selectedChannel.members.firstOrNull { it.user.id == user?.id }?.role == "admin"

    val channelMembers = selectedChannel.members
    val onlineMembers = channelMembers.count { it.user.online }

    val title = channelMembers.fold("") { current, member ->
        if (current.isEmpty()) {
            member.user.name
        } else {
            "$current, ${member.user.name}"
        }
    }

    val channelOptions = mutableListOf(
        ChannelOption(
            title = stringResource(id = R.string.stream_compose_view_info),
            titleColor = ChatTheme.colors.textHighEmphasis,
            icon = Icons.Default.Person,
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = ViewInfo(selectedChannel)
        ),
        ChannelOption(
            title = stringResource(id = R.string.stream_compose_leave_group),
            titleColor = ChatTheme.colors.textHighEmphasis,
            icon = Icons.Default.PersonRemove,
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = LeaveGroup(selectedChannel)
        ),
        ChannelOption(
            title = stringResource(id = R.string.stream_compose_cancel),
            titleColor = ChatTheme.colors.textHighEmphasis,
            icon = Icons.Default.Cancel,
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = Cancel,
        )
    )

    if (isAdmin) {
        channelOptions.add(
            2,
            ChannelOption(
                title = stringResource(id = R.string.stream_compose_delete_conversation),
                titleColor = ChatTheme.colors.errorAccent,
                icon = Icons.Default.Delete,
                iconColor = ChatTheme.colors.errorAccent,
                action = DeleteConversation(selectedChannel)
            )
        )
    }

    Card(
        modifier,
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
                text = title,
                style = ChatTheme.typography.bodyBold,
                color = ChatTheme.colors.textHighEmphasis,
            )
            Text(
                text = stringResource(
                    id = R.string.stream_compose_channel_members,
                    channelMembers.size,
                    onlineMembers
                ),
                style = ChatTheme.typography.footnoteBold,
                color = ChatTheme.colors.textLowEmphasis,
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp)
                    .align(Alignment.CenterHorizontally),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
            ) {
                items(selectedChannel.members) { member ->
                    ChannelInfoUserItem(member = member)
                }
            }

            ChannelOptions(channelOptions, onChannelOptionClick)
        }
    }
}

/**
 * The UI component that shows a user avatar and user name, as a member of a channel.
 *
 * @param modifier - Modifier for styling.
 * @param member - The member data to show.
 * */
@Composable
private fun ChannelInfoUserItem(
    member: Member,
    modifier: Modifier = Modifier,
) {
    val avatarPainter = rememberImagePainter(member.user.image)
    val memberName = member.user.name

    Column(
        modifier = modifier
            .width(48.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Image(
            modifier = modifier
                .size(48.dp)
                .clip(CircleShape),
            painter = avatarPainter,
            contentDescription = memberName
        )

        Text(
            text = memberName,
            style = ChatTheme.typography.footnoteBold,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = ChatTheme.colors.textHighEmphasis,
        )
    }
}

/**
 * This is the default bottom drawer UI that shows up when the user long taps on a channel item.
 *
 * It sets up different actions that we provide, based on user permissions.
 *
 * @param options - The list of options to show in the UI, according to user permissions.
 * @param onChannelOptionClick - Handler for when the user selects a channel action.
 * @param modifier - Modifier for styling.
 * */
@Composable
private fun ChannelOptions(
    options: List<ChannelOption>,
    onChannelOptionClick: (ChannelListAction) -> Unit,
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
                    .height(1.dp)
                    .background(color = Color.LightGray, shape = RectangleShape)
            )

            ChannelOptionItem(
                title = option.title,
                titleColor = option.titleColor,
                leadingIcon = {
                    Icon(
                        imageVector = option.icon,
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
 * Default component for channel info options.
 *
 * @param title - The text title of the action.
 * @param titleColor - The color of the title.
 * @param leadingIcon - The composable that defines the leading icon for the action.
 * @param onClick - The action to perform once the user taps on any option.
 * @param modifier - Modifier for styling.
 * */
@Composable
private fun ChannelOptionItem(
    title: String,
    titleColor: Color,
    leadingIcon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        leadingIcon()

        Text(
            modifier = Modifier.padding(start = 12.dp),
            text = title,
            style = ChatTheme.typography.footnoteBold,
            color = titleColor
        )
    }
}
