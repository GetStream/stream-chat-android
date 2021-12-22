package io.getstream.chat.android.compose.ui.channels.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewChannelData
import io.getstream.chat.android.compose.previewdata.PreviewUserData
import io.getstream.chat.android.compose.state.channel.list.ChannelItemState
import io.getstream.chat.android.compose.ui.components.Timestamp
import io.getstream.chat.android.compose.ui.components.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.components.channels.MessageReadStatusIcon
import io.getstream.chat.android.compose.ui.components.channels.UnreadCountIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastMessage

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
 * @param centerContent Customizable composable function that represents the center content of a channel item, usually
 * holding information about its name and the last message.
 * @param trailingContent Customizable composable function that represents the trailing content of the a channel item,
 * usually information about the last message and the number of unread messages.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
public fun ChannelItem(
    channelItem: ChannelItemState,
    currentUser: User?,
    onChannelClick: (Channel) -> Unit,
    onChannelLongClick: (Channel) -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.(ChannelItemState) -> Unit = {
        DefaultChannelAvatar(channelItem = it, currentUser = currentUser)
    },
    centerContent: @Composable RowScope.(ChannelItemState) -> Unit = {
        DefaultChannelCenterContent(
            channel = it.channel,
            isMuted = it.isMuted,
            currentUser = currentUser,
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
        )
    },
    trailingContent: @Composable RowScope.(ChannelItemState) -> Unit = {
        DefaultChannelTrailingContent(
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent(channelItem)

            centerContent(channelItem)

            trailingContent(channelItem)
        }
    }
}

/**
 * Represents the default channel avatar.
 *
 * @param channelItem The channel to show the avatar of.
 * @param currentUser The currently logged in user.
 */
@Composable
internal fun DefaultChannelAvatar(
    channelItem: ChannelItemState,
    currentUser: User?,
) {
    ChannelAvatar(
        modifier = Modifier
            .padding(end = ChatTheme.dimens.channelItemHorizontalPadding)
            .size(ChatTheme.dimens.channelAvatarSize),
        channel = channelItem.channel,
        currentUser = currentUser
    )
}

/**
 * Represents the center portion of the channel item, that shows the channel display name and the last message text
 * preview.
 *
 * @param channel The channel to show the info for.
 * @param isMuted If the channel is muted for the current user.
 * @param currentUser The currently logged in user, used for data handling.
 * @param modifier Modifier for styling.
 */
@Composable
internal fun DefaultChannelCenterContent(
    channel: Channel,
    isMuted: Boolean,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        val channelName: (@Composable (modifier: Modifier) -> Unit) = @Composable {
            Text(
                modifier = it,
                text = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
                style = ChatTheme.typography.bodyBold,
                fontSize = 16.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }

        if (isMuted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                channelName(Modifier.weight(weight = 1f, fill = false))

                Icon(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(16.dp),
                    painter = painterResource(id = R.drawable.stream_compose_ic_muted),
                    contentDescription = null,
                    tint = ChatTheme.colors.textLowEmphasis,
                )
            }
        } else {
            channelName(Modifier)
        }

        val lastMessageText = channel.getLastMessage(currentUser)?.let { lastMessage ->
            ChatTheme.messagePreviewFormatter.formatMessagePreview(lastMessage, currentUser)
        } ?: AnnotatedString("")

        if (lastMessageText.isNotEmpty()) {
            Text(
                text = lastMessageText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = ChatTheme.typography.body,
                color = ChatTheme.colors.textLowEmphasis,
            )
        }
    }
}

/**
 * Represents the information about the last message for the channel item, such as its read state and how many unread
 * messages the user has.
 *
 * @param channel The channel to show the info for.
 * @param currentUser The currently logged in user, used for data handling.
 * @param modifier Modifier for styling.
 */
@Composable
public fun DefaultChannelTrailingContent(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
) {
    val lastMessage = channel.getLastMessage(currentUser)

    if (lastMessage != null) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.End
        ) {
            val unreadCount = channel.unreadCount

            if (unreadCount != null && unreadCount > 0) {
                UnreadCountIndicator(
                    modifier = Modifier.padding(bottom = 4.dp),
                    unreadCount = unreadCount
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                MessageReadStatusIcon(
                    channel = channel,
                    lastMessage = lastMessage,
                    currentUser = currentUser,
                    modifier = Modifier
                        .padding(end = ChatTheme.dimens.channelItemHorizontalPadding)
                        .size(16.dp)
                )

                Timestamp(date = channel.lastUpdated)
            }
        }
    }
}

/**
 * Preview of [DefaultChannelCenterContent] component for one-to-one conversation.
 *
 * Should show a user name and the last message in the channel.
 */
@Preview(showBackground = true, name = "ChannelDetails Preview (One-to-one conversation)")
@Composable
private fun DefaultChannelCenterContentOneToOnePreview() {
    DefaultChannelDetailsPreview(
        channel = PreviewChannelData.channelWithMessages,
        isMuted = false,
        currentUser = PreviewUserData.user1
    )
}

/**
 * Preview of [DefaultChannelCenterContent] for muted channel.
 *
 * Should show a muted icon next to the channel name.
 */
@Preview(showBackground = true, name = "ChannelDetails Preview (Muted channel)")
@Composable
private fun DefaultChannelCenterContentMutedPreview() {
    DefaultChannelDetailsPreview(
        channel = PreviewChannelData.channelWithMessages,
        isMuted = true
    )
}

/**
 * Preview of [DefaultChannelCenterContent] for a channel without messages.
 *
 * Should show only channel name that is centered vertically.
 */
@Preview(showBackground = true, name = "ChannelDetails Preview (Without message)")
@Composable
private fun DefaultChannelCenterContentWithMessagePreview() {
    DefaultChannelDetailsPreview(channel = PreviewChannelData.channelWithImage)
}

/**
 * Shows [DefaultChannelCenterContent] preview for the provided parameters.
 *
 * @param channel The channel used to show the preview.
 * @param isMuted If the channel is muted.
 * @param currentUser The currently logged in user.
 */
@Composable
private fun DefaultChannelDetailsPreview(
    channel: Channel,
    isMuted: Boolean = false,
    currentUser: User? = null,
) {
    ChatTheme {
        DefaultChannelCenterContent(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            channel = channel,
            isMuted = isMuted,
            currentUser = currentUser
        )
    }
}

/**
 * Preview of [DefaultChannelTrailingContent].
 *
 * Should show unread count badge, delivery indicator and timestamp.
 */
@Preview(showBackground = true, name = "ChannelLastMessageInfo Preview")
@Composable
private fun DefaultChannelTrailingContentPreview() {
    ChatTheme {
        DefaultChannelTrailingContent(
            channel = PreviewChannelData.channelWithMessages,
            currentUser = PreviewUserData.user1,
        )
    }
}
