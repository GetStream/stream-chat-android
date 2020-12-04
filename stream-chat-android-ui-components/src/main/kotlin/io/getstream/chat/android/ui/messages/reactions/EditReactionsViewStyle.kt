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
            R.styleable.StreamUiReactionsViewStyle,
            0,
            0
        ).use {
            bubbleColorMine = it.getColor(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_edit_reactions_bubble_color_mine)
            )
            bubbleColorTheirs = it.getColor(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_edit_reactions_bubble_color_theirs)
            )
            totalHeight = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsTotalHeight,
                context.getDimension(R.dimen.stream_ui_edit_reactions_total_height)
            )
            horizontalPadding = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_ui_edit_reactions_horizontal_padding)
            )
            itemSize = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsItemSize,
                context.getDimension(R.dimen.stream_ui_edit_reactions_item_size)
            )
            itemMargin = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiViewReactionsItemMargin,
                context.getDimension(R.dimen.stream_ui_edit_reactions_item_margin)
            )
            bubbleHeight = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_height)
            )
            bubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_radius)
            )
            largeTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsLargeTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_cy)
            )
            largeTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsLargeTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_radius)
            )
            largeTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsLargeTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_offset)
            )
            smallTailBubbleCy = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsSmallTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_cy)
            )
            smallTailBubbleRadius = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsSmallTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_radius)
            )
            smallTailBubbleOffset = it.getDimensionPixelSize(
                R.styleable.StreamUiReactionsViewStyle_streamUiEditReactionsSmallTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_offset)
            )
        }
    }
}
