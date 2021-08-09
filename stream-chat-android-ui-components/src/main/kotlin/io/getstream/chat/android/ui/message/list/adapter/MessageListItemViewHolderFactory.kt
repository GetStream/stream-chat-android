package io.getstream.chat.android.ui.message.list.adapter

import android.view.View
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.message.list.GiphyViewHolderStyle
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.ERROR_MESSAGE
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.MESSAGE_DELETED
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.PLAIN_TEXT
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.SYSTEM_MESSAGE
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.TEXT_AND_ATTACHMENTS
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.THREAD_PLACEHOLDER
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.TYPING_INDICATOR
import io.getstream.chat.android.ui.message.list.adapter.internal.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.DateDividerViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ErrorMessageViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.SystemMessageViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ThreadSeparatorViewHolder

public open class MessageListItemViewHolderFactory {
    internal lateinit var decoratorProvider: DecoratorProvider

    protected lateinit var listenerContainer: MessageListListenerContainer
        private set

    protected lateinit var attachmentViewFactory: AttachmentViewFactory
        private set

    private lateinit var style: MessageListItemStyle

    private lateinit var messageReplyStyle: MessageReplyStyle

    private lateinit var giphyViewHolderStyle: GiphyViewHolderStyle

    internal fun setListenerContainer(listenerContainer: MessageListListenerContainer) {
        this.listenerContainer = listenerContainer
    }

    internal fun setAttachmentViewFactory(attachmentViewFactory: AttachmentViewFactory) {
        this.attachmentViewFactory = attachmentViewFactory
    }

    internal fun setMessageListItemStyle(style: MessageListItemStyle) {
        this.style = style
    }

    internal fun setReplyMessageListItemViewStyle(style: MessageReplyStyle) {
        this.messageReplyStyle = style
    }

    internal fun setGiphyViewHolderStyle(style: GiphyViewHolderStyle) {
        this.giphyViewHolderStyle = style
    }

    private val markdown: ChatMarkdown by lazy { ChatUI.markdown }

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
            TEXT_AND_ATTACHMENTS -> createTextAndAttachmentViewHolder(parentView)
            LOADING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
            TYPING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            GIPHY -> createGiphyMessageItemViewHolder(parentView)
            SYSTEM_MESSAGE -> createSystemMessageItemViewHolder(parentView)
            ERROR_MESSAGE -> createErrorMessageItemViewHolder(parentView)
            THREAD_PLACEHOLDER -> createEmptyMessageItemViewHolder(parentView)
            else -> throw IllegalArgumentException("Unhandled MessageList view type: $viewType")
        }
    }

    protected fun createTextAndAttachmentViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<out MessageListItem> {
        return TextAndAttachmentsViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            markdown,
            attachmentViewFactory,
            style
        )
    }

    protected fun createDateDividerViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.DateSeparatorItem> {
        return DateDividerViewHolder(parentView, decoratorProvider.decorators, style)
    }

    protected fun createMessageDeletedViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return MessageDeletedViewHolder(parentView, decoratorProvider.decorators, style)
    }

    protected fun createPlainTextViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return MessagePlainTextViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            markdown,
        )
    }

    protected fun createThreadSeparatorViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.ThreadSeparatorItem> {
        return ThreadSeparatorViewHolder(parentView, decoratorProvider.decorators, style)
    }

    protected fun createGiphyMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return GiphyViewHolder(parentView, decoratorProvider.decorators, listenerContainer, giphyViewHolderStyle)
    }

    protected fun createSystemMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return SystemMessageViewHolder(parentView, style)
    }

    protected fun createErrorMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return ErrorMessageViewHolder(parentView, style)
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
