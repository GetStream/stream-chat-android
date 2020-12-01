package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder

internal class GapDecorator : BaseDecorator() {

    override fun decorateMessageDeleted(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        if (data.positions.contains(MessageListItem.Position.TOP)) {
            viewHolder.binding.gapView.showBigGap()
        } else {
            viewHolder.binding.gapView.showSmallGap()
        }
    }
}
