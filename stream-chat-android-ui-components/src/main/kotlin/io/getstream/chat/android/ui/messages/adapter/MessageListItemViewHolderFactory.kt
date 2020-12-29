package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.viewholder.DateDividerViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.ThreadSeparatorViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.DecoratorsProvider

public open class MessageListItemViewHolderFactory(
    private val decoratorsProvider: DecoratorsProvider
) {

    public var listenerContainer: ListenerContainer? = null

    public fun createViewHolder(parentView: ViewGroup, viewType: Int): BaseMessageItemViewHolder<*> {
        return when (MessageListItemViewTypeMapper.viewTypeValueToViewType(viewType)) {
            MessageListItemViewType.DATE_DIVIDER -> createDateDividerViewHolder(parentView)
            MessageListItemViewType.MESSAGE_DELETED -> createMessageDeletedViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT -> createPlainTextViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS -> createPlainTextWithFileAttachmentsViewHolder(parentView)
            MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS -> createPlainTextWithMediaAttachmentsViewHolder(parentView)
            MessageListItemViewType.MEDIA_ATTACHMENTS -> createMediaAttachmentsViewHolder(parentView)
            MessageListItemViewType.ATTACHMENTS -> createAttachmentsViewHolder(parentView)
            MessageListItemViewType.LOADING_INDICATOR -> createLoadingIndicatorViewHolder(parentView)
            MessageListItemViewType.THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
            MessageListItemViewType.TYPING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            MessageListItemViewType.READ_STATE -> createEmptyMessageItemViewHolder(parentView)
            MessageListItemViewType.GIPHY -> createGiphyMessageItemViewHolder(parentView)
        }
    }

    public open fun createDateDividerViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return DateDividerViewHolder(parentView, decoratorsProvider.getDecorators())
    }

    public open fun createMessageDeletedViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return MessageDeletedViewHolder(parentView, decoratorsProvider.getDecorators())
    }

    public open fun createPlainTextViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return MessagePlainTextViewHolder(parentView, decoratorsProvider.getDecorators(), listenerContainer)
    }

    public open fun createPlainTextWithFileAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return PlainTextWithFileAttachmentsViewHolder(parentView, decoratorsProvider.getDecorators(), listenerContainer)
    }

    private fun createPlainTextWithMediaAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return PlainTextWithMediaAttachmentsViewHolder(parentView, decoratorsProvider.getDecorators(), listenerContainer)
    }

    public open fun createMediaAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return OnlyMediaAttachmentsViewHolder(parentView, decoratorsProvider.getDecorators(), listenerContainer)
    }

    public open fun createAttachmentsViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return OnlyFileAttachmentsViewHolder(parentView, decoratorsProvider.getDecorators(), listenerContainer)
    }

    public open fun createLoadingIndicatorViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return createEmptyMessageItemViewHolder(parentView)
    }

    public open fun createThreadSeparatorViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return ThreadSeparatorViewHolder(parentView, decoratorsProvider.getDecorators())
    }

    private fun createGiphyMessageItemViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<*> {
        return GiphyViewHolder(parentView, decoratorsProvider.getDecorators(), listenerContainer)
    }

    private fun createEmptyMessageItemViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<MessageListItem> {
        return object : BaseMessageItemViewHolder<MessageListItem>(View(parentView.context), decoratorsProvider.getDecorators()) {
            override fun bindData(data: MessageListItem, diff: MessageListItemPayloadDiff?) = Unit
        }
    }
}
