package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal interface Decorator {
    fun <T : MessageListItem> decorate(viewHolder: BaseMessageItemViewHolder<T>, data: T)
}

internal abstract class BaseDecorator : Decorator {
    override fun <T : MessageListItem> decorate(viewHolder: BaseMessageItemViewHolder<T>, data: T) {
        if (data !is MessageListItem.MessageItem) {
            return
        }
        when (viewHolder) {
            is MessageDeletedViewHolder -> decorateDeletedMessage(viewHolder, data)
            is MessagePlainTextViewHolder -> decoratePlainTextMessage(viewHolder, data)
            is OnlyMediaAttachmentsViewHolder -> decorateOnlyMediaAttachmentsMessage(viewHolder, data)
            is PlainTextWithMediaAttachmentsViewHolder -> decoratePlainTextWithMediaAttachmentsMessage(viewHolder, data)
            is OnlyFileAttachmentsViewHolder -> decorateOnlyFileAttachmentsMessage(viewHolder, data)
            is PlainTextWithFileAttachmentsViewHolder -> decoratePlainTextWithFileAttachmentsMessage(viewHolder, data)
            else -> Unit
        }.exhaustive
    }

    protected open fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem
    ) = Unit

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
