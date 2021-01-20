package io.getstream.chat.android.ui.messages.adapter.viewholder.decorator

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.core.internal.exhaustive
import io.getstream.chat.android.ui.messages.adapter.BaseMessageItemViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.DateDividerViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder

internal abstract class BaseDecorator : Decorator {
    final override fun <T : MessageListItem> decorate(
        viewHolder: BaseMessageItemViewHolder<T>,
        data: T,
        isThread: Boolean,
    ) {
        if (data !is MessageListItem.MessageItem) {
            return
        }
        when (viewHolder) {
            is MessageDeletedViewHolder -> decorateDeletedMessage(viewHolder, data, isThread)
            is MessagePlainTextViewHolder -> decoratePlainTextMessage(viewHolder, data, isThread)
            is OnlyMediaAttachmentsViewHolder -> decorateOnlyMediaAttachmentsMessage(viewHolder, data, isThread)
            is PlainTextWithMediaAttachmentsViewHolder -> decoratePlainTextWithMediaAttachmentsMessage(
                viewHolder,
                data,
                isThread
            )
            is OnlyFileAttachmentsViewHolder -> decorateOnlyFileAttachmentsMessage(viewHolder, data, isThread)
            is PlainTextWithFileAttachmentsViewHolder -> decoratePlainTextWithFileAttachmentsMessage(
                viewHolder,
                data,
                isThread
            )
            is GiphyViewHolder -> decorateGiphyMessage(viewHolder, data, isThread)
            is DateDividerViewHolder -> Unit
            else -> Unit
        }.exhaustive
    }

    protected abstract fun decoratePlainTextWithFileAttachmentsMessage(
        viewHolder: PlainTextWithFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean,
    )

    protected abstract fun decorateOnlyFileAttachmentsMessage(
        viewHolder: OnlyFileAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean,
    )

    protected abstract fun decoratePlainTextWithMediaAttachmentsMessage(
        viewHolder: PlainTextWithMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean,
    )

    protected abstract fun decorateOnlyMediaAttachmentsMessage(
        viewHolder: OnlyMediaAttachmentsViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean,
    )

    protected abstract fun decoratePlainTextMessage(
        viewHolder: MessagePlainTextViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean,
    )

    protected open fun decorateDeletedMessage(
        viewHolder: MessageDeletedViewHolder,
        data: MessageListItem.MessageItem,
        isThread: Boolean,
    ) = Unit

    abstract fun decorateGiphyMessage(viewHolder: GiphyViewHolder, data: MessageListItem.MessageItem, isThread: Boolean)
}
