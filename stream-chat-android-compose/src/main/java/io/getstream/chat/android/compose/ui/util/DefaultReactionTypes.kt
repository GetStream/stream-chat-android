package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.compose.R

/**
 * Defines the default reaction values that we support that correspond to specific reaction icon resources.
 */
public object DefaultReactionTypes {
    private const val LOVE: String = "love"
    private const val THUMBS_UP: String = "like"
    private const val THUMBS_DOWN: String = "sad"
    private const val LOL: String = "haha"
    private const val WUT: String = "wow"

    /**
     * Represents the default reaction types supported by our SDK.
     *
     * @return A [Map] of reaction [String] values to [Int] icon resources.
     */
    public fun defaultReactionTypes(): Map<String, Int> = mapOf(
        THUMBS_UP to R.drawable.stream_compose_ic_reaction_thumbs_up,
        LOVE to R.drawable.stream_compose_ic_reaction_love,
        LOL to R.drawable.stream_compose_ic_reaction_lol,
        WUT to R.drawable.stream_compose_ic_reaction_wut,
        THUMBS_DOWN to R.drawable.stream_compose_ic_reaction_thumbs_down
    )
}
