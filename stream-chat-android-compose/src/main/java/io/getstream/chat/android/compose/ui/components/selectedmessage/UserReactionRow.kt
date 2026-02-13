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

package io.getstream.chat.android.compose.ui.components.selectedmessage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.compose.previewdata.PreviewUserReactionData
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.theme.StreamTokens
import io.getstream.chat.android.compose.ui.util.clickable
import io.getstream.chat.android.compose.ui.util.ifNotNull

/**
 * Represent a reaction item with the user who left it as a horizontal list row.
 *
 * @param item The reaction item state.
 * @param modifier Modifier for styling.
 * @param onClick Optional click handler, used for tap-to-remove on own reactions.
 */
@Composable
internal fun UserReactionRow(
    item: UserReactionItemState,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .ifNotNull(onClick) { clickable(onClick = it) }
            .padding(horizontal = StreamTokens.spacingSm, vertical = StreamTokens.spacingXs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(StreamTokens.spacingXs),
    ) {
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier.size(AvatarSize.Medium),
            user = item.user,
            showIndicator = false,
            showBorder = true,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (item.isMine) stringResource(R.string.stream_compose_reactions_you) else item.user.name,
                style = ChatTheme.typography.bodyDefault,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = ChatTheme.colors.textPrimary,
            )
            if (item.isMine) {
                Text(
                    text = stringResource(R.string.stream_compose_reactions_remove),
                    style = ChatTheme.typography.captionDefault,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = ChatTheme.colors.textTertiary,
                )
            }
        }

        ChatTheme.componentFactory.ReactionIcon(
            type = item.type,
            emoji = item.emojiCode,
            size = ReactionIconSize.Medium,
            modifier = Modifier,
        )
    }
}

/**
 * Preview of the [UserReactionRow] component with a reaction left by the current user.
 */
@Preview(showBackground = true)
@Composable
private fun CurrentUserReactionItemPreview() {
    ChatPreviewTheme {
        UserReactionRow(item = PreviewUserReactionData.user1Reaction())
    }
}

/**
 * Preview of the [UserReactionRow] component with a reaction left by another user.
 */
@Preview(showBackground = true)
@Composable
private fun OtherUserReactionItemPreview() {
    ChatPreviewTheme {
        UserReactionRow(item = PreviewUserReactionData.user2Reaction())
    }
}
