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

package io.getstream.chat.android.compose.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.theme.UserAvatarParams
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.models.ConnectionState
import io.getstream.chat.android.models.User

/**
 * Common header layout.
 *
 * @param modifier Modifier for styling.
 * @param color The background color of the header.
 * @param shape The shape of the header.
 * @param elevation The elevation of the header.
 * @param leadingContent Composable for the leading slot (e.g. user avatar).
 * @param centerContent Composable for the center slot (e.g. title or loading indicator).
 * @param trailingContent Composable for the trailing slot (e.g. action button or spacer).
 */
@Composable
internal fun ListHeader(
    modifier: Modifier = Modifier,
    color: Color = ChatTheme.colors.backgroundCoreElevation1,
    shape: Shape = RectangleShape,
    elevation: Dp = 0.dp,
    leadingContent: @Composable RowScope.() -> Unit,
    centerContent: @Composable RowScope.() -> Unit,
    trailingContent: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shadowElevation = elevation,
        color = color,
        shape = shape,
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(StreamTokens.spacingSm),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
            ) {
                leadingContent()

                centerContent()

                trailingContent()
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = ChatTheme.colors.borderCoreSubtle,
            )
        }
    }
}

/**
 * Default leading content for a [ListHeader].
 * Shows the user avatar if available, otherwise a spacer to preserve alignment.
 *
 * @param currentUser The currently logged in user.
 * @param onAvatarClick Action invoked when the avatar is clicked.
 */
@Composable
internal fun DefaultListHeaderLeadingContent(
    currentUser: User?,
    onAvatarClick: (User?) -> Unit,
) {
    if (currentUser != null) {
        Box(
            modifier = Modifier
                .size(AvatarSize.ExtraLarge)
                .clip(CircleShape)
                .clickable { onAvatarClick(currentUser) },
            contentAlignment = Alignment.Center,
        ) {
            ChatTheme.componentFactory.UserAvatar(
                params = UserAvatarParams(
                    modifier = Modifier
                        .size(AvatarSize.Large)
                        .testTag("Stream_UserAvatar"),
                    user = currentUser,
                ),
            )
        }
    } else {
        Spacer(modifier = Modifier.size(AvatarSize.ExtraLarge))
    }
}

/**
 * Default center content for a [ListHeader].
 * Shows a title when connected, a loading indicator when connecting, or "Disconnected" when offline.
 *
 * @param connectionState The state of WebSocket connection.
 * @param title The title to show.
 */
@Composable
internal fun RowScope.DefaultListHeaderCenterContent(
    connectionState: ConnectionState,
    title: String,
) {
    when (connectionState) {
        is ConnectionState.Connected -> {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
                    .padding(horizontal = StreamTokens.spacingMd),
                text = title,
                style = ChatTheme.typography.headingSmall,
                maxLines = 1,
                color = ChatTheme.colors.textPrimary,
            )
        }

        is ConnectionState.Connecting -> NetworkLoadingIndicator(modifier = Modifier.weight(1f))
        is ConnectionState.Offline -> {
            Text(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentWidth()
                    .padding(horizontal = StreamTokens.spacingMd),
                text = stringResource(R.string.stream_compose_disconnected),
                style = ChatTheme.typography.headingSmall,
                maxLines = 1,
                color = ChatTheme.colors.textPrimary,
            )
        }
    }
}
