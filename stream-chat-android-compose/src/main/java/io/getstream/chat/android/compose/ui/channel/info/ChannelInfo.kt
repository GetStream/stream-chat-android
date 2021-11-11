package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.Member
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.channel.list.Cancel
import io.getstream.chat.android.compose.state.channel.list.ChannelListAction
import io.getstream.chat.android.compose.state.channel.list.ChannelOption
import io.getstream.chat.android.compose.state.channel.list.DeleteConversation
import io.getstream.chat.android.compose.state.channel.list.LeaveGroup
import io.getstream.chat.android.compose.state.channel.list.ViewInfo
import io.getstream.chat.android.compose.ui.common.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.compose.ui.util.isDistinct

/**
 * Shows special UI when an item is selected.
 * It also prepares the available options for the channel, based on if we're an admin or not.
 *
 * @param selectedChannel The channel the user selected.
 * @param currentUser The currently logged-in user data.
 * @param onChannelOptionClick Handler for when the user selects a channel option.
 * @param modifier Modifier for styling.
 * @param shape The shape of the component.
 */
@Composable
public fun ChannelInfo(
    selectedChannel: Channel,
    currentUser: User?,
    onChannelOptionClick: (ChannelListAction) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.bottomSheet,
) {
    val channelMembers = selectedChannel.members

    val canLeaveChannel = !selectedChannel.isDistinct()
    val canDeleteChannel = channelMembers.firstOrNull { it.user.id == currentUser?.id }
        ?.role
        ?.let { it == "admin" || it == "owner" }
        ?: false

    val otherMembers = channelMembers.filter { it.user.id != currentUser?.id }

    val subtitle = when {
        otherMembers.isEmpty() -> ""
        otherMembers.size == 1 -> otherMembers.first().user.getLastSeenText(LocalContext.current)
        else -> {
            LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_channel_members,
                otherMembers.count(),
                otherMembers.count(),
                otherMembers.count { it.user.online }
            )
        }
    }

    val channelOptions = listOfNotNull(
        ChannelOption(
            title = stringResource(id = R.string.stream_compose_channel_info_view_info),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_person),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = ViewInfo(selectedChannel)
        ),
        if (canLeaveChannel) {
            ChannelOption(
                title = stringResource(id = R.string.stream_compose_channel_info_leave_group),
                titleColor = ChatTheme.colors.textHighEmphasis,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_person_remove),
                iconColor = ChatTheme.colors.textLowEmphasis,
                action = LeaveGroup(selectedChannel)
            )
        } else null,
        if (canDeleteChannel) {
            ChannelOption(
                title = stringResource(id = R.string.stream_compose_channel_info_delete_conversation),
                titleColor = ChatTheme.colors.errorAccent,
                iconPainter = painterResource(id = R.drawable.stream_compose_ic_delete),
                iconColor = ChatTheme.colors.errorAccent,
                action = DeleteConversation(selectedChannel)
            )
        } else null,
        ChannelOption(
            title = stringResource(id = R.string.stream_compose_channel_info_dismiss),
            titleColor = ChatTheme.colors.textHighEmphasis,
            iconPainter = painterResource(id = R.drawable.stream_compose_ic_clear),
            iconColor = ChatTheme.colors.textLowEmphasis,
            action = Cancel,
        )
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
                    text = subtitle,
                    style = ChatTheme.typography.footnoteBold,
                    color = ChatTheme.colors.textLowEmphasis,
                )

                ChannelMembers(otherMembers)

                ChannelOptions(channelOptions, onChannelOptionClick)
            }
        }
    }
}

/**
 * Represents a list of members in the channel.
 *
 * @param members The list of channel members.
 * @param modifier Modifier for styling.
 */
@Composable
private fun ChannelMembers(
    members: List<Member>,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalArrangement = Arrangement.Center,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
    ) {
        items(members) { member ->
            ChannelInfoUserItem(
                modifier = Modifier
                    .width(ChatTheme.dimens.channelInfoUserItemWidth)
                    .padding(horizontal = ChatTheme.dimens.channelInfoUserItemHorizontalPadding),
                member = member,
            )
        }
    }
}

/**
 * The UI component that shows a user avatar and user name, as a member of a channel.
 *
 * @param modifier Modifier for styling.
 * @param member The member data to show.
 */
@Composable
private fun ChannelInfoUserItem(
    member: Member,
    modifier: Modifier = Modifier,
) {
    val memberName = member.user.name

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        UserAvatar(
            modifier = Modifier.size(ChatTheme.dimens.channelInfoUserItemAvatarSize),
            user = member.user,
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
 * @param options The list of options to show in the UI, according to user permissions.
 * @param onChannelOptionClick Handler for when the user selects a channel action.
 * @param modifier Modifier for styling.
 */
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
                    .height(0.5.dp)
                    .background(color = ChatTheme.colors.borders)
            )

            ChannelOptionItem(
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
 * Default component for channel info options.
 *
 * @param title The text title of the action.
 * @param titleColor The color of the title.
 * @param leadingIcon The composable that defines the leading icon for the action.
 * @param onClick The action to perform once the user taps on any option.
 * @param modifier Modifier for styling.
 */
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
            .height(56.dp)
            .clickable(
                onClick = onClick,
                indication = rememberRipple(),
                interactionSource = remember { MutableInteractionSource() }
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        leadingIcon()

        Text(
            text = title,
            style = ChatTheme.typography.bodyBold,
            color = titleColor
        )
    }
}
