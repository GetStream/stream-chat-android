package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

public class EditReactionsViewStyle internal constructor(
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
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTotalHeight,
                context.getDimension(R.dimen.stream_edit_reactions_total_height)
            )
            reactionsHorizontalPadding = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_edit_reactions_horizontal_padding)
            )
            reactionsItemSize = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsItemSize,
                context.getDimension(R.dimen.stream_edit_reactions_item_size)
            )
            reactionsItemMargin = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsItemMargin,
                context.getDimension(R.dimen.stream_edit_reactions_item_margin)
            )
            reactionsBubbleHeight = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_edit_reactions_bubble_height)
            )
            reactionsBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_edit_reactions_bubble_radius)
            )
            reactionsBigTailCircleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTailCircleBigCy,
                context.getDimension(R.dimen.stream_edit_reactions_tail_circle_big_cy)
            )
            reactionsBigTailCircleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTailCircleBigRadius,
                context.getDimension(R.dimen.stream_edit_reactions_tail_circle_big_radius)
            )
            reactionsBigTailCircleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTailCircleBigOffset,
                context.getDimension(R.dimen.stream_edit_reactions_tail_circle_big_offset)
            )
            reactionsSmallTailCircleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTailCircleSmallCy,
                context.getDimension(R.dimen.stream_edit_reactions_tail_circle_small_cy)
            )
            reactionsSmallTailCircleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTailCircleSmallRadius,
                context.getDimension(R.dimen.stream_edit_reactions_tail_circle_small_radius)
            )
            reactionsSmallTailCircleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTailCircleSmallOffset,
                context.getDimension(R.dimen.stream_edit_reactions_tail_circle_small_offset)
            )
        }
    }
}
