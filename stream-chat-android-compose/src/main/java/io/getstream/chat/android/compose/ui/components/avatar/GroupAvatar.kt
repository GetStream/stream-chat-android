/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.ui.util.extensions.internal.getAvatarPositionOffset
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.utils.extensions.initials

/**
 * Default max number of avatars shown in the grid.
 */
private const val DefaultNumberOfAvatars = 4

/**
 * Represents an avatar with a matrix of user images or initials.
 *
 * @param users The users to show.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param textStyle The [TextStyle] that will be used for the initials.
 * @param onClick The handler when the user clicks on the avatar.
 */
@Composable
public fun GroupAvatar(
    users: List<User>,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.captionBold,
    onClick: (() -> Unit)? = null,
) {
    val avatarUsers = users.take(DefaultNumberOfAvatars)
    val imageCount = avatarUsers.size

    val clickableModifier: Modifier = if (onClick != null) {
        modifier.clickable(
            onClick = onClick,
            indication = ripple(bounded = false),
            interactionSource = remember { MutableInteractionSource() },
        )
    } else {
        modifier
    }

    Row(clickableModifier.clip(shape)) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight(),
        ) {
            for (imageIndex in 0 until imageCount step 2) {
                val user = avatarUsers[imageIndex]
                if (imageIndex < imageCount) {
                    ChatTheme.componentFactory.Avatar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        imageUrl = user.image,
                        initials = user.initials,
                        shape = RectangleShape,
                        textStyle = textStyle,
                        initialsAvatarOffset = getAvatarPositionOffset(
                            dimens = ChatTheme.dimens,
                            userPosition = imageIndex,
                            memberCount = imageCount,
                        ),
                        contentDescription = null,
                        onClick = null,
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .fillMaxHeight(),
        ) {
            for (imageIndex in 1 until imageCount step 2) {
                val user = avatarUsers[imageIndex]
                if (imageIndex < imageCount) {
                    ChatTheme.componentFactory.Avatar(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        imageUrl = user.image,
                        initials = user.initials,
                        shape = RectangleShape,
                        textStyle = textStyle,
                        initialsAvatarOffset = getAvatarPositionOffset(
                            dimens = ChatTheme.dimens,
                            userPosition = imageIndex,
                            memberCount = imageCount,
                        ),
                        contentDescription = null,
                        onClick = null,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun GroupAvatarPreview() {
    ChatTheme {
        GroupAvatar(
            modifier = Modifier.size(36.dp),
            users = listOf(PreviewUserData.user1, PreviewUserData.user2, PreviewUserData.user3, PreviewUserData.user4),
        )
    }
}
