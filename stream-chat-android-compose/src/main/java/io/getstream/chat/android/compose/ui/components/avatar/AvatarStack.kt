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

package io.getstream.chat.android.compose.ui.components.avatar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData

@Composable
internal inline fun AvatarStack(
    overlap: Dp,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(-overlap), content = content)
}

@Composable
internal fun UserAvatarStack(
    overlap: Dp,
    users: List<User>,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
) {
    AvatarStack(overlap, modifier) {
        for (user in users) {
            UserAvatar(
                modifier = Modifier.size(avatarSize),
                user = user,
                showBorder = showBorder,
                showOnlineIndicator = false,
            )
        }
    }
}

@Preview
@Composable
private fun AvatarStackPreview() {
    val users = List(size = 5) { PreviewUserData.userWithoutImage.copy(name = "User $it", id = "$it") }

    ChatTheme {
        UserAvatarStack(overlap = 16.dp, users = users, avatarSize = AvatarSize.Medium)
    }
}
