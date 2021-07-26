package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.compose.R

public object DefaultReactionTypes {
    private const val LOVE: String = "love"
    private const val THUMBS_UP: String = "like"
    private const val THUMBS_DOWN: String = "sad"
    private const val LOL: String = "haha"
    private const val WUT: String = "wow"

    internal val defaultReactionTypes: Map<String, Int> = mapOf(
        THUMBS_UP to R.drawable.stream_ui_ic_reaction_thumbs_up,
        LOVE to R.drawable.stream_ui_ic_reaction_love,
        LOL to R.drawable.stream_ui_ic_reaction_lol,
        WUT to R.drawable.stream_ui_ic_reaction_wut,
        THUMBS_DOWN to R.drawable.stream_ui_ic_reaction_thumbs_down
    )
}