package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.use

internal class ViewReactionsViewStyle(
    context: Context,
    attrs: AttributeSet?
) : ReactionsViewStyle(
    context = context,
    attrs = attrs,
    defaultBubbleColorMine = R.color.stream_ui_view_reactions_bubble_color_mine,
    defaultBubbleColorTheirs = R.color.stream_ui_view_reactions_bubble_color_theirs,
    defaultTotalHeight = R.dimen.stream_ui_view_reactions_total_height,
    defaultHorizontalPadding = R.dimen.stream_ui_view_reactions_horizontal_padding,
    defaultItemSize = R.dimen.stream_ui_view_reactions_item_size,
    defaultItemMargin = R.dimen.stream_ui_view_reactions_item_margin,
    defaultBubbleHeight = R.dimen.stream_ui_view_reactions_bubble_height,
    defaultBubbleRadius = R.dimen.stream_ui_view_reactions_bubble_radius,
    defaultLargeTailBubbleCy = R.dimen.stream_ui_view_reactions_large_tail_bubble_cy,
    defaultLargeTailBubbleRadius = R.dimen.stream_ui_view_reactions_large_tail_bubble_radius,
    defaultLargeTailBubbleOffset = R.dimen.stream_ui_view_reactions_large_tail_bubble_offset,
    defaultSmallTailBubbleCy = R.dimen.stream_ui_view_reactions_small_tail_bubble_cy,
    defaultSmallTailBubbleRadius = R.dimen.stream_ui_view_reactions_small_tail_bubble_radius,
    defaultSmallTailBubbleOffset = R.dimen.stream_ui_view_reactions_small_tail_bubble_offset,
) {

    val bubbleBorderColor: Int

    init {
        context.obtainStyledAttributes(
            attrs,
            R.styleable.ViewReactionsView,
            0,
            0,
        ).use { a ->
            bubbleBorderColor = a.getColor(
                R.styleable.ViewReactionsView_streamUiReactionsBubbleBorderColorMine,
                context.getColorCompat(R.color.stream_ui_view_reactions_bubble_border_color_mine),
            )
        }
    }
}
