package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.DateDividerViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.PlainTextWithMediaAttachmentsViewHolder

internal abstract class BaseDecorator : Decorator {

    final override fun <T : MessageListItem> decorate(
        viewHolder: BaseMessageItemViewHolder<T>,
        data: T,
    ) {
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
            is GiphyViewHolder -> decorateGiphyMessage(viewHolder, data)
            is DateDividerViewHolder -> Unit
            else -> Unit
        }.exhaustive
    }

    protected abstract fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    protected abstract fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    protected abstract fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    protected abstract fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
    )

    protected abstract fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
    )

    protected open fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
    ) = Unit

    abstract fun decorateGiphyMessage(
        viewHolder: GiphyViewHolder,
        data: MessageListItem.MessageItem,
    )
}
