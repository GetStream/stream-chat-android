package io.getstream.chat.android.ui.messages.reactions.edit

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

internal class EditReactionsViewStyle(context: Context, attrs: AttributeSet?) {
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
            R.styleable.EditReactionsView,
            0,
            0,
        ).use { a ->
            bubbleColorMine = a.getColor(
                R.styleable.EditReactionsView_streamUiReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_edit_reactions_bubble_color_mine),
            )
            bubbleColorTheirs = a.getColor(
                R.styleable.EditReactionsView_streamUiReactionsBubbleColorTheirs,
                context.getColorCompat(R.color.stream_ui_edit_reactions_bubble_color_theirs),
            )
            totalHeight = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsTotalHeight,
                context.getDimension(R.dimen.stream_ui_edit_reactions_total_height),
            )
            horizontalPadding = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsHorizontalPadding,
                context.getDimension(R.dimen.stream_ui_edit_reactions_horizontal_padding),
            )
            itemSize = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsItemSize,
                context.getDimension(R.dimen.stream_ui_edit_reactions_item_size),
            )
            bubbleHeight = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsBubbleHeight,
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_height),
            )
            bubbleRadius = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_radius),
            )
            largeTailBubbleCy = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsLargeTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_cy),
            )
            largeTailBubbleRadius = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsLargeTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_radius),
            )
            largeTailBubbleOffset = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsLargeTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_offset),
            )
            smallTailBubbleCy = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsSmallTailBubbleCy,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_cy),
            )
            smallTailBubbleRadius = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsSmallTailBubbleRadius,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_radius),
            )
            smallTailBubbleOffset = a.getDimensionPixelSize(
                R.styleable.EditReactionsView_streamUiReactionsSmallTailBubbleOffset,
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_offset),
            )
        }
    }
}
