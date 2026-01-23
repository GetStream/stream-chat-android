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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.User
import io.getstream.chat.android.ui.common.utils.extensions.initials

@Composable
public fun UserAvatar(
    user: User,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = false,
    showBorder: Boolean = false,
) {
    BoxWithConstraints(modifier) {
        Avatar(
            imageUrl = user.image,
            fallback = { UserAvatarPlaceholder(user, maxWidth) },
            showBorder = showBorder,
            modifier = Modifier.size(maxWidth),
        )

        if (showIndicator) {
            val indicatorSize = resolveIndicatorSize()
            OnlineIndicator(
                isOnline = user.online,
                size = indicatorSize,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(
                        x = indicatorSize.borderWidth,
                        y = -indicatorSize.borderWidth,
                    ),
            )
        }
    }
}

private fun BoxWithConstraintsScope.resolveIndicatorSize(): OnlineIndicatorSize = when {
    maxWidth >= AvatarSize.ExtraLarge -> OnlineIndicatorSize.ExtraLarge
    maxWidth >= AvatarSize.Large -> OnlineIndicatorSize.Large
    maxWidth >= AvatarSize.Medium -> OnlineIndicatorSize.Medium
    else -> OnlineIndicatorSize.Small
}

@Composable
internal fun UserAvatarPlaceholder(user: User, size: Dp, modifier: Modifier = Modifier) {
    val (background, foreground) = rememberAvatarPlaceholderColors(user.id)
    val initials = rememberPlaceholderInitials(user, size)

    Box(
        modifier
            .background(background)
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        if (initials.isNotEmpty()) {
            Text(
                text = initials,
                style = size.toPlaceholderTextStyle(),
                color = foreground,
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.stream_compose_ic_user),
                contentDescription = null,
                tint = foreground,
                modifier = modifier
                    .background(background)
                    .size(size.toPlaceholderIconSize()),
            )
        }
    }
}

@Composable
private fun rememberPlaceholderInitials(user: User, availableWidth: Dp): String = remember(user.name, availableWidth) {
    val initials = user.initials
    if (availableWidth >= AvatarSize.Medium) {
        initials
    } else {
        initials.take(1)
    }
}
