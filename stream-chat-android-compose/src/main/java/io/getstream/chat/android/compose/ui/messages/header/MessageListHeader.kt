package io.getstream.chat.android.compose.ui.messages.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.MessageMode
import io.getstream.chat.android.compose.state.messages.Normal
import io.getstream.chat.android.compose.state.messages.Thread
import io.getstream.chat.android.compose.ui.common.BackButton
import io.getstream.chat.android.compose.ui.common.NetworkLoadingView
import io.getstream.chat.android.compose.ui.common.avatar.ChannelAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getDisplayName

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [MessageListHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions, as well as customize the slot APIs.
 *
 * @param channel Channel info to display.
 * @param currentUser The current user, required for different UI states.
 * @param modifier Modifier for styling.
 * @param messageMode The current message mode, that changes the header content, if we're in a Thread.
 * @param isNetworkAvailable A flag that governs if we show the subtitle or the network loading view.
 * @param onBackPressed Handler that propagates the back button click event.
 * @param onHeaderActionClick Action handler when the user taps on the header action.
 * @param leadingContent The content shown at the start of the header, by default a [BackButton].
 * @param titleContent The content shown in the middle of the header and represents the core information, by default
 * [DefaultMessageHeaderTitle].
 * @param trailingContent The content shown at the end of the header, by default a [ChannelAvatar].
 */
@Composable
public fun MessageListHeader(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    messageMode: MessageMode = Normal,
    isNetworkAvailable: Boolean = true,
    onBackPressed: () -> Unit = {},
    onHeaderActionClick: (Channel) -> Unit = {},
    leadingContent: @Composable RowScope.() -> Unit = {
        BackButton(
            modifier = Modifier
                .size(36.dp),
            imageVector = Icons.Default.ArrowBack,
            onBackPressed = onBackPressed,
        )
    },

    titleContent: @Composable RowScope.() -> Unit = {
        DefaultMessageHeaderTitle(
            modifier = Modifier.weight(1f),
            channel = channel,
            messageMode = messageMode,
            onHeaderActionClick = onHeaderActionClick,
            isNetworkAvailable = isNetworkAvailable
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        ChannelAvatar(
            modifier = Modifier.size(36.dp),
            channel = channel,
            currentUser = currentUser,
            contentDescription = channel.name,
        )
    },
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        elevation = 4.dp,
        color = ChatTheme.colors.barsBackground,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {

            leadingContent()

            titleContent()

            trailingContent()
        }
    }
}

/**
 * Default header title, that handles if we should show a loading view for network,
 * or the channel information.
 *
 * @param channel The channel used for the title information.
 * @param modifier Modifier for styling.
 * @param messageMode Currently active message mode, used to define the title information.
 * @param onHeaderActionClick Handler for when the user taps on the header content.
 * @param isNetworkAvailable A flag that governs if we show the subtitle or the network loading view.
 */
@Composable
public fun DefaultMessageHeaderTitle(
    channel: Channel,
    modifier: Modifier,
    messageMode: MessageMode = Normal,
    onHeaderActionClick: (Channel) -> Unit = {},
    isNetworkAvailable: Boolean = true,
) {

    val title = when (messageMode) {
        Normal -> channel.getDisplayName()
        is Thread -> stringResource(id = R.string.stream_compose_thread_title)
    }

    val subtitle = when (messageMode) {
        Normal -> stringResource(
            id = R.string.stream_compose_channel_members,
            channel.memberCount,
            channel.members.count { it.user.online }
        )
        is Thread -> stringResource(id = R.string.stream_compose_thread_subtitle, channel.getDisplayName())
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onHeaderActionClick(channel) }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = ChatTheme.typography.title3Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
        )

        if (isNetworkAvailable) {
            Text(
                text = subtitle,
                color = ChatTheme.colors.textLowEmphasis,
                style = ChatTheme.typography.footnote,
            )
        } else {
            NetworkLoadingView(
                modifier = Modifier.wrapContentHeight(),
                spinnerSize = 12.dp,
            )
        }
    }
}
