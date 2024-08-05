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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.compose.state.OnlineIndicatorAlignment
import io.getstream.chat.android.compose.ui.components.OnlineIndicator
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.models.User
import io.getstream.chat.android.previewdata.PreviewUserData
import io.getstream.chat.android.ui.common.utils.extensions.initials

/**
 * Represents the [User] avatar that's shown on the Messages screen or in headers of DMs.
 *
 * Based on the state within the [User], we either show an image or their initials.
 *
 * @param user The user whose avatar we want to show.
 * @param modifier Modifier for styling.
 * @param shape The shape of the avatar.
 * @param textStyle The [TextStyle] that will be used for the initials.
 * @param contentDescription The content description of the avatar.
 * @param showOnlineIndicator If we show online indicator or not.
 * @param onlineIndicatorAlignment The alignment of online indicator.
 * @param initialsAvatarOffset The initials offset to apply to the avatar.
 * @param onlineIndicator Custom composable that allows to replace the default online indicator.
 * @param onClick The handler when the user clicks on the avatar.
 */
@Composable
public fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    textStyle: TextStyle = ChatTheme.typography.title3Bold,
    contentDescription: String? = null,
    showOnlineIndicator: Boolean = true,
    placeholderPainter: Painter? = null,
    onlineIndicatorAlignment: OnlineIndicatorAlignment = OnlineIndicatorAlignment.TopEnd,
    initialsAvatarOffset: DpOffset = DpOffset(0.dp, 0.dp),
    onlineIndicator: @Composable BoxScope.() -> Unit = {
        DefaultOnlineIndicator(onlineIndicatorAlignment)
    },
    onClick: (() -> Unit)? = null,
) {
    Box(modifier = modifier) {
        Avatar(
            modifier = Modifier.fillMaxSize(),
            imageUrl = user.image,
            initials = user.initials,
            textStyle = textStyle,
            shape = shape,
            contentDescription = contentDescription,
            onClick = onClick,
            placeholderPainter = placeholderPainter,
            initialsAvatarOffset = initialsAvatarOffset,
        )

        if (showOnlineIndicator && user.online) {
            onlineIndicator()
        }
    }
}

/**
 * The default online indicator for channel members.
 */
@Composable
internal fun BoxScope.DefaultOnlineIndicator(onlineIndicatorAlignment: OnlineIndicatorAlignment) {
    OnlineIndicator(modifier = Modifier.align(onlineIndicatorAlignment.alignment))
}

/**
 * Preview of [UserAvatar] for a user with avatar image.
 *
 * Should show a placeholder that represents user avatar image.
 */
@Preview(showBackground = true, name = "UserAvatar Preview (With avatar image)")
@Composable
private fun UserAvatarForUserWithImagePreview() {
    UserAvatarPreview(PreviewUserData.userWithImage)
}

/**
 * Preview of [UserAvatar] for a user which is online.
 *
 * Should show an avatar with an online indicator in the upper right corner.
 */
@Preview(showBackground = true, name = "UserAvatar Preview (With online status)")
@Composable
private fun UserAvatarForOnlineUserPreview() {
    UserAvatarPreview(PreviewUserData.userWithOnlineStatus)
}

/**
 * Preview of [UserAvatar] for a user without avatar image.
 *
 * Should show background gradient and user initials.
 */
@Preview(showBackground = true, name = "UserAvatar Preview (Without avatar image)")
@Composable
private fun UserAvatarForUserWithoutImagePreview() {
    UserAvatarPreview(PreviewUserData.userWithoutImage)
}

/**
 * Shows [UserAvatar] preview for the provided parameters.
 *
 * @param user The user used to show the preview.
 */
@Composable
private fun UserAvatarPreview(user: User) {
    ChatTheme {
        UserAvatar(
            modifier = Modifier.size(36.dp),
            user = user,
            showOnlineIndicator = true,
        )
    }
}
