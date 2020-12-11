package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import io.getstream.chat.android.ui.R

internal class EditReactionsViewStyle(
    context: Context,
    attrs: AttributeSet?
) : ReactionsViewStyle(
    context = context,
    attrs = attrs,
    defaultBubbleColorMine = R.color.stream_ui_edit_reactions_bubble_color_mine,
    defaultBubbleColorTheirs = R.color.stream_ui_edit_reactions_bubble_color_theirs,
    defaultTotalHeight = R.dimen.stream_ui_edit_reactions_total_height,
    defaultHorizontalPadding = R.dimen.stream_ui_edit_reactions_horizontal_padding,
    defaultItemSize = R.dimen.stream_ui_edit_reactions_item_size,
    defaultItemMargin = R.dimen.stream_ui_edit_reactions_item_margin,
    defaultBubbleHeight = R.dimen.stream_ui_edit_reactions_bubble_height,
    defaultBubbleRadius = R.dimen.stream_ui_edit_reactions_bubble_radius,
    defaultLargeTailBubbleCy = R.dimen.stream_ui_edit_reactions_large_tail_bubble_cy,
    defaultLargeTailBubbleRadius = R.dimen.stream_ui_edit_reactions_large_tail_bubble_radius,
    defaultLargeTailBubbleOffset = R.dimen.stream_ui_edit_reactions_large_tail_bubble_offset,
    defaultSmallTailBubbleCy = R.dimen.stream_ui_edit_reactions_small_tail_bubble_cy,
    defaultSmallTailBubbleRadius = R.dimen.stream_ui_edit_reactions_small_tail_bubble_radius,
    defaultSmallTailBubbleOffset = R.dimen.stream_ui_edit_reactions_small_tail_bubble_offset,
)
