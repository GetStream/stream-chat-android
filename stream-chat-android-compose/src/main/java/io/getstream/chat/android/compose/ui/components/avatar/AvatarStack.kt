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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData

/**
 * A composable that displays a stack of user avatars. The avatars can overlap.
 *
 * @param overlap The amount of overlap between avatars.
 * @param users The list of users to display avatars for.
 * @param avatarSize The size of each avatar.
 * @param modifier Modifier for styling.
 * @param showBorder Whether to show a border around the first avatar.
 * @param trailingContent Optional composable rendered after the avatars.
 */
@Composable
public fun UserAvatarStack(
    overlap: Dp,
    users: List<User>,
    avatarSize: Dp,
    modifier: Modifier = Modifier,
    showBorder: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    val componentFactory = ChatTheme.componentFactory
    val colors = ChatTheme.colors
    val borderSize = 2.dp

    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(-overlap)
    ) {
        users.forEachIndexed { index, user ->
            componentFactory.UserAvatar(
                modifier = Modifier
                    .size(avatarSize + borderSize)
                    .background(colors.borderCoreOnDark, CircleShape)
                    .padding(borderSize),
                user = user,
                showBorder = showBorder && index == 0,
                showIndicator = false,
            )
        }
        trailingContent?.invoke()
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
