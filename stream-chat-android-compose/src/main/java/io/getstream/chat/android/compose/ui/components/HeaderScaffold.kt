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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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
 * Three-slot header layout with leading, center, and trailing slots laid out horizontally and a
 * bottom divider.
 *
 * @param modifier Modifier for styling.
 * @param leadingContent Slot rendered at the start of the row (e.g. avatar, back button).
 * @param centerContent Slot rendered in the center of the row (e.g. title, loading indicator).
 * @param trailingContent Slot rendered at the end of the row (e.g. action button, channel avatar).
 */
@Composable
internal fun HeaderScaffold(
    modifier: Modifier = Modifier,
    leadingContent: @Composable RowScope.() -> Unit,
    centerContent: @Composable RowScope.() -> Unit,
    trailingContent: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = ChatTheme.colors.backgroundCoreElevation1,
    ) {
        Column {
            Row(
                modifier = Modifier
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
 * Default list-header leading content. Shows the user avatar if available, otherwise a spacer to
 * preserve alignment.
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
 * Default list-header center content. Shows a title when connected, a loading indicator when
 * connecting, or "Disconnected" when offline.
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
                    .padding(horizontal = StreamTokens.spacingMd)
                    .semantics { heading() },
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
                    .padding(horizontal = StreamTokens.spacingMd)
                    .semantics { heading() },
                text = stringResource(R.string.stream_compose_disconnected),
                style = ChatTheme.typography.headingSmall,
                maxLines = 1,
                color = ChatTheme.colors.textPrimary,
            )
        }
    }
}
