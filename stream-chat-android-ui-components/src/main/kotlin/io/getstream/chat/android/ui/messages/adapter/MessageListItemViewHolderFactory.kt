package io.getstream.chat.android.ui.messages.adapter

import android.view.View
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.MEDIA_ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.MESSAGE_DELETED
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.PLAIN_TEXT
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.PLAIN_TEXT_WITH_FILE_ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.messages.adapter.MessageListItemViewType.TYPING_INDICATOR
import io.getstream.chat.android.ui.messages.adapter.viewholder.DateDividerViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.GiphyViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessageDeletedViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.OnlyMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithFileAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.PlainTextWithMediaAttachmentsViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.ThreadSeparatorViewHolder
import io.getstream.chat.android.ui.messages.adapter.viewholder.decorator.DecoratorProvider

public open class MessageListItemViewHolderFactory {

    internal lateinit var decoratorProvider: DecoratorProvider

    protected lateinit var listenerContainer: MessageListListenerContainer
        private set

    internal fun setListenerContainer(listenerContainer: MessageListListenerContainer) {
        this.listenerContainer = listenerContainer
    }

    /**
     * Returns a view type value based on the type and contents of the given [item].
     * The view type returned here will be used as a parameter in [createViewHolder].
     *
     * For built-in view types, see [MessageListItemViewType] and its constants.
     */
    public open fun getItemViewType(item: MessageListItem): Int {
        return MessageListItemViewTypeMapper.getViewTypeValue(item)
    }

    /**
     * Creates a new ViewHolder to be used in the Message List.
     * The [viewType] parameter is determined by [getItemViewType].
     */
    public open fun createViewHolder(
        parentView: ViewGroup,
        viewType: Int,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return when (viewType) {
            DATE_DIVIDER -> createDateDividerViewHolder(parentView)
            MESSAGE_DELETED -> createMessageDeletedViewHolder(parentView)
            PLAIN_TEXT -> createPlainTextViewHolder(parentView)
            PLAIN_TEXT_WITH_FILE_ATTACHMENTS -> createPlainTextWithFileAttachmentsViewHolder(parentView)
            PLAIN_TEXT_WITH_MEDIA_ATTACHMENTS -> createPlainTextWithMediaAttachmentsViewHolder(parentView)
            MEDIA_ATTACHMENTS -> createMediaAttachmentsViewHolder(parentView)
            ATTACHMENTS -> createAttachmentsViewHolder(parentView)
            LOADING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
            TYPING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            GIPHY -> createGiphyMessageItemViewHolder(parentView)
            else -> throw IllegalArgumentException("Unhandled MessageList view type: $viewType")
        }
    }

    protected fun createDateDividerViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.DateSeparatorItem> {
        return DateDividerViewHolder(parentView, decoratorProvider.decorators)
    }

    protected fun createMessageDeletedViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return MessageDeletedViewHolder(parentView, decoratorProvider.decorators)
    }

    protected fun createPlainTextViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return MessagePlainTextViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
        )
    }

    protected fun createPlainTextWithFileAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return PlainTextWithFileAttachmentsViewHolder(parentView, decoratorProvider.decorators, listenerContainer)
    }

    protected fun createPlainTextWithMediaAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return PlainTextWithMediaAttachmentsViewHolder(parentView, decoratorProvider.decorators, listenerContainer)
    }

    protected fun createMediaAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return OnlyMediaAttachmentsViewHolder(parentView, decoratorProvider.decorators, listenerContainer)
    }

    protected fun createAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return OnlyFileAttachmentsViewHolder(parentView, decoratorProvider.decorators, listenerContainer)
    }

    protected fun createThreadSeparatorViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.ThreadSeparatorItem> {
        return ThreadSeparatorViewHolder(parentView, decoratorProvider.decorators)
    }

    protected fun createGiphyMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return GiphyViewHolder(parentView, decoratorProvider.decorators, listenerContainer)
    }

    private fun createEmptyMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem> {
        return object :
            BaseMessageItemViewHolder<MessageListItem>(View(parentView.context)) {
            override fun bindData(data: MessageListItem, diff: MessageListItemPayloadDiff?) = Unit
        }
    }
}
