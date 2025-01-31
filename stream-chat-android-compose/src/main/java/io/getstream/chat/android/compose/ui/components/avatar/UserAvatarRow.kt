/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.utils.extensions.initials

/**
 * Represents the [User] avatar that's shown on the Messages screen or in headers of DMs.
 *
 * Based on the state within the [User], we either show an image or their initials.
 *
 * @param users The list of users whose avatar we want to show.
 * @param modifier Modifier for styling.
 * @param maxAvatarCount The maximum count for displaying the avatar row.
 * @param size The size of each user avatar.
 * @param offset The offset of the
 * @param shape The shape of the avatar.
 * @param textStyle The [TextStyle] that will be used for the initials.
 * @param contentDescription The content description of the avatar.
 * @param initialsAvatarOffset The initials offset to apply to the avatar.
 * @param onClick The handler when the user clicks on the avatar.
 */
@Composable
public fun UserAvatarRow(
    users: List<User>,
    modifier: Modifier = Modifier,
    maxAvatarCount: Int = 3,
    size: Dp = 20.dp,
    offset: Int = 7,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    contentDescription: String? = null,
    initialsAvatarOffset: DpOffset = DpOffset(0.dp, 0.dp),
    onClick: (() -> Unit)? = null,
) {
    if (users.isEmpty()) return

    Row(
        modifier = modifier.width(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy((-offset).dp),
    ) {
        users.take(maxAvatarCount).forEachIndexed { index, user ->
            ChatTheme.componentFactory.Avatar(
                modifier = Modifier
                    .size(size)
                    .zIndex((users.size - index).toFloat()),
                imageUrl = user.image,
                initials = user.initials,
                shape = shape,
                textStyle = textStyle,
                contentDescription = contentDescription,
                initialsAvatarOffset = initialsAvatarOffset,
                onClick = onClick,
            )
        }
    }
}

@Preview
@Composable
private fun UserAvatarRowPreview() {
    ChatTheme {
        Column(
            horizontalAlignment = Alignment.End,
        ) {
            UserAvatarRow(
                users = listOf(
                    PreviewUserData.user1,
                ),
            )
            UserAvatarRow(
                users = listOf(
                    PreviewUserData.user1,
                    PreviewUserData.user2,
                ),
            )
            UserAvatarRow(
                users = listOf(
                    PreviewUserData.user1,
                    PreviewUserData.user2,
                    PreviewUserData.user3,
                ),
            )
        }
    }
}
