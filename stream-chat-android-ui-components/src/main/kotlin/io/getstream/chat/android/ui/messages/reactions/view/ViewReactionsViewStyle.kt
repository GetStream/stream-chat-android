package io.getstream.chat.android.ui.messages.reactions.view

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

internal class ViewReactionsViewStyle(context: Context, attrs: AttributeSet?) {
    val bubbleBorderColor: Int
    val bubbleColorMine: Int
    val bubbleColorTheirs: Int
    val totalHeight: Int
    val horizontalPadding: Int
    val itemSize: Int
    val bubbleHeight: Int
    val bubbleRadius: Int
    val largeTailBubbleCy: Int
    val largeTailBubbleRadius: Int
    val largeTailBubbleOffset: Int
    val smallTailBubbleCy: Int
    val smallTailBubbleRadius: Int
    val smallTailBubbleOffset: Int

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.ViewReactionsView,
            0,
            0,
        ).use { a ->
            bubbleBorderColor = a.getColor(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleBorderColorMine,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_border_color_mine)
            )
            bubbleColorMine = a.getColor(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_color_mine)
            )
            bubbleColorTheirs = a.getColor(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleColorTheirs,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_color_theirs)
            )
            totalHeight = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsTotalHeight,
                context.getDimension(R.dimen.stream_ui_view_reactions_total_height)
            )
            horizontalPadding = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_ui_view_reactions_horizontal_padding)
            )
            itemSize = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsItemSize,
                context.getDimension(R.dimen.stream_ui_view_reactions_item_size)
            )
            bubbleHeight = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_ui_view_reactions_bubble_height)
            )
            bubbleRadius = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_ui_view_reactions_bubble_radius)
            )
            largeTailBubbleCy = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsLargeTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_cy)
            )
            largeTailBubbleRadius = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsLargeTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_radius)
            )
            largeTailBubbleOffset = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsLargeTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_offset)
            )
            smallTailBubbleCy = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsSmallTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_cy)
            )
            smallTailBubbleRadius = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsSmallTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_radius)
            )
            smallTailBubbleOffset = a.getDimensionPixelSize(
                R.styleable.ViewReactionsView_streamUiReactionsSmallTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_offset)
            )
        }
    }
}
