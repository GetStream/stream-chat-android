package io.getstream.chat.android.ui.messages.reactions

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.utils.extensions.getColorCompat
import io.getstream.chat.android.ui.utils.extensions.getDimension
import io.getstream.chat.android.ui.utils.extensions.use

internal abstract class ReactionsViewStyle(
    context: Context,
    attrs: AttributeSet?,
    @ColorRes defaultBubbleColorMine: Int,
    @ColorRes defaultBubbleColorTheirs: Int,
    @DimenRes defaultTotalHeight: Int,
    @DimenRes defaultHorizontalPadding: Int,
    @DimenRes defaultItemSize: Int,
    @DimenRes defaultBubbleHeight: Int,
    @DimenRes defaultBubbleRadius: Int,
    @DimenRes defaultLargeTailBubbleCy: Int,
    @DimenRes defaultLargeTailBubbleRadius: Int,
    @DimenRes defaultLargeTailBubbleOffset: Int,
    @DimenRes defaultSmallTailBubbleCy: Int,
    @DimenRes defaultSmallTailBubbleRadius: Int,
    @DimenRes defaultSmallTailBubbleOffset: Int,
) {

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
            R.styleable.ReactionsView,
            0,
            0,
        ).use { a ->
            bubbleColorMine = a.getColor(
                R.styleable.ReactionsView_streamUiReactionsBubbleColorMine,
                context.getColorCompat(defaultBubbleColorMine),
            )
            bubbleColorTheirs = a.getColor(
                R.styleable.ReactionsView_streamUiReactionsBubbleColorTheirs,
                context.getColorCompat(defaultBubbleColorTheirs),
            )
            totalHeight = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsTotalHeight,
                context.getDimension(defaultTotalHeight),
            )
            horizontalPadding = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsHorizontalPadding,
                context.getDimension(defaultHorizontalPadding),
            )
            itemSize = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsItemSize,
                context.getDimension(defaultItemSize),
            )
            bubbleHeight = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsBubbleHeight,
                context.getDimension(defaultBubbleHeight),
            )
            bubbleRadius = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsBubbleRadius,
                context.getDimension(defaultBubbleRadius),
            )
            largeTailBubbleCy = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsLargeTailBubbleCy,
                context.getDimension(defaultLargeTailBubbleCy),
            )
            largeTailBubbleRadius = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsLargeTailBubbleRadius,
                context.getDimension(defaultLargeTailBubbleRadius),
            )
            largeTailBubbleOffset = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsLargeTailBubbleOffset,
                context.getDimension(defaultLargeTailBubbleOffset),
            )
            smallTailBubbleCy = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsSmallTailBubbleCy,
                context.getDimension(defaultSmallTailBubbleCy),
            )
            smallTailBubbleRadius = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsSmallTailBubbleRadius,
                context.getDimension(defaultSmallTailBubbleRadius),
            )
            smallTailBubbleOffset = a.getDimensionPixelSize(
                R.styleable.ReactionsView_streamUiReactionsSmallTailBubbleOffset,
                context.getDimension(defaultSmallTailBubbleOffset),
            )
        }
    }
}
