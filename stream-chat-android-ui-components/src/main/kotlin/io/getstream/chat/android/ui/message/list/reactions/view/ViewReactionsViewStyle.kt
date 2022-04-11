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

package io.getstream.chat.android.ui.message.list.reactions.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getColorOrNull
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.getDimensionOrNull
import io.getstream.chat.android.ui.common.extensions.internal.use

public data class ViewReactionsViewStyle(
    @ColorInt public val bubbleBorderColorMine: Int,
    @ColorInt public val bubbleBorderColorTheirs: Int?,
    @ColorInt public val bubbleColorMine: Int,
    @ColorInt public val bubbleColorTheirs: Int,
    @Px public val bubbleBorderWidthMine: Float,
    @Px public val bubbleBorderWidthTheirs: Float?,
    @Px public val totalHeight: Int,
    @Px public val horizontalPadding: Int,
    @Px public val itemSize: Int,
    @Px public val bubbleHeight: Int,
    @Px public val bubbleRadius: Int,
    @Px public val largeTailBubbleCy: Int,
    @Px public val largeTailBubbleRadius: Int,
    @Px public val largeTailBubbleOffset: Int,
    @Px public val smallTailBubbleCy: Int,
    @Px public val smallTailBubbleRadius: Int,
    @Px public val smallTailBubbleOffset: Int,
) {

    internal companion object {
        private val DEFAULT_BUBBLE_BORDER_COLOR_MINE = R.color.stream_ui_grey_whisper
        private val DEFAULT_BUBBLE_COLOR_MINE = R.color.stream_ui_grey_whisper
        private val DEFAULT_BUBBLE_COLOR_THEIRS = R.color.stream_ui_grey_gainsboro
        private val DEFAULT_BUBBLE_BORDER_WIDTH_MINE = 1.dpToPx() * 1.5f

        operator fun invoke(context: Context, attrs: AttributeSet?): ViewReactionsViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ViewReactionsView,
                0,
                0,
            ).use { a ->
                return Builder(a, context)
                    .bubbleBorderColorMine(R.styleable.ViewReactionsView_streamUiReactionsBubbleBorderColorMine)
                    .bubbleBorderColorTheirs(R.styleable.ViewReactionsView_streamUiReactionsBubbleBorderColorTheirs)
                    .bubbleBorderWidthMine(R.styleable.ViewReactionsView_streamUiReactionsBubbleBorderWidthMine)
                    .bubbleBorderWidthTheirs(R.styleable.ViewReactionsView_streamUiReactionsBubbleBorderWidthTheirs)
                    .bubbleColorMine(R.styleable.ViewReactionsView_streamUiReactionsBubbleColorMine)
                    .bubbleColorTheirs(R.styleable.ViewReactionsView_streamUiReactionsBubbleColorTheirs)
                    .build()
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

            fun build(): ViewReactionsViewStyle {
                val totalHeight =
                    context.getDimension(R.dimen.stream_ui_view_reactions_total_height)
                val horizontalPadding =
                    context.getDimension(R.dimen.stream_ui_view_reactions_horizontal_padding)
                val itemSize =
                    context.getDimension(R.dimen.stream_ui_view_reactions_item_size)
                val bubbleHeight =
                    context.getDimension(R.dimen.stream_ui_view_reactions_bubble_height)
                val bubbleRadius =
                    context.getDimension(R.dimen.stream_ui_view_reactions_bubble_radius)
                val largeTailBubbleCy =
                    context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_cy)
                val largeTailBubbleRadius =
                    context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_radius)
                val largeTailBubbleOffset =
                    context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_offset)
                val smallTailBubbleCy =
                    context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_cy)
                val smallTailBubbleRadius =
                    context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_radius)
                val smallTailBubbleOffset =
                    context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_offset)

                return ViewReactionsViewStyle(
                    bubbleBorderColorMine = bubbleBorderColorMine,
                    bubbleBorderColorTheirs = bubbleBorderColorTheirs,
                    bubbleBorderWidthMine = bubbleBorderWidthMine,
                    bubbleBorderWidthTheirs = bubbleBorderWidthTheirs,
                    bubbleColorMine = bubbleColorMine,
                    bubbleColorTheirs = bubbleColorTheirs,
                    totalHeight = totalHeight,
                    horizontalPadding = horizontalPadding,
                    itemSize = itemSize,
                    bubbleHeight = bubbleHeight,
                    bubbleRadius = bubbleRadius,
                    largeTailBubbleCy = largeTailBubbleCy,
                    largeTailBubbleRadius = largeTailBubbleRadius,
                    largeTailBubbleOffset = largeTailBubbleOffset,
                    smallTailBubbleCy = smallTailBubbleCy,
                    smallTailBubbleRadius = smallTailBubbleRadius,
                    smallTailBubbleOffset = smallTailBubbleOffset,
                ).let(TransformStyle.viewReactionsStyleTransformer::transform)
            }
        }
    }
}
