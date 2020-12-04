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
            R.styleable.StreamReactionsViewStyle,
            0,
            0
        ).use {
            bubbleColorMine = it.getColor(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_color_mine)
            )
            bubbleColorTheirs = it.getColor(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleColorTheirs,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_color_theirs)
            )
            bubbleBorderColor = it.getColor(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleColorTheirs,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_border_color_mine)
            )
            totalHeight = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTotalHeight,
                context.getDimension(R.dimen.stream_view_reactions_total_height)
            )
            horizontalPadding = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_view_reactions_horizontal_padding)
            )
            itemSize = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsItemSize,
                context.getDimension(R.dimen.stream_view_reactions_item_size)
            )
            itemMargin = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsItemMargin,
                context.getDimension(R.dimen.stream_view_reactions_item_margin)
            )
            bubbleHeight = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_view_reactions_bubble_height)
            )
            bubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_view_reactions_bubble_radius)
            )
            largeTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsLargeTailBubbleCy,
                context.getDimension(R.dimen.stream_view_reactions_large_tail_bubble_cy)
            )
            largeTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsLargeTailBubbleRadius,
                context.getDimension(R.dimen.stream_view_reactions_large_tail_bubble_radius)
            )
            largeTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsLargeTailBubbleOffset,
                context.getDimension(R.dimen.stream_view_reactions_large_tail_bubble_offset)
            )
            smallTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsSmallTailBubbleCy,
                context.getDimension(R.dimen.stream_view_reactions_small_tail_bubble_cy)
            )
            smallTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsSmallTailBubbleRadius,
                context.getDimension(R.dimen.stream_view_reactions_small_tail_bubble_radius)
            )
            smallTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsSmallTailBubbleOffset,
                context.getDimension(R.dimen.stream_view_reactions_small_tail_bubble_offset)
            )
        }
    }
}
