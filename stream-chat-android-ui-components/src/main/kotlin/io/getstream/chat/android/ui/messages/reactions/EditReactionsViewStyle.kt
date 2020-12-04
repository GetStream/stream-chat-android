package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

internal class EditReactionsViewStyle(
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
                context.getColorCompat(R.color.stream_ui_edit_reactions_bubble_color_mine)
            )
            bubbleColorTheirs = it.getColor(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_edit_reactions_bubble_color_theirs)
            )
            totalHeight = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsTotalHeight,
                context.getDimension(R.dimen.stream_ui_edit_reactions_total_height)
            )
            horizontalPadding = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_ui_edit_reactions_horizontal_padding)
            )
            itemSize = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsItemSize,
                context.getDimension(R.dimen.stream_ui_edit_reactions_item_size)
            )
            itemMargin = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamViewReactionsItemMargin,
                context.getDimension(R.dimen.stream_ui_edit_reactions_item_margin)
            )
            bubbleHeight = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_height)
            )
            bubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_radius)
            )
            largeTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsLargeTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_cy)
            )
            largeTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsLargeTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_radius)
            )
            largeTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsLargeTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_offset)
            )
            smallTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsSmallTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_cy)
            )
            smallTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsSmallTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_radius)
            )
            smallTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamReactionsViewStyle_streamEditReactionsSmallTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_offset)
            )
        }
    }
}
