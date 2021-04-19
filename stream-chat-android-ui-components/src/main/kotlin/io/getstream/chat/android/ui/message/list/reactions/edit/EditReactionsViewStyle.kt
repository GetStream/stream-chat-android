package io.getstream.chat.android.ui.message.list.reactions.edit

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.annotation.ColorInt
import androidx.annotation.Px
import androidx.annotation.StyleableRes
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.TransformStyle
import io.getstream.chat.android.ui.common.extensions.internal.getColorCompat
import io.getstream.chat.android.ui.common.extensions.internal.getDimension
import io.getstream.chat.android.ui.common.extensions.internal.use
import java.io.Serializable

public data class EditReactionsViewStyle(
    @ColorInt public val bubbleColorMine: Int,
    @ColorInt public val bubbleColorTheirs: Int,
    @Px public val totalHeight: Int,
    @Px public val horizontalPadding: Int,
    @Px public val itemSize: Int,
    @Px public val bubbleHeight: Int,
    @Px public val bubbleRadius: Int,
    @Px public val largeTailBubbleCy: Int,
    @Px public val largeTailBubbleRadius: Int,
    @Px public val largeTailBubbleOffset: Int,
    @Px public val smallTailBubbleCy: Int,
    @Px public val smallTailBubbleRadius: Int,
    @Px public val smallTailBubbleOffset: Int,
) : Serializable {

    internal data class Builder(private val array: TypedArray, private val context: Context) {
        @ColorInt
        private var bubbleColorMine: Int = context.getColorCompat(R.color.stream_ui_white)

        @ColorInt
        private var bubbleColorTheirs: Int = context.getColorCompat(R.color.stream_ui_white)

        fun bubbleColorMine(@StyleableRes bubbleColorMineResId: Int) = apply {
            bubbleColorMine = array.getColor(bubbleColorMineResId, context.getColorCompat(R.color.stream_ui_white))
        }

        fun bubbleColorTheirs(@StyleableRes bubbleColorTheirsResId: Int) = apply {
            bubbleColorTheirs = array.getColor(bubbleColorTheirsResId, context.getColorCompat(R.color.stream_ui_white))
        }

        fun build(): EditReactionsViewStyle {
            val totalHeight =
                context.getDimension(R.dimen.stream_ui_edit_reactions_total_height)
            val horizontalPadding =
                context.getDimension(R.dimen.stream_ui_edit_reactions_horizontal_padding)
            val itemSize =
                context.getDimension(R.dimen.stream_ui_edit_reactions_item_size)
            val bubbleHeight =
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_height)
            val bubbleRadius =
                context.getDimension(R.dimen.stream_ui_edit_reactions_bubble_radius)
            val largeTailBubbleCy =
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_cy)
            val largeTailBubbleRadius =
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_radius)
            val largeTailBubbleOffset =
                context.getDimension(R.dimen.stream_ui_edit_reactions_large_tail_bubble_offset)
            val smallTailBubbleCy =
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_cy)
            val smallTailBubbleRadius =
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_radius)
            val smallTailBubbleOffset =
                context.getDimension(R.dimen.stream_ui_edit_reactions_small_tail_bubble_offset)

            return EditReactionsViewStyle(
                bubbleColorMine = bubbleColorMine,
                bubbleColorTheirs = bubbleColorTheirs,
                totalHeight = totalHeight,
                horizontalPadding = horizontalPadding,
                itemSize = itemSize,
                bubbleHeight = bubbleHeight,
                bubbleRadius = bubbleRadius,
                largeTailBubbleCy = largeTailBubbleCy,
                largeTailBubbleRadius = largeTailBubbleRadius,
                largeTailBubbleOffset = largeTailBubbleOffset,
                smallTailBubbleCy = smallTailBubbleCy,
                smallTailBubbleRadius = smallTailBubbleRadius,
                smallTailBubbleOffset = smallTailBubbleOffset
            ).let(TransformStyle.editReactionsStyleTransformer::transform)
        }
    }

    public companion object {

        internal operator fun invoke(context: Context, attrs: AttributeSet?): EditReactionsViewStyle {
            context.obtainStyledAttributes(
                attrs,
                R.styleable.EditReactionsView,
                0,
                0,
            ).use { a ->
                return Builder(a, context)
                    .bubbleColorMine(R.styleable.EditReactionsView_streamUiReactionsBubbleColorMine)
                    .bubbleColorTheirs(R.styleable.EditReactionsView_streamUiReactionsBubbleColorTheirs)
                    .build()
            }
        }
    }
}
