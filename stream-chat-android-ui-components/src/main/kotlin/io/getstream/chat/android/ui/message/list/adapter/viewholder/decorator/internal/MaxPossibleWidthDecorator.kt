package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.constraintlayout.widget.Guideline
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

internal class MaxPossibleWidthDecorator(private val style: MessageListItemStyle) : BaseDecorator() {
    override fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) = applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    private fun applyMaxPossibleWidth(marginStart: Guideline, marginEnd: Guideline, data: MessageListItem.MessageItem) {
        val marginStartPercent = if (data.isTheirs) {
            START_PERCENT
        } else {
            START_PERCENT + getMaxPossibleWidthFactor(data.isTheirs)
        }
        val marginEndPercent = if (data.isTheirs) {
            END_PERCENT - getMaxPossibleWidthFactor(data.isTheirs)
        } else {
            END_PERCENT
        }
        marginStart.setGuidelinePercent(marginStartPercent)
        marginEnd.setGuidelinePercent(marginEndPercent)
    }

    /**
     * Gets message's max possible width factor from [style] based on [isTheirs] and scales it using [MAX_POSSIBLE_WIDTH_FACTOR]
     */
    private fun getMaxPossibleWidthFactor(isTheirs: Boolean): Float {
        val maxPossibleWidthFactor = if (isTheirs) {
            style.messageMaxPossibleWidthFactorTheirs
        } else {
            style.messageMaxPossibleWidthFactorMine
        }

        return maxPossibleWidthFactor * MAX_POSSIBLE_WIDTH_FACTOR
    }

    companion object {
        private const val MAX_POSSIBLE_WIDTH_FACTOR = .25f
        private const val START_PERCENT = 0f
        private const val END_PERCENT = 0.97f
        private const val MINE_START_PERCENT = START_PERCENT + MAX_POSSIBLE_WIDTH_FACTOR
        private const val THEIR_END_PERCENT = END_PERCENT - MAX_POSSIBLE_WIDTH_FACTOR
    }
}
