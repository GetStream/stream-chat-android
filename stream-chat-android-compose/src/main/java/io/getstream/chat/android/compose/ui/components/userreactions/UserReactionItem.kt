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

package io.getstream.chat.android.compose.ui.components.userreactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.previewdata.PreviewUserReactionData
import io.getstream.chat.android.compose.state.userreactions.UserReactionItemState
import io.getstream.chat.android.compose.ui.components.reactions.ReactionIconSize
import io.getstream.chat.android.compose.ui.theme.ChatPreviewTheme
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.ui.common.state.messages.list.isStartAlignment

/**
 * Represent a reaction item with the user who left it.
 *
 * @param item The reaction item state.
 * @param modifier Modifier for styling.
 */
@Composable
public fun UserReactionItem(
    item: UserReactionItemState,
    modifier: Modifier = Modifier,
) {
    val (user, type, isMine, emojiCode) = item

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val isStartAlignment = ChatTheme.messageOptionsUserReactionAlignment.isStartAlignment(isMine)
        val alignment = if (isStartAlignment) Alignment.BottomStart else Alignment.BottomEnd

        Box(modifier = Modifier.width(64.dp)) {
            ChatTheme.componentFactory.UserAvatar(
                modifier = Modifier.size(ChatTheme.dimens.userReactionItemAvatarSize),
                user = user,
                showIndicator = false,
                showBorder = false,
            )

            ChatTheme.componentFactory.ReactionIcon(
                type = type,
                emoji = emojiCode,
                size = ReactionIconSize.Small,
                modifier = Modifier
                    .align(alignment)
                    .background(shape = RoundedCornerShape(16.dp), color = ChatTheme.colors.barsBackground)
                    .padding(4.dp),
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.name,
            style = ChatTheme.typography.footnoteBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = ChatTheme.colors.textHighEmphasis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * Preview of the [UserReactionItem] component with a reaction left by the current user.
 */
@Preview(showBackground = true)
@Composable
public fun CurrentUserReactionItemPreview() {
    ChatPreviewTheme {
        UserReactionItem(item = PreviewUserReactionData.user1Reaction())
    }
}

/**
 * Preview of the [UserReactionItem] component with a reaction left by another user.
 */
@Preview(showBackground = true)
@Composable
public fun OtherUserReactionItemPreview() {
    ChatPreviewTheme {
        UserReactionItem(item = PreviewUserReactionData.user2Reaction())
    }
}
