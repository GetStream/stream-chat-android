package io.getstream.chat.android.ui.utils.extensions

import io.getstream.chat.android.ui.message.list.reactions.user.SingleReactionViewStyle
import io.getstream.chat.android.ui.message.list.reactions.view.ViewReactionsViewStyle

/**
 * Converts [ViewReactionsViewStyle] to [SingleReactionViewStyle].
 *
 * @return [SingleReactionViewStyle] from [ViewReactionsViewStyle].
 */
internal fun ViewReactionsViewStyle.toSingleReactionViewStyle(): SingleReactionViewStyle {
    return SingleReactionViewStyle(
        bubbleBorderColorMine = bubbleBorderColorMine,
        bubbleBorderColorTheirs = bubbleBorderColorTheirs,
        bubbleColorMine = bubbleColorMine,
        bubbleColorTheirs = bubbleColorTheirs,
        bubbleBorderWidthMine = bubbleBorderWidthMine,
        bubbleBorderWidthTheirs = bubbleBorderWidthTheirs,
        totalHeight = totalHeight,
        bubbleHeight = bubbleHeight,
        bubbleRadius = bubbleRadius,
        largeTailBubbleCy = largeTailBubbleCy,
        largeTailBubbleRadius = largeTailBubbleRadius,
        largeTailBubbleOffset = largeTailBubbleOffset,
        smallTailBubbleCy = smallTailBubbleCy,
        smallTailBubbleRadius = smallTailBubbleRadius,
        smallTailBubbleOffset = smallTailBubbleOffset,
        reactionOrientation = messageOptionsUserReactionOrientation
    )
}