package io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.message.list.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.DateDividerViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder

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
            is TextAndAttachmentsViewHolder -> decorateTextAndAttachmentsMessage(viewHolder, data)
            is GiphyViewHolder -> decorateGiphyMessage(viewHolder, data)
            is DateDividerViewHolder -> Unit
            else -> Unit
        }.exhaustive
    }

    abstract fun decorateTextAndAttachmentsMessage(
        viewHolder: TextAndAttachmentsViewHolder,
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
