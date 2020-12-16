package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.utils.extensions.exhaustive

internal interface Decorator {
    fun <T : MessageListItem> decorate(viewHolder: BaseMessageItemViewHolder<T>, data: T)
}

internal abstract class BaseDecorator : Decorator {
    override fun <T : MessageListItem> decorate(viewHolder: BaseMessageItemViewHolder<T>, data: T) {
        when (viewHolder) {
            is MessageDeletedViewHolder -> decorateDeletedMessage(viewHolder, data as MessageListItem.MessageItem)
            is MessagePlainTextViewHolder -> decoratePlainTextMessage(viewHolder, data as MessageListItem.MessageItem)
            is OnlyMediaAttachmentsViewHolder -> decorateOnlyMediaAttachmentsMessage(viewHolder, data as MessageListItem.MessageItem)
            is PlainTextWithMediaAttachmentsViewHolder -> decoratePlainTextWithMediaAttachmentsMessage(viewHolder, data as MessageListItem.MessageItem)
            is OnlyFileAttachmentsViewHolder -> decorateOnlyFileAttachmentsMessage(viewHolder, data as MessageListItem.MessageItem)
            else -> Unit
        }.exhaustive
    }

    protected open fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit

    protected open fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit

    protected open fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit

    protected open fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit

    protected open fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit
}
