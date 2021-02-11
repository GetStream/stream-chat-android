package io.getstream.chat.android.ui.message.list.reactions.user.internal

import androidx.annotation.DrawableRes
import io.getstream.chat.android.client.models.Reaction
import io.getstream.chat.android.client.models.User

internal data class UserReactionItem(
    val user: User,
    val reaction: Reaction,
    val isMine: Boolean,
    @DrawableRes val iconDrawableRes: Int,
)
