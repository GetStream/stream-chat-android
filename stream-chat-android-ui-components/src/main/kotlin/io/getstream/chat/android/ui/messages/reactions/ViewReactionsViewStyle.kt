package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

internal class ViewReactionsViewStyle constructor(
    context: Context,
    attrs: AttributeSet?
) : ReactionsViewStyle() {

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.StreamUiReactionsViewStyle,
            0,
            0
        ).use {
            bubbleColorMine = it.getColor(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_color_mine)
            )
            bubbleColorTheirs = it.getColor(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsBubbleColorTheirs,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_color_theirs)
            )
            bubbleBorderColor = it.getColor(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsBubbleColorTheirs,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_border_color_mine)
            )
            totalHeight = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsTotalHeight,
                context.getDimension(R.dimen.stream_ui_view_reactions_total_height)
            )
            horizontalPadding = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_ui_view_reactions_horizontal_padding)
            )
            itemSize = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsItemSize,
                context.getDimension(R.dimen.stream_ui_view_reactions_item_size)
            )
            itemMargin = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsItemMargin,
                context.getDimension(R.dimen.stream_ui_view_reactions_item_margin)
            )
            bubbleHeight = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_ui_view_reactions_bubble_height)
            )
            bubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_ui_view_reactions_bubble_radius)
            )
            largeTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsLargeTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_cy)
            )
            largeTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsLargeTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_radius)
            )
            largeTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsLargeTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_offset)
            )
            smallTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsSmallTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_cy)
            )
            smallTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsSmallTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_radius)
            )
            smallTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsSmallTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_offset)
            )
        }
    }
}
