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

package io.getstream.chat.android.compose.ui.channels.header

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.ListHeader
import io.getstream.chat.android.compose.ui.components.button.StreamButton
import io.getstream.chat.android.compose.ui.theme.ChannelListHeaderCenterContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelListHeaderLeadingContentParams
import io.getstream.chat.android.compose.ui.theme.ChannelListHeaderTrailingContentParams
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [ChannelListHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions.
 *
 * @param modifier Modifier for styling.
 * @param title The title to display, when the network is available.
 * @param currentUser The currently logged in user, to load its image in the avatar.
 * @param connectionState The state of WS connection used to switch between the title and the network loading view.
 * @param onAvatarClick Action handler when the user taps on an avatar.
 * @param onHeaderActionClick Action handler when the user taps on the header action.
 * @param leadingContent Custom composable that allows the user to replace the default header leading content.
 * By default it shows the currently logged-in user avatar.
 * @param centerContent Custom composable that allows the user to replace the default header center content.
 * By default it either shows a text with [title] or [connectionState].
 * @param trailingContent Custom composable that allows the user to replace the default leading content.
 * By default it shows an action icon.
 */
@Composable
public fun ChannelListHeader(
    modifier: Modifier = Modifier,
    title: String = "",
    currentUser: User? = null,
    connectionState: ConnectionState,
    onAvatarClick: (User?) -> Unit = {},
    onHeaderActionClick: () -> Unit = {},
    leadingContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelListHeaderLeadingContent(
                params = ChannelListHeaderLeadingContentParams(
                    currentUser = currentUser,
                    onAvatarClick = onAvatarClick,
                ),
            )
        }
    },
    centerContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelListHeaderCenterContent(
                params = ChannelListHeaderCenterContentParams(
                    connectionState = connectionState,
                    title = title,
                ),
            )
        }
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        with(ChatTheme.componentFactory) {
            ChannelListHeaderTrailingContent(
                params = ChannelListHeaderTrailingContentParams(
                    onHeaderActionClick = onHeaderActionClick,
                ),
            )
        }
    },
) {
    ListHeader(
        modifier = modifier,
        leadingContent = leadingContent,
        centerContent = centerContent,
        trailingContent = trailingContent,
    )
}

/**
 * Represents the default trailing content for the [ChannelListHeader], which is an action icon.
 *
 * @param onHeaderActionClick Handler for when the user taps on the action.
 */
@Composable
internal fun DefaultChannelListHeaderTrailingContent(
    onHeaderActionClick: () -> Unit,
) {
    StreamButton(
        onClick = onHeaderActionClick,
    ) {
        Icon(
            modifier = Modifier.testTag("Stream_CreateChannelIcon"),
            painter = painterResource(id = R.drawable.stream_design_ic_plus),
            contentDescription = stringResource(id = R.string.stream_compose_channel_list_header_new_chat),
        )
    }
}

@Preview
@Composable
private fun ChannelListHeaderConnectedNoUserPreview() {
    ChatTheme {
        ChannelListHeaderConnectedNoUser()
    }
}

@Preview
@Composable
private fun ChannelListHeaderConnectedWithUserPreview() {
    ChatTheme {
        ChannelListHeaderConnectedWithUser()
    }
}

@Preview
@Composable
private fun ChannelListHeaderConnectingNoUserPreview() {
    ChatTheme {
        ChannelListHeaderConnectingNoUser()
    }
}

@Preview
@Composable
private fun ChannelListHeaderConnectingWithUserPreview() {
    ChatTheme {
        ChannelListHeaderConnectingWithUser()
    }
}

@Preview
@Composable
private fun ChannelListHeaderOfflineNoUserPreview() {
    ChatTheme {
        ChannelListHeaderOfflineNoUser()
    }
}

@Preview
@Composable
private fun ChannelListHeaderOfflineWithUserPreview() {
    ChatTheme {
        ChannelListHeaderOfflineWithUser()
    }
}

@Composable
internal fun ChannelListHeaderConnectedNoUser() {
    ChannelListHeader(
        title = Title,
        connectionState = ConnectionState.Connected,
    )
}

@Composable
internal fun ChannelListHeaderConnectedWithUser() {
    ChannelListHeader(
        title = Title,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connected,
    )
}

@Composable
internal fun ChannelListHeaderConnectingNoUser() {
    ChannelListHeader(
        title = Title,
        connectionState = ConnectionState.Connecting,
    )
}

@Composable
internal fun ChannelListHeaderConnectingWithUser() {
    ChannelListHeader(
        title = Title,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connecting,
    )
}

@Composable
internal fun ChannelListHeaderOfflineNoUser() {
    ChannelListHeader(
        title = Title,
        connectionState = ConnectionState.Offline,
    )
}

@Composable
internal fun ChannelListHeaderOfflineWithUser() {
    ChannelListHeader(
        title = Title,
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Offline,
    )
}

private const val Title = "Stream Chat"
