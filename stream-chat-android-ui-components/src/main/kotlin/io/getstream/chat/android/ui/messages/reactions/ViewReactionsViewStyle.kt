package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

public class ViewReactionsViewStyle internal constructor(
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
            reactionsTotalHeight = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTotalHeight,
                context.getDimension(R.dimen.stream_view_reactions_total_height)
            )
            reactionsHorizontalPadding = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_view_reactions_horizontal_padding)
            )
            reactionsItemSize = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsItemSize,
                context.getDimension(R.dimen.stream_view_reactions_item_size)
            )
            reactionsItemMargin = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsItemMargin,
                context.getDimension(R.dimen.stream_view_reactions_item_margin)
            )
            reactionsBubbleHeight = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_view_reactions_bubble_height)
            )
            reactionsBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_view_reactions_bubble_radius)
            )
            reactionsBigTailCircleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTailCircleBigCy,
                context.getDimension(R.dimen.stream_view_reactions_tail_circle_big_cy)
            )
            reactionsBigTailCircleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTailCircleBigRadius,
                context.getDimension(R.dimen.stream_view_reactions_tail_circle_big_radius)
            )
            reactionsBigTailCircleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTailCircleBigOffset,
                context.getDimension(R.dimen.stream_view_reactions_tail_circle_big_offset)
            )
            reactionsSmallTailCircleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTailCircleSmallCy,
                context.getDimension(R.dimen.stream_view_reactions_tail_circle_small_cy)
            )
            reactionsSmallTailCircleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTailCircleSmallRadius,
                context.getDimension(R.dimen.stream_view_reactions_tail_circle_small_radius)
            )
            reactionsSmallTailCircleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsTailCircleSmallOffset,
                context.getDimension(R.dimen.stream_view_reactions_tail_circle_small_offset)
            )
        }
    }
}
