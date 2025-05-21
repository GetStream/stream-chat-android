/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.channel.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.components.avatar.UserAvatar
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.getLastSeenText
import io.getstream.chat.android.models.User

@Composable
internal fun ChannelInfoMemberInfo(
    user: User,
    avatarAlignment: Alignment.Vertical,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (avatarAlignment == Alignment.Top) {
            UserAvatar(
                modifier = Modifier.size(72.dp),
                user = user,
            )
        }
        Text(
            text = user.name.takeIf(String::isNotBlank) ?: user.id,
            style = ChatTheme.typography.title3Bold,
            color = ChatTheme.colors.textHighEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = user.getLastSeenText(LocalContext.current),
            style = ChatTheme.typography.footnote,
            color = ChatTheme.colors.textLowEmphasis,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (avatarAlignment == Alignment.Bottom) {
            UserAvatar(
                modifier = Modifier.size(72.dp),
                user = user,
            )
        }
    }
}
