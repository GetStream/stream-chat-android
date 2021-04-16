package io.getstream.chat.android.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_DOWN
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactions.lolDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactions.loveDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactions.thumbsDownDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactions.thumbsUpDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactions.wutDrawable

/**
 * Class allowing to define set of supported reactions
 */
public class SupportedReactions(
    context: Context,
    /**
     * Map with keys corresponding to reaction type, value corresponding to Drawable Int.
     * By default it's filled with standard reactions. You can
     */
    public val reactions: Map<String, ReactionDrawable> = mapOf(
        LOVE to loveDrawable(context),
        THUMBS_UP to thumbsUpDrawable(context),
        THUMBS_DOWN to thumbsDownDrawable(context),
        LOL to lolDrawable(context),
        WUT to wutDrawable(context),
    ),
) {
    public val types: List<String> = reactions.keys.toList()

    internal fun isReactionTypeSupported(type: String): Boolean {
        return reactions.keys.contains(type)
    }

    public fun getReactionIconStateInactive(type: String): Drawable? {
        return reactions[type]?.inactiveDrawable
    }

    public fun getReactionIconStateActive(type: String): Drawable? {
        return reactions[type]?.activeDrawable
    }

    public fun getReactionDrawable(type: String): ReactionDrawable? {
        return reactions[type]
    }

    public class ReactionDrawable(public val inactiveDrawable: Drawable?, public val activeDrawable: Drawable?)

    /**
     * Default reaction types
     */
    public object DefaultReactionTypes {
        public const val LOVE: String = "love"
        public const val THUMBS_UP: String = "like"
        public const val THUMBS_DOWN: String = "sad"
        public const val LOL: String = "haha"
        public const val WUT: String = "wow"
    }

    /**
     * Default reaction drawables
     */
    private object DefaultReactions {
        fun loveDrawable(context: Context): ReactionDrawable {
            val drawableInactive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)?.apply {
                setTint(ContextCompat.getColor(context, R.color.stream_ui_grey))
            }
            val drawableActive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)?.apply {
                setTint(ContextCompat.getColor(context, R.color.stream_ui_accent_blue))
            }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun thumbsUpDrawable(context: Context): ReactionDrawable {
            val drawableInactive =
                ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_up)?.apply {
                    setTint(ContextCompat.getColor(context, R.color.stream_ui_grey))
                }
            val drawableActive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_up)?.apply {
                setTint(ContextCompat.getColor(context, R.color.stream_ui_accent_blue))
            }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun thumbsDownDrawable(context: Context): ReactionDrawable {
            val drawableInactive =
                ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_down)?.apply {
                    setTint(ContextCompat.getColor(context, R.color.stream_ui_grey))
                }
            val drawableActive =
                ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_down)?.apply {
                    setTint(ContextCompat.getColor(context, R.color.stream_ui_accent_blue))
                }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun lolDrawable(context: Context): ReactionDrawable {
            val drawableInactive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_lol)?.apply {
                setTint(ContextCompat.getColor(context, R.color.stream_ui_grey))
            }
            val drawableActive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_lol)?.apply {
                setTint(ContextCompat.getColor(context, R.color.stream_ui_accent_blue))
            }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun wutDrawable(context: Context): ReactionDrawable {
            val drawableInactive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_wut)?.apply {
                setTint(ContextCompat.getColor(context, R.color.stream_ui_grey))
            }
            val drawableActive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_wut)?.apply {
                setTint(ContextCompat.getColor(context, R.color.stream_ui_accent_blue))
            }
            return ReactionDrawable(drawableInactive, drawableActive)
        }
    }
}
