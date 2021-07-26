package io.getstream.chat.android.compose.ui.messages.header

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.MaterialTheme
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
import io.getstream.chat.android.client.models.name
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.state.messages.MessageMode
import io.getstream.chat.android.compose.state.messages.Normal
import io.getstream.chat.android.compose.ui.common.Avatar
import io.getstream.chat.android.compose.ui.common.BackButton
import io.getstream.chat.android.compose.ui.common.NetworkLoadingView
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.rememberChannelImagePainter

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [MessageListHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions.
 *
 * @param modifier - Modifier for styling.
 * @param channel - Channel info to display.
 * @param currentUser - The current user, required for different UI states.
 * @param isNetworkAvailable - A flag that governs if we show the subtitle or the network loading view.
 * @param messageMode - The current message mode, that changes the header content, if we're in a Thread.
 * @param onBackPressed - Handler that propagates the back button click event.
 * @param onHeaderActionClick - Action handler when the user taps on the header action.
 * */
@Composable
fun MessageListHeader(
    channel: Channel,
    currentUser: User?,
    isNetworkAvailable: Boolean,
    messageMode: MessageMode,
    onBackPressed: () -> Unit,
    onHeaderActionClick: () -> Unit,
    modifier: Modifier = Modifier,
) {

    val memberCount = channel.memberCount
    val onlineCount = channel.members.count { it.user.online }

    val channelName = if (channel.name.isNotEmpty()) channel.name else channel.id

    val title = if (messageMode == Normal) {
        channelName
    } else {
        stringResource(id = R.string.thread_title)
    }

    val subtitle = if (messageMode == Normal) {
        stringResource(id = R.string.channel_members, memberCount, onlineCount)
    } else {
        stringResource(id = R.string.thread_subtitle, channelName)
    }

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

            BackButton(
                modifier = Modifier.weight(1f),
                imageVector = Icons.Default.ArrowBack,
                onBackPressed = onBackPressed
            )

            MessagesHeaderTitle(
                modifier = Modifier
                    .weight(6f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onHeaderActionClick
                    ),
                title = title,
                subtitle = subtitle,
                isNetworkAvailable = isNetworkAvailable
            )

            val painter = rememberChannelImagePainter(channel = channel, currentUser = currentUser)

            Avatar(
                modifier = Modifier
                    .size(36.dp),
                painter = painter,
                contentDescription = channel.name
            )
        }
    }
}

/**
 * Wrapper for the title of the header, that handles if we should show a loading view for network,
 * or the channel information.
 *
 * @param modifier - Modifier for styling.
 * @param title - The title of the header.
 * @param subtitle - The subtitle of the header - usually information about channel members.
 * @param isNetworkAvailable - A flag that governs if we show the subtitle or the network loading view.
 * */
@Composable
private fun MessagesHeaderTitle(
    modifier: Modifier,
    title: String,
    subtitle: String,
    isNetworkAvailable: Boolean,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = ChatTheme.typography.bodyBold,
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
                textStyle = MaterialTheme.typography.body2,
                spinnerSize = 12.dp,
            )
        }
    }
}
