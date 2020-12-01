package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.utils.extensions.exhaustive

internal interface Decorator {
    fun <T : MessageListItem> decorate(viewHolder: BaseMessageItemViewHolder<T>, data: T)
}

internal abstract class BaseDecorator : Decorator {
    override fun <T : MessageListItem> decorate(viewHolder: BaseMessageItemViewHolder<T>, data: T) {
        when (viewHolder) {
            is MessageDeletedViewHolder -> decorateMessageDeleted(viewHolder, data as MessageListItem.MessageItem)
            else -> Unit
        }.exhaustive
    }

    protected abstract fun decorateMessageDeleted(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem
    )
}
