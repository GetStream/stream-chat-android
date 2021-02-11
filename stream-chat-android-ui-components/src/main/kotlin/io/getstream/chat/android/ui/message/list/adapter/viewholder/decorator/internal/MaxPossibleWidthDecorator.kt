package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import androidx.constraintlayout.widget.Guideline
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithMediaAttachmentsViewHolder

internal class MaxPossibleWidthDecorator : BaseDecorator() {
    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    override fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    override fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    ) {
        applyMaxPossibleWidth(viewHolder.binding.marginStart, viewHolder.binding.marginEnd, data)
    }

    private fun applyMaxPossibleWidth(marginStart: Guideline, marginEnd: Guideline, data: MessageListItem.MessageItem) {
        val marginStartPercent = if (data.isTheirs) START_PERCENT else MINE_START_PERCENT
        val marginEndPercent = if (data.isTheirs) THEIR_END_PERCENT else END_PERCENT
        marginStart.setGuidelinePercent(marginStartPercent)
        marginEnd.setGuidelinePercent(marginEndPercent)
    }

    companion object {
        private const val MAX_POSSIBLE_WIDTH_FACTOR = .30f
        private const val START_PERCENT = 0f
        private const val END_PERCENT = 1f
        private const val MINE_START_PERCENT = START_PERCENT + MAX_POSSIBLE_WIDTH_FACTOR
        private const val THEIR_END_PERCENT = END_PERCENT - MAX_POSSIBLE_WIDTH_FACTOR
    }
}
