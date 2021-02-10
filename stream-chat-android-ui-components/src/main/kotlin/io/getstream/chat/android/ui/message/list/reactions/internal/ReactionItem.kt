package io.getstream.chat.android.ui.message.list.reactions.internal

import androidx.annotation.DrawableRes

internal data class ReactionItem(
    val type: String,
    val isMine: Boolean,
    @DrawableRes val iconDrawableRes: Int,
)
