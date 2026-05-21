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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.BackButton
import io.getstream.chat.android.compose.ui.components.HeaderScaffold
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.theme.ChannelAvatarParams
import io.getstream.chat.android.compose.ui.theme.ChannelHeaderCenterContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelHeaderLeadingContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelHeaderTrailingContentParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.getMembersStatusText
import io.getstream.chat.android.compose.ui.util.ifNotNull
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewChannelData
import io.getstream.chat.android.previewdata.PreviewMessageData
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.state.messages.MessageMode

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [ChannelHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions, as well as customize the slot APIs.
 *
 * @param channel Channel info to display.
 * @param currentUser The current user, required for different UI states.
 * @param connectionState The state of WS connection used to switch between the subtitle and the network loading view.
 * @param modifier Modifier for styling.
 * @param typingUsers The list of typing users.
 * @param messageMode The current message mode, that changes the header content, if we're in a Thread.
 * @param onBackPressed Handler that propagates the back button click event.
 * @param onHeaderTitleClick Action handler when the user taps on the header title section.
 * @param onHeaderTitleClickLabel Semantic / accessibility label for [onHeaderTitleClick].
 * @param onChannelAvatarClick Action handler called when the user taps on the channel avatar.
 * @param onChannelAvatarClickLabel Semantic / accessibility label for [onChannelAvatarClick].
 * @param leadingContent The content shown at the start of the header, by default a [BackButton].
 * @param centerContent The content shown in the middle of the header and represents the core information, by default
 * [DefaultChannelHeaderCenterContent].
 * @param trailingContent The content shown at the end of the header, by default the channel avatar.
 */
@Composable
public fun ChannelHeader(
    channel: Channel,
    currentUser: User?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    typingUsers: List<User> = emptyList(),
    messageMode: MessageMode = MessageMode.Normal,
    onBackPressed: () -> Unit = {},
    onHeaderTitleClick: ((Channel) -> Unit)? = null,
    onHeaderTitleClickLabel: String? = null,
    onChannelAvatarClick: ((Channel) -> Unit)? = null,
    onChannelAvatarClickLabel: String? = null,
    leadingContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelHeaderLeadingContent(
                params = ChannelHeaderLeadingContentParams(
                    onBackPressed = onBackPressed,
                ),
            )
        }
    },
    centerContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelHeaderCenterContent(
                params = ChannelHeaderCenterContentParams(
                    modifier = Modifier.weight(1f),
                    channel = channel,
                    currentUser = currentUser,
                    connectionState = connectionState,
                    typingUsers = typingUsers,
                    messageMode = messageMode,
                    onClick = onHeaderTitleClick,
                    onClickLabel = onHeaderTitleClickLabel,
                ),
            )
        }
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelHeaderTrailingContent(
                params = ChannelHeaderTrailingContentParams(
                    channel = channel,
                    currentUser = currentUser,
                    onClick = onChannelAvatarClick,
                    onClickLabel = onChannelAvatarClickLabel,
                ),
            )
        }
    },
) {
    HeaderScaffold(
        modifier = modifier,
        leadingContent = leadingContent,
        centerContent = centerContent,
        trailingContent = trailingContent,
    )
}

/**
 * Represents the leading content of [ChannelHeader]. By default shows a back button.
 *
 * @param onBackPressed Handler that propagates the back button click event.
 */
@Composable
internal fun DefaultChannelHeaderLeadingContent(onBackPressed: () -> Unit) {
    BackButton(
        painter = painterResource(id = R.drawable.stream_design_ic_arrow_left),
        onBackPressed = onBackPressed,
    )
}

/**
 * Represents the center content of [ChannelHeader]. By default shows header title, that handles
 * if we should show a loading view for network, or the channel information.
 *
 * @param channel The channel used for the title information.
 * @param currentUser The current user.
 * @param connectionState A flag that governs if we show the subtitle or the network loading view.
 * @param modifier Modifier for styling.
 * @param messageMode Currently active message mode, used to define the title information.
 * @param onHeaderTitleClick Handler for when the user taps on the header title section.
 * @param onHeaderTitleClickLabel Semantic / accessibility label for [onHeaderTitleClick].
 */
