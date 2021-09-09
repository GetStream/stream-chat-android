package io.getstream.chat.android.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionDrawables.lolDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionDrawables.loveDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionDrawables.thumbsDownDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionDrawables.thumbsUpDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionDrawables.wutDrawable
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_DOWN
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat

/**
 * Class allowing to define set of supported reactions.
 * You can customize reactions by providing your own implementation of this class to [ChatUI.supportedReactions].
 *
 * @property reactions Map<String, ReactionDrawable> instance. Map with keys corresponding to reaction type, and
 * value corresponding to [ReactionDrawable] object. By default it's initialized with standard reactions.
 *
 */
public class SupportedReactions(
    context: Context,
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

    public fun getReactionDrawable(type: String): ReactionDrawable? {
        return reactions[type]
    }

    public class ReactionDrawable(public val inactiveDrawable: Drawable, public val activeDrawable: Drawable) {
        public fun getDrawable(isActive: Boolean): Drawable = if (isActive) {
            activeDrawable
        } else {
            inactiveDrawable
        }
    }

    /**
     * Default reaction types.
     */
    public object DefaultReactionTypes {
        public const val LOVE: String = "love"
        public const val THUMBS_UP: String = "like"
        public const val THUMBS_DOWN: String = "sad"
        public const val LOL: String = "haha"
        public const val WUT: String = "wow"
    }

    /**
     * Default reaction drawables.
     */
    private object DefaultReactionDrawables {
        fun loveDrawable(context: Context): ReactionDrawable {
            val drawableInactive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)!!.apply {
                setTint(context.getColorCompat(R.color.stream_ui_grey))
            }
            val drawableActive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love)!!.apply {
                setTint(context.getColorCompat(R.color.stream_ui_accent_blue))
            }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun thumbsUpDrawable(context: Context): ReactionDrawable {
            val drawableInactive =
                ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_up)!!.apply {
                    setTint(context.getColorCompat(R.color.stream_ui_grey))
                }
            val drawableActive =
                ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_up)!!.apply {
                    setTint(context.getColorCompat(R.color.stream_ui_accent_blue))
                }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun thumbsDownDrawable(context: Context): ReactionDrawable {
            val drawableInactive =
                ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_down)!!.apply {
                    setTint(context.getColorCompat(R.color.stream_ui_grey))
                }
            val drawableActive =
                ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_down)!!.apply {
                    setTint(context.getColorCompat(R.color.stream_ui_accent_blue))
                }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun lolDrawable(context: Context): ReactionDrawable {
            val drawableInactive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_lol)!!.apply {
                setTint(context.getColorCompat(R.color.stream_ui_grey))
            }
            val drawableActive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_lol)!!.apply {
                setTint(context.getColorCompat(R.color.stream_ui_accent_blue))
            }
            return ReactionDrawable(drawableInactive, drawableActive)
        }

        fun wutDrawable(context: Context): ReactionDrawable {
            val drawableInactive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_wut)!!.apply {
                setTint(context.getColorCompat(R.color.stream_ui_grey))
            }
            val drawableActive = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_wut)!!.apply {
                setTint(context.getColorCompat(R.color.stream_ui_accent_blue))
            }
            return ReactionDrawable(drawableInactive, drawableActive)
        }
    }
}
