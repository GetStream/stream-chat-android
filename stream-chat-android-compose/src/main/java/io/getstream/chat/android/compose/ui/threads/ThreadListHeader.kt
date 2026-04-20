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

package io.getstream.chat.android.compose.ui.threads

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.DefaultListHeaderCenterContent
import io.getstream.chat.android.compose.ui.components.DefaultListHeaderLeadingContent
import io.getstream.chat.android.compose.ui.components.ListHeader
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData

/**
 * A header composable for the thread list screen.
 * Uses the shared [ListHeader] layout with no trailing action button.
 *
 * @param modifier Modifier for styling.
 * @param title The title to display, when the network is available.
 * @param currentUser The currently logged in user, to load its image in the avatar.
 * @param connectionState The state of WS connection used to switch between the title and the network loading view.
 * @param onAvatarClick Action handler when the user taps on an avatar.
 */
@Composable
public fun ThreadListHeader(
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.stream_compose_thread_list_header_title),
    currentUser: User? = null,
    connectionState: ConnectionState = ConnectionState.Connected,
    onAvatarClick: (User?) -> Unit = {},
) {
    ListHeader(
        modifier = modifier,
        leadingContent = {
            DefaultListHeaderLeadingContent(
                currentUser = currentUser,
                onAvatarClick = onAvatarClick,
            )
        },
        centerContent = {
            DefaultListHeaderCenterContent(
                connectionState = connectionState,
                title = title,
            )
        },
        trailingContent = {
            Spacer(modifier = Modifier.size(AvatarSize.ExtraLarge))
        },
    )
}

@Composable
internal fun ThreadListHeaderConnectedNoUser() {
    ThreadListHeader(
        connectionState = ConnectionState.Connected,
    )
}

@Composable
internal fun ThreadListHeaderConnectedWithUser() {
    ThreadListHeader(
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connected,
    )
}

@Composable
internal fun ThreadListHeaderConnectingNoUser() {
    ThreadListHeader(
        connectionState = ConnectionState.Connecting,
    )
}

@Composable
internal fun ThreadListHeaderConnectingWithUser() {
    ThreadListHeader(
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Connecting,
    )
}

@Composable
internal fun ThreadListHeaderOfflineNoUser() {
    ThreadListHeader(
        connectionState = ConnectionState.Offline,
    )
}

@Composable
internal fun ThreadListHeaderOfflineWithUser() {
    ThreadListHeader(
        currentUser = PreviewUserData.user1,
        connectionState = ConnectionState.Offline,
    )
}

@Preview
@Composable
private fun ThreadListHeaderConnectedPreview() {
    ChatTheme {
        ThreadListHeaderConnectedWithUser()
    }
}

@Preview
@Composable
private fun ThreadListHeaderConnectingPreview() {
    ChatTheme {
        ThreadListHeaderConnectingWithUser()
    }
}
