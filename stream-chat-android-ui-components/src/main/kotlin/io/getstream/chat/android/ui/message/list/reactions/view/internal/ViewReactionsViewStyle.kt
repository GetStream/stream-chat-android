package io.getstream.chat.android.ui.message.list.reactions.view.internal

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.use

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
                context.getColorCompat(R.color.stream_ui_grey_whisper)
            )
            bubbleColorMine = a.getColor(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleColorMine,
                context.getColorCompat(R.color.stream_ui_white)
            )
            bubbleColorTheirs = a.getColor(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleColorTheirs,
                context.getColorCompat(R.color.stream_ui_grey_gainsboro)
            )
            totalHeight =
                context.getDimension(R.dimen.stream_ui_view_reactions_total_height)
            horizontalPadding =
                context.getDimension(R.dimen.stream_ui_view_reactions_horizontal_padding)
            itemSize =
                context.getDimension(R.dimen.stream_ui_view_reactions_item_size)
            bubbleHeight =
                context.getDimension(R.dimen.stream_ui_view_reactions_bubble_height)
            bubbleRadius =
                context.getDimension(R.dimen.stream_ui_view_reactions_bubble_radius)
            largeTailBubbleCy =
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_cy)
            largeTailBubbleRadius =
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_radius)
            largeTailBubbleOffset =
                context.getDimension(R.dimen.stream_ui_view_reactions_large_tail_bubble_offset)
            smallTailBubbleCy =
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_cy)
            smallTailBubbleRadius =
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_radius)
            smallTailBubbleOffset =
                context.getDimension(R.dimen.stream_ui_view_reactions_small_tail_bubble_offset)
        }
    }
}
