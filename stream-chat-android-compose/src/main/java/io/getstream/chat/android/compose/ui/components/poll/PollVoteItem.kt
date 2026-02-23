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

package io.getstream.chat.android.compose.ui.components.poll

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.avatar.AvatarSize
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.Vote

@Composable
internal fun PollVoteItem(
    vote: Vote,
    modifier: Modifier = Modifier,
) {
    val user = vote.user ?: return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        ChatTheme.componentFactory.UserAvatar(
            modifier = Modifier.size(AvatarSize.ExtraSmall),
            user = user,
            showIndicator = false,
            showBorder = false,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = user.name,
            color = ChatTheme.colors.textPrimary,
            style = ChatTheme.typography.body,
        )

        Text(
            text = ChatTheme.dateFormatter.formatRelativeDate(vote.createdAt),
            color = ChatTheme.colors.textSecondary,
            style = ChatTheme.typography.bodyBold,
        )

        Text(
            text = ChatTheme.dateFormatter.formatTime(vote.createdAt),
            color = ChatTheme.colors.textSecondary,
            style = ChatTheme.typography.body,
        )
    }
}
