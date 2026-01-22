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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import io.getstream.chat.android.compose.R
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.User

@Composable
public fun ChannelAvatar(
    channel: Channel,
    currentUser: User?,
    modifier: Modifier = Modifier,
    showIndicator: Boolean = false,
    showBorder: Boolean = false,
) {
    if (channel.image.isNotEmpty()) {
        GroupAvatar(
            channel = channel,
            showBorder = showBorder,
            modifier = modifier,
        )
    } else if (channel.members.size == 1) {
        UserAvatar(
            modifier = modifier,
            user = channel.members.first().user,
            showIndicator = showIndicator,
            showBorder = showBorder,
        )
    } else {
        val directMessageRecipient = directMessageRecipient(channel, currentUser)

        if (directMessageRecipient != null) {
            UserAvatar(
                modifier = modifier,
                user = directMessageRecipient,
                showIndicator = showIndicator,
                showBorder = showBorder,
            )
        } else {
            GroupAvatar(
                channel = channel,
                showBorder = showBorder,
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun GroupAvatar(
    channel: Channel,
    showBorder: Boolean,
    modifier: Modifier,
) {
    BoxWithConstraints(modifier) {
        Avatar(
            imageUrl = channel.image,
            fallback = { ChannelAvatarPlaceholder(channel, size = this.maxWidth) },
            showBorder = showBorder,
        )
    }
}

@Composable
internal fun ChannelAvatarPlaceholder(channel: Channel, size: Dp, modifier: Modifier = Modifier) {
    val (background, foreground) = rememberAvatarPlaceholderColors(channel.cid)

    Box(
        modifier
            .background(background)
            .size(size),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_compose_ic_team),
            contentDescription = null,
            tint = foreground,
            modifier = modifier
                .background(background)
                .size(size.toPlaceholderIconSize()),
        )
    }
}

/** Returns the other participant if this is a 1-to-1 direct message involving the current user. */
@Composable
private fun directMessageRecipient(channel: Channel, currentUser: User?): User? {
    val currentUserId = currentUser?.id ?: return null

    return remember(channel, currentUserId) {
        if (channel.memberCount == 2 && channel.members.any { it.user.id == currentUserId }) {
            channel.members.first { it.user.id != currentUserId }.user
        } else {
            null
        }
    }
}
