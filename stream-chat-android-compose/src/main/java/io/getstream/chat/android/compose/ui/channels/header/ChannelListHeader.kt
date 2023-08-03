/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewUserData
import io.getstream.chat.android.compose.ui.components.NetworkLoadingIndicator
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User

/**
 * A clean, decoupled UI element that doesn't rely on ViewModels or our custom architecture setup.
 * This allows the user to fully govern how the [ChannelListHeader] behaves, by passing in all the
 * data that's required to display it and drive its actions.
 *
 * @param modifier Modifier for styling.
 * @param title The title to display, when the network is available.
 * @param currentUser The currently logged in user, to load its image in the avatar.
 * @param connectionState The state of WS connection used to switch between the title and the network loading view.
 * @param color The color of the header.
 * @param shape The shape of the header.
 * @param elevation The elevation of the header.
 * @param onAvatarClick Action handler when the user taps on an avatar.
 * @param onHeaderActionClick Action handler when the user taps on the header action.
 * @param leadingContent Custom composable that allows the user to replace the default header leading content.
 * By default it shows a [UserAvatar].
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
    color: Color = ChatTheme.colors.barsBackground,
    shape: Shape = ChatTheme.shapes.header,
    elevation: Dp = ChatTheme.dimens.headerElevation,
    onAvatarClick: (User?) -> Unit = {},
    onHeaderActionClick: () -> Unit = {},
    leadingContent: @Composable RowScope.() -> Unit = {
        DefaultChannelHeaderLeadingContent(
            currentUser = currentUser,
            onAvatarClick = onAvatarClick,
        )
    },
    centerContent: @Composable RowScope.() -> Unit = {
        DefaultChannelListHeaderCenterContent(
            connectionState = connectionState,
            title = title,
        )
    },
    trailingContent: @Composable RowScope.() -> Unit = {
        DefaultChannelListHeaderTrailingContent(
            onHeaderActionClick = onHeaderActionClick,
        )
    },
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        elevation = elevation,
        color = color,
        shape = shape,
    ) {
        Row(
            Modifier
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
 * Represents the default leading content for the [ChannelListHeader], which is a [UserAvatar].
 *
 * We show the avatar if the user is available, otherwise we add a spacer to make sure the alignment is correct.
 */
@Composable
internal fun DefaultChannelHeaderLeadingContent(
    currentUser: User?,
    onAvatarClick: (User?) -> Unit,
) {
    val size = Modifier.size(40.dp)

    if (currentUser != null) {
        UserAvatar(
            modifier = size,
            user = currentUser,
            contentDescription = currentUser.name,
            showOnlineIndicator = false,
            onClick = { onAvatarClick(currentUser) },
        )
    } else {
        Spacer(modifier = size)
    }
}

/**
 * Represents the channel header's center slot. It either shows a [Text] if [connectionState] is
 * [ConnectionState.CONNECTED], or a [NetworkLoadingIndicator] if there is no connections.
 *
 * @param connectionState The state of WebSocket connection.
 * @param title The title to show.
 */
@Composable
internal fun RowScope.DefaultChannelListHeaderCenterContent(
    connectionState: ConnectionState,
    title: String,
) {
    when (connectionState) {
        is ConnectionState.Connected -> {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
                    .padding(horizontal = 16.dp),
                text = title,
                style = ChatTheme.typography.title3Bold,
                maxLines = 1,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
        is ConnectionState.Connecting -> NetworkLoadingIndicator(modifier = Modifier.weight(1f))
        is ConnectionState.Offline -> {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.stream_compose_disconnected),
                style = ChatTheme.typography.title3Bold,
                maxLines = 1,
                color = ChatTheme.colors.textHighEmphasis,
            )
        }
    }
}

/**
 * Represents the default trailing content for the [ChannelListHeader], which is an action icon.
 *
 * @param onHeaderActionClick Handler for when the user taps on the action.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
internal fun DefaultChannelListHeaderTrailingContent(
    onHeaderActionClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.size(40.dp),
        onClick = onHeaderActionClick,
        interactionSource = remember { MutableInteractionSource() },
        color = ChatTheme.colors.primaryAccent,
        shape = ChatTheme.shapes.avatar,
        elevation = 4.dp,
    ) {
        Icon(
            modifier = Modifier.wrapContentSize(),
            painter = painterResource(id = R.drawable.stream_compose_ic_new_chat),
            contentDescription = stringResource(id = R.string.stream_compose_channel_list_header_new_chat),
            tint = Color.White,
        )
    }
}

/**
 * Preview of [ChannelListHeader] for the client that is connected to the WS.
 *
 * Should show a user avatar, a title, and an action button.
 */
@Preview(name = "ChannelListHeader Preview (Connected state)")
@Composable
private fun ChannelListHeaderForConnectedStatePreview() {
    ChannelListHeaderPreview(connectionState = ConnectionState.Connected)
}

/**
 * Preview of [ChannelListHeader] for the client that is trying to connect to the WS.
 *
 * Should show a user avatar, "Waiting for network" caption, and an action button.
 */
@Preview(name = "ChannelListHeader Preview (Connecting state)")
@Composable
private fun ChannelListHeaderForConnectingStatePreview() {
    ChannelListHeaderPreview(connectionState = ConnectionState.Connecting)
}

/**
 * Shows [ChannelListHeader] preview for the provided parameters.
 *
 * @param title The title used to show the preview.
 * @param currentUser The currently logged in user.
 * @param connectionState The state of WS connection.
 */
@Composable
private fun ChannelListHeaderPreview(
    title: String = "Stream Chat",
    currentUser: User? = PreviewUserData.user1,
    connectionState: ConnectionState = ConnectionState.Connected,
) {
    ChatTheme {
        ChannelListHeader(
            title = title,
            currentUser = currentUser,
            connectionState = connectionState,
        )
    }
}
