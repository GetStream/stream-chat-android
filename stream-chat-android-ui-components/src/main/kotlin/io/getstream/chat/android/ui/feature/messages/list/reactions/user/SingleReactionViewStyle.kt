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

package io.getstream.chat.android.ui.feature.messages.list.reactions.user

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.state.messages.list.MessageOptionsUserReactionAlignment
import io.getstream.chat.android.ui.feature.messages.list.reactions.view.ViewReactionsViewStyle
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.android.ui.helper.ViewStyle
import io.getstream.chat.android.ui.utils.extensions.dpToPx
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getColorOrNull
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.getDimensionOrNull
import io.getstream.chat.android.ui.utils.extensions.toSingleReactionViewStyle
import io.getstream.chat.android.ui.utils.extensions.use

/**
 * Style for [SingleReactionView].
 * Use this class together with [TransformStyle.singleReactionViewStyleTransformer] to change [SingleReactionView]
 * styles programmatically.
 *
 * @param bubbleBorderColorMine Reaction bubble border color for the current user.
 * @param bubbleBorderColorTheirs Reaction bubble border color for other users.
 * @param bubbleColorMine Reaction bubble color for the current user.
 * @param bubbleColorTheirs Reaction bubble color for other users.
 * @param bubbleBorderWidthMine Reaction bubble border width for the current user.
 * @param bubbleBorderWidthTheirs Reaction bubble border width for other users.
 * @param totalHeight The total height of the reaction bubble.
 * @param bubbleHeight Height of the reactions part of the bubble.
 * @param bubbleRadius The radius of the reactions part of the bubble.
 * @param largeTailBubbleCy The y axis position of the large tail bubble center point.
 * @param largeTailBubbleRadius The radius of the large tail bubble.
 * @param largeTailBubbleOffset The x axis offset of the large tail bubble center point.
 * @param smallTailBubbleCy The y axis position of the large tail bubble center point.
 * @param smallTailBubbleOffset The x axis offset of the small tail bubble center point
 * @param reactionOrientation The orientation of the bubble. By default is [MessageOptionsUserReactionAlignment.BY_USER]
 */
