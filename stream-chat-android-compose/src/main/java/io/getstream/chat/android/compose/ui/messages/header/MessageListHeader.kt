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

package io.getstream.chat.android.compose.ui.messages.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.TypingIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.compose.ui.util.ifNotNull
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageMode

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [MessageListHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions, as well as customize the slot APIs.
 *
 * @param channel Channel info to display.
 * @param currentUser The current user, required for different UI states.
 * @param connectionState The state of WS connection used to switch between the subtitle and the network loading view.
 * @param modifier Modifier for styling.
 * @param typingUsers The list of typing users.
 * @param messageMode The current message mode, that changes the header content, if we're in a Thread.
 * @param color The color of the header.
 * @param shape The shape of the header.
 * @param elevation The elevation of the header.
 * @param onBackPressed Handler that propagates the back button click event.
 * @param onHeaderTitleClick Action handler when the user taps on the header title section.
 * @param onChannelAvatarClick Action handler called when the user taps on the channel avatar.
 * @param leadingContent The content shown at the start of the header, by default a [BackButton].
 * @param centerContent The content shown in the middle of the header and represents the core information, by default
 * [DefaultMessageListHeaderCenterContent].
 * @param trailingContent The content shown at the end of the header, by default the channel avatar.
 */
@Composable
public fun MessageListHeader(
    channel: Channel,
    currentUser: User?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    typingUsers: List<User> = emptyList(),
    messageMode: MessageMode = MessageMode.Normal,
    color: Color = ChatTheme.colors.backgroundElevationElevation1,
    shape: Shape = ChatTheme.shapes.header,
    elevation: Dp = ChatTheme.dimens.headerElevation,
    onBackPressed: () -> Unit = {},
    onHeaderTitleClick: ((Channel) -> Unit)? = null,
    onChannelAvatarClick: (() -> Unit)? = null,
    leadingContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageListHeaderLeadingContent(
                onBackPressed = onBackPressed,
            )
        }
    },
    centerContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            if (onHeaderTitleClick == null) {
                MessageListHeaderCenterContent(
                    modifier = Modifier.weight(1f),
                    channel = channel,
                    currentUser = currentUser,
                    connectionState = connectionState,
                    typingUsers = typingUsers,
                    messageMode = messageMode,
                    onClick = null,
                )
            } else {
                MessageListHeaderCenterContent(
                    modifier = Modifier.weight(1f),
                    channel = channel,
                    currentUser = currentUser,
                    typingUsers = typingUsers,
                    messageMode = messageMode,
                    onHeaderTitleClick = onHeaderTitleClick,
                    connectionState = connectionState,
                )
            }
        }
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            MessageListHeaderTrailingContent(
                channel = channel,
                currentUser = currentUser,
                onClick = onChannelAvatarClick,
            )
        }
    },
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = elevation,
        color = color,
        shape = shape,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingContent()

            centerContent()

            trailingContent()
        }
    }
}

/**
 * Represents the leading content of [MessageListHeader]. By default shows a back button.
 *
 * @param onBackPressed Handler that propagates the back button click event.
 */
@Composable
internal fun DefaultMessageListHeaderLeadingContent(onBackPressed: () -> Unit) {
    BackButton(
        painter = painterResource(id = R.drawable.stream_compose_ic_arrow_back),
        onBackPressed = onBackPressed,
    )
}

/**
 * Represents the center content of [MessageListHeader]. By default shows header title, that handles
 * if we should show a loading view for network, or the channel information.
 *
 * @param channel The channel used for the title information.
 * @param currentUser The current user.
 * @param connectionState A flag that governs if we show the subtitle or the network loading view.
 * @param modifier Modifier for styling.
 * @param typingUsers The list of typing users.
 * @param messageMode Currently active message mode, used to define the title information.
 * @param onHeaderTitleClick Handler for when the user taps on the header title section.
 */
