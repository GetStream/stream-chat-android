/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.ui.helper

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.helper.ReactionDefaults
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionDrawables.lolDrawable
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionDrawables.loveDrawable
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionDrawables.thumbsDownDrawable
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionDrawables.thumbsUpDrawable
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionDrawables.wutDrawable
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOL
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.LOVE
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.THUMBS_DOWN
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.THUMBS_UP
import io.getstream.chat.android.ui.helper.SupportedReactions.DefaultReactionTypes.WUT
import io.getstream.chat.android.ui.utils.extensions.applyTint
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDrawableCompat

/**
 * Class allowing to define set of supported reactions. You can customize reactions by providing your own
 * implementation of this class to [ChatUI.supportedReactions].
 *
 * @param context The context to load drawable resources.
 * @param reactions Map<String, ReactionDrawable> instance. Map with keys corresponding to reaction type, and
 * value corresponding to [ReactionDrawable] object. By default it's initialized with standard reactions.
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

    /**
     * Contains drawables for normal and selected reaction states.
     *
     * @param inactiveDrawable The drawable for the normal icon.
     * @param activeDrawable The drawable for the selected state icon.
     */
    public class ReactionDrawable(
        private val inactiveDrawable: Drawable,
        private val activeDrawable: Drawable,
    ) {
        public fun getDrawable(isActive: Boolean): Drawable = if (isActive) {
            activeDrawable
        } else {
            inactiveDrawable
        }
    }

    /**
     * The reaction types we support by default.
     */
    public object DefaultReactionTypes {
        public const val LOVE: String = ReactionDefaults.LOVE
        public const val THUMBS_UP: String = ReactionDefaults.THUMBS_UP
        public const val THUMBS_DOWN: String = ReactionDefaults.THUMBS_DOWN
        public const val LOL: String = ReactionDefaults.LOL
        public const val WUT: String = ReactionDefaults.WUT
    }

    /**
     * Default reaction drawables.
     */
    private object DefaultReactionDrawables {
        fun loveDrawable(context: Context): ReactionDrawable {
            return createTintedReactionDrawable(context, R.drawable.stream_ui_ic_reaction_love)
        }

        fun thumbsUpDrawable(context: Context): ReactionDrawable {
            return createTintedReactionDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_up)
        }

        fun thumbsDownDrawable(context: Context): ReactionDrawable {
            return createTintedReactionDrawable(context, R.drawable.stream_ui_ic_reaction_thumbs_down)
        }

        fun lolDrawable(context: Context): ReactionDrawable {
            return createTintedReactionDrawable(context, R.drawable.stream_ui_ic_reaction_lol)
        }

        fun wutDrawable(context: Context): ReactionDrawable {
            return createTintedReactionDrawable(context, R.drawable.stream_ui_ic_reaction_wut)
        }

        /**
         * Creates [ReactionDrawable] from the desired reaction drawable resource id.
         *
         * @param context The context to load drawable resources.
         * @param drawableResId The reaction drawable id.
         */
        private fun createTintedReactionDrawable(
            context: Context,
            @DrawableRes drawableResId: Int,
        ): ReactionDrawable {
            return ReactionDrawable(
                inactiveDrawable = context.getDrawableCompat(drawableResId)!!
                    .applyTint(context.getColorCompat(R.color.stream_ui_grey)),
                activeDrawable = context.getDrawableCompat(drawableResId)!!
                    .applyTint(context.getColorCompat(R.color.stream_ui_accent_blue)),
            )
        }
    }
}