@Suppress("LongMethod")
@Composable
internal fun DefaultChannelHeaderCenterContent(
    channel: Channel,
    currentUser: User?,
    connectionState: ConnectionState,
    modifier: Modifier = Modifier,
    messageMode: MessageMode = MessageMode.Normal,
    onHeaderTitleClick: ((Channel) -> Unit)? = null,
    onHeaderTitleClickLabel: String? = null,
) {
    val title = when (messageMode) {
        MessageMode.Normal -> ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
        is MessageMode.MessageThread -> stringResource(id = R.string.stream_compose_thread_title)
    }

    val subtitle = when (messageMode) {
        MessageMode.Normal -> channel.getMembersStatusText(
            context = LocalContext.current,
            currentUser = currentUser,
        )

        is MessageMode.MessageThread -> stringResource(
            R.string.stream_compose_thread_subtitle,
            ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser),
        )
    }

    Column(
        modifier = modifier
            .height(IntrinsicSize.Max)
            .ifNotNull(onHeaderTitleClick) { callback ->
                clickable(
                    onClickLabel = onHeaderTitleClickLabel,
                    role = Role.Button,
                ) { callback(channel) }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            modifier = Modifier.testTag("Stream_ChannelName"),
            text = title,
            style = ChatTheme.typography.headingSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textPrimary,
        )

        when (connectionState) {
            is ConnectionState.Connected -> {
                DefaultChannelHeaderSubtitle(
                    subtitle = subtitle,
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
 * Represents the default message list header subtitle, which shows the number of people online
 * and total member count.
 *
 * @param subtitle The subtitle to show.
 */
@Composable
internal fun DefaultChannelHeaderSubtitle(
    subtitle: String,
) {
    Text(
        modifier = Modifier.testTag("Stream_ParticipantsInfo"),
        text = subtitle,
        color = ChatTheme.colors.textSecondary,
        style = ChatTheme.typography.captionDefault,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * Represents the trailing content of [ChannelHeader]. By default shows the channel avatar.
 *
 * @param channel The channel used to display the avatar.
 * @param currentUser The current user. Used for choosing which avatar to display.
 * @param onClick The handler called when the user taps on the channel avatar.
 * @param onClickLabel Semantic / accessibility label for [onClick].
 */
@Composable
internal fun DefaultChannelHeaderTrailingContent(
    channel: Channel,
    currentUser: User?,
    onClick: ((Channel) -> Unit)?,
    onClickLabel: String? = null,
) {
    val avatarLabel = ChatTheme.channelNameFormatter.formatChannelName(channel, currentUser)
    ChatTheme.componentFactory.ChannelAvatar(
        params = ChannelAvatarParams(
            modifier = Modifier
                .size(40.dp)
                .ifNotNull(onClick) { callback ->
                    clickable(
                        bounded = false,
                        onClickLabel = onClickLabel,
                        role = Role.Button,
                    ) { callback(channel) }
                        .semantics(mergeDescendants = true) {
                            contentDescription = avatarLabel
                        }
                },
            channel = channel,
            currentUser = currentUser,
        ),
    )
}

@Preview
@Composable
private fun ChannelHeaderConnectedPreview() {
    ChatTheme {
        ChannelHeaderConnected()
    }
}

@Composable
internal fun ChannelHeaderConnected() {
    ChannelHeader(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        channel = PreviewChannelData.channelWithImage,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connected,
    )
}

@Preview
@Composable
private fun ChannelHeaderConnectingPreview() {
    ChatTheme {
        ChannelHeaderConnecting()
    }
}

@Composable
internal fun ChannelHeaderConnecting() {
    ChannelHeader(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        channel = PreviewChannelData.channelWithImage,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connecting,
    )
}

@Preview
@Composable
private fun ChannelHeaderOfflinePreview() {
    ChatTheme {
        ChannelHeaderOffline()
    }
}

@Composable
internal fun ChannelHeaderOffline() {
    ChannelHeader(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        channel = PreviewChannelData.channelWithImage,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Offline,
    )
}

@Preview
@Composable
private fun ChannelHeaderManyMembersPreview() {
    ChatTheme {
        ChannelHeaderManyMembers()
    }
}

@Composable
internal fun ChannelHeaderManyMembers() {
    ChannelHeader(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        channel = PreviewChannelData.channelWithManyMembers,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connected,
    )
}

@Preview
@Composable
private fun ChannelHeaderFewMembersPreview() {
    ChatTheme {
        ChannelHeaderFewMembers()
    }
}

@Composable
internal fun ChannelHeaderFewMembers() {
    ChannelHeader(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        channel = PreviewChannelData.channelWithFewMembers,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connected,
    )
}

@Preview
@Composable
private fun ChannelHeaderThreadModePreview() {
    ChatTheme {
        ChannelHeaderThreadMode()
    }
}

@Composable
internal fun ChannelHeaderThreadMode() {
    ChannelHeader(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        channel = PreviewChannelData.channelWithImage,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connected,
        messageMode = MessageMode.MessageThread(
            parentMessage = PreviewMessageData.message1,
            threadState = null,
        ),
    )
}