@Suppress("LongMethod")
@Composable
public fun DefaultMessageListHeaderCenterContent(
    channel: Channel,
    currentUser: User?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    typingUsers: List<User> = emptyList(),
    messageMode: MessageMode = MessageMode.Normal,
    onHeaderTitleClick: ((Channel) -> Unit)? = null,
) {
    val title = when (messageMode) {
        MessageMode.Normal -> ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
        is MessageMode.MessageThread -> stringResource(id = R.string.stream_compose_thread_title)
    }

    val subtitle = when (messageMode) {
        MessageMode.Normal -> channel.getMembersStatusText(
            context = LocalContext.current,
            currentUser = currentUser,
            userPresence = ChatTheme.userPresence,
        )

        is MessageMode.MessageThread -> stringResource(
            R.string.stream_compose_thread_subtitle,
            ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
        )
    }

    Column(
        modifier = modifier
            .height(IntrinsicSize.Max)
            .run {
                if (onHeaderTitleClick != null) {
                    clickable { onHeaderTitleClick(channel) }
                } else {
                    this
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.testTag("Stream_ChannelName"),
            text = title,
            style = ChatTheme.typography.headingMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textPrimary,
        )

        when (connectionState) {
            is ConnectionState.Connected -> {
                DefaultMessageListHeaderSubtitle(
                    subtitle = subtitle,
                    typingUsers = typingUsers,
                )
            }

            is ConnectionState.Connecting -> {
                NetworkLoadingIndicator(
                    modifier = Modifier.wrapContentHeight(),
                    spinnerSize = 12.dp,
                    textColor = ChatTheme.colors.textSecondary,
                    textStyle = ChatTheme.typography.metadataDefault,
                )
            }

            is ConnectionState.Offline -> {
                Text(
                    text = stringResource(id = R.string.stream_compose_disconnected),
                    color = ChatTheme.colors.textSecondary,
                    style = ChatTheme.typography.metadataDefault,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Represents the default message list header subtitle, which shows either the number of people online
 * and total member count or the currently typing users.
 *
 * @param subtitle The subtitle to show.
 * @param typingUsers Currently typing users.
 */
@Composable
internal fun DefaultMessageListHeaderSubtitle(
    subtitle: String,
    typingUsers: List<User>,
) {
    val textColor = ChatTheme.colors.textSecondary
    val textStyle = ChatTheme.typography.metadataDefault

    if (typingUsers.isEmpty()) {
        Text(
            modifier = Modifier.testTag("Stream_ParticipantsInfo"),
            text = subtitle,
            color = textColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    } else {
        Row(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val typingUsersText = LocalContext.current.resources.getQuantityString(
                R.plurals.stream_compose_message_list_header_typing_users,
                typingUsers.size,
                typingUsers.first().name,
                typingUsers.size - 1,
            )

            TypingIndicator()

            Text(
                modifier = Modifier.testTag("Stream_MessageListTypingIndicator"),
                text = typingUsersText,
                color = textColor,
                style = textStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * Represents the trailing content of [MessageListHeader]. By default shows the channel avatar.
 *
 * @param channel The channel used to display the avatar.
 * @param currentUser The current user. Used for choosing which avatar to display.
 * @param onClick The handler called when the user taps on the channel avatar.
 */
@Composable
internal fun DefaultMessageListHeaderTrailingContent(
    channel: Channel,
    currentUser: User?,
    onClick: (() -> Unit)?,
) {
    ChatTheme.componentFactory.ChannelAvatar(
        modifier = Modifier
            .size(ChatTheme.dimens.channelAvatarSize)
            .ifNotNull(onClick) { clickable(onClick = it) },
        channel = channel,
        currentUser = currentUser,
        showIndicator = false,
        showBorder = false,
    )
}

@Preview(name = "MessageListHeader Preview (Connected)")
@Composable
private fun MessageListHeaderConnectedPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
        )
    }
}

@Preview(name = "MessageListHeader Preview (Connecting)")
@Composable
private fun MessageListHeaderConnectingPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connecting,
        )
    }
}

@Preview(name = "MessageListHeader Preview (Offline)")
@Composable
private fun MessageListHeaderOfflinePreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Offline,
        )
    }
}

@Preview(name = "MessageListHeader Preview (User Typing)")
@Composable
private fun MessageListHeaderUserTypingPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithImage,
            currentUser = PreviewUserData.user1,
            typingUsers = listOf(PreviewUserData.user2),
            connectionState = ConnectionState.Connected,
        )
    }
}

@Preview(name = "MessageListHeader Preview (Many Members)")
@Composable
private fun MessageListHeaderManyMembersPreview() {
    ChatTheme {
        MessageListHeader(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            channel = PreviewChannelData.channelWithManyMembers,
            currentUser = PreviewUserData.user1,
            connectionState = ConnectionState.Connected,
        )
    }
}
