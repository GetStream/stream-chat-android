package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder

internal class SpaceDecorator : BaseDecorator() {

    override fun decorateMessageDeleted(viewHolder: MessageDeletedViewHolder, data: MessageListItem.MessageItem) {
        if (data.positions.contains(MessageListItem.Position.TOP)) {
            viewHolder.binding.bigGap.isVisible = true
            viewHolder.binding.standardGap.isVisible = false
        } else {
            viewHolder.binding.bigGap.isVisible = false
            viewHolder.binding.standardGap.isVisible = true
        }
    }
}