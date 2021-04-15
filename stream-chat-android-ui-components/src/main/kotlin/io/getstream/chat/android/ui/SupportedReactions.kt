package io.getstream.chat.android.ui

import androidx.annotation.DrawableRes
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_DOWN
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.WUT

/**
 * Class allowing to define set of supported reactions
 */
public class SupportedReactions(
    /**
     * Map with keys corresponding to reaction type, value corresponding to Drawable Int
     */
    public val reactions: Map<String, Int> = mapOf(
        LOVE to R.drawable.stream_ui_ic_reaction_love,
        THUMBS_UP to R.drawable.stream_ui_ic_reaction_thumbs_up,
        THUMBS_DOWN to R.drawable.stream_ui_ic_reaction_thumbs_down,
        LOL to R.drawable.stream_ui_ic_reaction_lol,
        WUT to R.drawable.stream_ui_ic_reaction_wut,
    )
) {
    public val types: List<String> = reactions.keys.toList()

    internal fun isReactionTypeSupported(type: String): Boolean {
        return reactions.keys.contains(type)
    }

    @DrawableRes
    internal fun getReactionIcon(type: String): Int? {
        return reactions[type]
    }

    /**
     * Default reaction types provided by Stream SDK
     */
    public object DefaultReactionTypes {
        public const val LOVE: String = "love"
        public const val THUMBS_UP: String = "like"
        public const val THUMBS_DOWN: String = "sad"
        public const val LOL: String = "haha"
        public const val WUT: String = "wow"
    }
}
