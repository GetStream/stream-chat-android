package io.getstream.chat.android.ui.avatar.internal

import io.getstream.chat.android.client.models.Channel
import io.getstream.chat.android.client.models.User

internal sealed class Avatar(open val avatarStyle: AvatarStyle) {
    data class UserAvatar(
        val user: User,
        override val avatarStyle: AvatarStyle
    ) : Avatar(avatarStyle)

    data class ChannelAvatar(
        val channel: Channel,
        override val avatarStyle: AvatarStyle
    ) : Avatar(avatarStyle)
}