public data class SingleReactionViewStyle(
    @ColorInt public val bubbleBorderColorMine: Int,
    @ColorInt public val bubbleBorderColorTheirs: Int?,
    @ColorInt public val bubbleColorMine: Int,
    @ColorInt public val bubbleColorTheirs: Int,
    @Px public val bubbleBorderWidthMine: Float,
    @Px public val bubbleBorderWidthTheirs: Float?,
    @Px public val totalHeight: Int,
    @Px public val bubbleHeight: Int,
    @Px public val bubbleRadius: Int,
    @Px public val largeTailBubbleCy: Int,
    @Px public val largeTailBubbleRadius: Int,
    @Px public val largeTailBubbleOffset: Int,
    @Px public val smallTailBubbleCy: Int,
    @Px public val smallTailBubbleRadius: Int,
    @Px public val smallTailBubbleOffset: Int,
    public val reactionOrientation: Int,
) : ViewStyle {

    internal companion object {
        private val DEFAULT_BUBBLE_BORDER_COLOR_MINE = R.color.stream_ui_grey_whisper
        private val DEFAULT_BUBBLE_COLOR_MINE = R.color.stream_ui_grey_whisper
        private val DEFAULT_BUBBLE_COLOR_THEIRS = R.color.stream_ui_grey_gainsboro
        private val DEFAULT_BUBBLE_BORDER_WIDTH_MINE = 1.dpToPx() * 1.5f

        operator fun invoke(context: Context, attrs: AttributeSet?): SingleReactionViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.SingleReactionView,
                R.attr.streamUiSingleReactionViewStyle,
                0,
            ).use { a ->
                return if (a.indexCount != 0) {
                    Builder(a, context)
                        .bubbleBorderColorMine(
                            R.styleable.SingleReactionView_streamUiSingleReactionBubbleBorderColorMine,
                        )
                        .bubbleBorderColorTheirs(
                            R.styleable.SingleReactionView_streamUiSingleReactionBubbleBorderColorTheirs,
                        )
                        .bubbleBorderWidthMine(
                            R.styleable.SingleReactionView_streamUiSingleReactionBubbleBorderWidthMine,
                        )
                        .bubbleBorderWidthTheirs(
                            R.styleable.SingleReactionView_streamUiSingleReactionBubbleBorderWidthTheirs,
                        )
                        .bubbleColorMine(
                            R.styleable.SingleReactionView_streamUiSingleReactionBubbleColorMine,
                        )
                        .bubbleColorTheirs(
                            R.styleable.SingleReactionView_streamUiSingleReactionBubbleColorTheirs,
                        )
                        .messageOptionsUserReactionBubbleOrientation(
                            R.styleable.SingleReactionView_streamUiSingleReactionBubbleOrientation,
                        )
                        .build()
                } else {
                    ViewReactionsViewStyle(context, attrs).toSingleReactionViewStyle()
                }
            }
        }

        class Builder(private val array: TypedArray, private val context: Context) {
            @ColorInt
            private var bubbleColorTheirs: Int = context.getColorCompat(DEFAULT_BUBBLE_COLOR_THEIRS)

            @ColorInt
            private var bubbleColorMine: Int = context.getColorCompat(DEFAULT_BUBBLE_COLOR_MINE)

            @ColorInt
            private var bubbleBorderColorMine: Int = context.getColorCompat(DEFAULT_BUBBLE_BORDER_COLOR_MINE)

            @ColorInt
            private var bubbleBorderColorTheirs: Int? = null

            @Px
            private var bubbleBorderWidthMine: Float = DEFAULT_BUBBLE_BORDER_WIDTH_MINE

            @Px
            private var bubbleBorderWidthTheirs: Float? = null
            private var reactionOrientation: Int =
                MessageOptionsUserReactionAlignment.BY_USER.value

            fun bubbleColorTheirs(@StyleableRes theirsBubbleColorAttribute: Int) = apply {
                bubbleColorTheirs =
                    array.getColor(theirsBubbleColorAttribute, context.getColorCompat(DEFAULT_BUBBLE_COLOR_THEIRS))
            }

            fun bubbleColorMine(@StyleableRes mineBubbleColorAttribute: Int) = apply {
                bubbleColorMine =
                    array.getColor(mineBubbleColorAttribute, context.getColorCompat(DEFAULT_BUBBLE_COLOR_MINE))
            }

            fun bubbleBorderColorMine(@StyleableRes bubbleBorderColorAttribute: Int) = apply {
                bubbleBorderColorMine =
                    array.getColor(bubbleBorderColorAttribute, context.getColorCompat(DEFAULT_BUBBLE_BORDER_COLOR_MINE))
            }

            fun bubbleBorderColorTheirs(@StyleableRes bubbleBorderColorAttribute: Int) = apply {
                bubbleBorderColorTheirs = array.getColorOrNull(bubbleBorderColorAttribute)
            }

            fun bubbleBorderWidthMine(@StyleableRes bubbleBorderWidthAttribute: Int) = apply {
                bubbleBorderWidthMine = array.getDimension(bubbleBorderWidthAttribute, DEFAULT_BUBBLE_BORDER_WIDTH_MINE)
            }

            fun bubbleBorderWidthTheirs(@StyleableRes bubbleBorderWidthAttribute: Int) = apply {
                bubbleBorderWidthTheirs = array.getDimensionOrNull(bubbleBorderWidthAttribute)
            }

            fun messageOptionsUserReactionBubbleOrientation(@StyleableRes bubbleOrientation: Int) = apply {
                this.reactionOrientation =
                    array.getInt(bubbleOrientation, MessageOptionsUserReactionAlignment.BY_USER.value)
            }

            fun build(): SingleReactionViewStyle {
                val totalHeight =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_total_height)
                val bubbleHeight =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_bubble_height)
                val bubbleRadius =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_bubble_radius)
                val largeTailBubbleCy =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_large_tail_bubble_cy)
                val largeTailBubbleRadius =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_large_tail_bubble_radius)
                val largeTailBubbleOffset =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_large_tail_bubble_offset)
                val smallTailBubbleCy =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_small_tail_bubble_cy)
                val smallTailBubbleRadius =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_small_tail_bubble_radius)
                val smallTailBubbleOffset =
                    context.getDimension(R.dimen.stream_ui_single_reaction_view_small_tail_bubble_offset)

                return SingleReactionViewStyle(
                    bubbleBorderColorMine = bubbleBorderColorMine,
                    bubbleBorderColorTheirs = bubbleBorderColorTheirs,
                    bubbleBorderWidthMine = bubbleBorderWidthMine,
                    bubbleBorderWidthTheirs = bubbleBorderWidthTheirs,
                    bubbleColorMine = bubbleColorMine,
                    bubbleColorTheirs = bubbleColorTheirs,
                    totalHeight = totalHeight,
                    bubbleHeight = bubbleHeight,
                    bubbleRadius = bubbleRadius,
                    largeTailBubbleCy = largeTailBubbleCy,
                    largeTailBubbleRadius = largeTailBubbleRadius,
                    largeTailBubbleOffset = largeTailBubbleOffset,
                    smallTailBubbleCy = smallTailBubbleCy,
                    smallTailBubbleRadius = smallTailBubbleRadius,
                    smallTailBubbleOffset = smallTailBubbleOffset,
                    reactionOrientation = reactionOrientation,
                ).let(TransformStyle.singleReactionViewStyleTransformer::transform)
            }
        }
    }
}
