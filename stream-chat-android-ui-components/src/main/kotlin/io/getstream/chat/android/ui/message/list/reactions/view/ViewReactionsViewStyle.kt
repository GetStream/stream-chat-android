package io.getstream.chat.android.ui.message.list.reactions.view

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.use
import io.getstream.chat.android.ui.message.list.reactions.view.internal.ViewReactionsView
import java.io.Serializable

public data class ViewReactionsViewStyle(
    public val bubbleBorderColor: Int,
    public val bubbleColorMine: Int,
    public val bubbleColorTheirs: Int,
    public val totalHeight: Int,
    public val horizontalPadding: Int,
    public val itemSize: Int,
    public val bubbleHeight: Int,
    public val bubbleRadius: Int,
    public val largeTailBubbleCy: Int,
    public val largeTailBubbleRadius: Int,
    public val largeTailBubbleOffset: Int,
    public val smallTailBubbleCy: Int,
    public val smallTailBubbleRadius: Int,
    public val smallTailBubbleOffset: Int,
) : Serializable {

    internal companion object {
        private val DEFAULT_BUBBLE_BORDER_COLOR = R.color.stream_ui_grey_whisper
        private val DEFAULT_BUBBLE_COLOR_MINE = R.color.stream_ui_grey_whisper
        private val DEFAULT_BUBBLE_COLOR_THEIRS = R.color.stream_ui_grey_gainsboro

        operator fun invoke(context: Context, attrs: AttributeSet?): ViewReactionsViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.ViewReactionsView,
                0,
                0,
            ).use { a ->
                return Builder(a, context)
                    .bubbleBorderColor(R.styleable.ViewReactionsView_streamUiReactionsBubbleBorderColorMine)
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
            private var bubbleBorderColor: Int = context.getColorCompat(DEFAULT_BUBBLE_BORDER_COLOR)

            fun bubbleColorTheirs(@StyleableRes theirsBubbleColorAttribute: Int) = apply {
                bubbleColorTheirs =
                    array.getColor(theirsBubbleColorAttribute, context.getColorCompat(DEFAULT_BUBBLE_COLOR_THEIRS))
            }

            fun bubbleColorMine(@StyleableRes mineBubbleColorAttribute: Int) = apply {
                bubbleColorMine =
                    array.getColor(mineBubbleColorAttribute, context.getColorCompat(DEFAULT_BUBBLE_COLOR_MINE))
            }

            fun bubbleBorderColor(@StyleableRes bubbleBorderColorAttribute: Int) = apply {
                bubbleBorderColor =
                    array.getColor(bubbleBorderColorAttribute, context.getColorCompat(DEFAULT_BUBBLE_BORDER_COLOR))
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
                    bubbleBorderColor = bubbleBorderColor,
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
                    smallTailBubbleOffset = smallTailBubbleOffset
                ).let(TransformStyle.viewReactionsStyleTransformer::transform)
            }
        }
    }
}
