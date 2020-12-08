package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.GapView
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder

internal class GapDecorator : BaseDecorator() {

    private fun setupGapView(gapView: GapView, data: MessageListItem.MessageItem) {
        if (data.positions.contains(MessageListItem.Position.TOP)) {
            gapView.showBigGap()
        } else {
            gapView.showSmallGap()
        }
    }

    override fun decorateDeletedMessage(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        setupGapView(viewHolder.binding.gapView, data)
    }

    override fun decoratePlainTextMessage(viewHolder: MessagePlainTextViewHolder, data: MessageListItem.MessageItem) {
        setupGapView(viewHolder.binding.gapView, data)
    }

    override fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = setupGapView(viewHolder.binding.gapView, data)
}
