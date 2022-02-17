package io.getstream.chat.android.ui.message.list.adapter

import android.view.View
import android.view.ViewGroup
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.message.list.GiphyViewHolderStyle
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageReplyStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.ERROR_MESSAGE
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.FILE_ATTACHMENTS
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.GIPHY_ATTACHMENT
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemViewType.IMAGE_ATTACHMENT
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
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ImageAttachmentViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.SystemMessageViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.TextAndAttachmentsViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.internal.ThreadSeparatorViewHolder
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer

/**
 * A factory class designed to create ViewHolders for the RecyclerView
 * inside [io.getstream.chat.android.ui.message.list.MessageListView].
 */
public open class MessageListItemViewHolderFactory {

    /**
     * Provides a list of decorators to be used by the various ViewHolders.
     */
    internal lateinit var decoratorProvider: DecoratorProvider

    protected lateinit var attachmentViewFactory: AttachmentViewFactory
        private set

    /**
     * Style applied to the ViewHolders created by this class.
     */
    private lateinit var style: MessageListItemStyle

    /**
     * Sets the style for messages containing replies.
     */
    private lateinit var messageReplyStyle: MessageReplyStyle

    /**
     * Sets the style for the Giphy ViewHolder.
     */
    private lateinit var giphyViewHolderStyle: GiphyViewHolderStyle

    /**
     * A container containing listeners used by the ViewHolders for
     * setting reactions, opening message options, etc.
     */
    protected var listenerContainer: MessageListListenerContainer? = null
        private set

    /**
     * Setter for [listenerContainer].
     */
    internal fun setListenerContainer(listenerContainer: MessageListListenerContainer?) {
        this.listenerContainer = listenerContainer
    }

    /**
     * Setter for [attachmentViewFactory].
     */
    internal fun setAttachmentViewFactory(attachmentViewFactory: AttachmentViewFactory) {
        this.attachmentViewFactory = attachmentViewFactory
    }

    /**
     * Setter for [style].
     */
    internal fun setMessageListItemStyle(style: MessageListItemStyle) {
        this.style = style
    }

    /**
     * Setter for [messageReplyStyle].
     */
    internal fun setReplyMessageListItemViewStyle(style: MessageReplyStyle) {
        this.messageReplyStyle = style
    }

    /**
     * Setter for [giphyViewHolderStyle].
     */
    internal fun setGiphyViewHolderStyle(style: GiphyViewHolderStyle) {
        this.giphyViewHolderStyle = style
    }

    /**
     * Transforms the message text.
     */
    private val textTransformer: ChatMessageTextTransformer by lazy { ChatUI.messageTextTransformer }

    /**
     * This is necessary to maintain the configuration of this factory, but without sharing the instance. Please use
     * clone when sharing the factory between MessageListView and MessageOptionsDialogFragment
     */
    internal fun clone(): MessageListItemViewHolderFactory {
        val newFactory = MessageListItemViewHolderFactory()

        if (::decoratorProvider.isInitialized) {
            newFactory.decoratorProvider = decoratorProvider
        }
        if (::attachmentViewFactory.isInitialized) {
            newFactory.attachmentViewFactory = attachmentViewFactory
        }
        if (::style.isInitialized) {
            newFactory.style = style
        }
        if (::messageReplyStyle.isInitialized) {
            newFactory.messageReplyStyle = messageReplyStyle
        }
        if (::giphyViewHolderStyle.isInitialized) {
            newFactory.giphyViewHolderStyle = giphyViewHolderStyle
        }

        newFactory.listenerContainer = listenerContainer

        return newFactory
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
            TEXT_AND_ATTACHMENTS -> createTextAndAttachmentViewHolder(parentView)
            LOADING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
            TYPING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            GIPHY -> createGiphyMessageItemViewHolder(parentView)
            SYSTEM_MESSAGE -> createSystemMessageItemViewHolder(parentView)
            ERROR_MESSAGE -> createErrorMessageItemViewHolder(parentView)
            THREAD_PLACEHOLDER -> createEmptyMessageItemViewHolder(parentView)
            GIPHY_ATTACHMENT -> createGiphyAttachmentViewHolder(parentView)
            FILE_ATTACHMENTS -> createFileAttachmentsViewHolder(parentView)
            IMAGE_ATTACHMENT -> createImageAttachmentViewHolder(parentView)
            else -> throw IllegalArgumentException("Unhandled MessageList view type: $viewType")
        }
    }

    /**
     * Creates the text and attachment view holder, that holds various types of attachments and the text at the bottom.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that can hold attachments and text.
     */
    protected fun createTextAndAttachmentViewHolder(parentView: ViewGroup): BaseMessageItemViewHolder<out MessageListItem> {
        return TextAndAttachmentsViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            textTransformer,
            attachmentViewFactory,
            style
        )
    }

    /**
     * Creates the Giphy view holder, that holds various a Giphy image.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that holds a Giphy image in various quality types.
     */
    protected fun createGiphyAttachmentViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return GiphyAttachmentViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            markdown = textTransformer
        )
    }

    /**
     * Creates a ViewHolder for messages containing image attachments and no other type
     * of attachments.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that displays messages with image attachments.
     */
    protected fun createImageAttachmentViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return ImageAttachmentViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            textTransformer,
        )
    }

    /**
     * Creates a date divider view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the date divider.
     */
    protected fun createDateDividerViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.DateSeparatorItem> {
        return DateDividerViewHolder(parentView, decoratorProvider.decorators, style)
    }

    /**
     * Creates the deleted message view holder, that's visible only to the user that deleted the message.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows information about the deleted message.
     */
    protected fun createMessageDeletedViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return MessageDeletedViewHolder(parentView, decoratorProvider.decorators, style)
    }

    /**
     * Creates a text  view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that holds just text.
     */
    protected fun createPlainTextViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return MessagePlainTextViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            textTransformer,
        )
    }

    /**
     * Creates a thread separator view holder when in a Thread.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the thread separator.
     */
    protected fun createThreadSeparatorViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.ThreadSeparatorItem> {
        return ThreadSeparatorViewHolder(parentView, decoratorProvider.decorators, style)
    }

    /**
     * Creates the Giphy message view holder, that holds a Giphy that hasn't been sent yet and can be shuffled and canceled.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that can holds the Giphy preview and options.
     */
    protected fun createGiphyMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return GiphyViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            giphyViewHolderStyle
        )
    }

    /**
     * Creates the system message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows the system message.
     */
    protected fun createSystemMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return SystemMessageViewHolder(parentView, style)
    }

    /**
     * Creates the error message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows the error.
     */
    protected fun createErrorMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return ErrorMessageViewHolder(parentView, style)
    }

    /**
     * Creates the empty message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the empty message.
     */
    private fun createEmptyMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem> {
        return object :
            BaseMessageItemViewHolder<MessageListItem>(View(parentView.context)) {
            override fun bindData(data: MessageListItem, diff: MessageListItemPayloadDiff?) = Unit
        }
    }

    /**
     * Creates the empty message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the empty message.
     */
    private fun createFileAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return FileAttachmentsViewHolder(
            parent = parentView,
            decorators = decoratorProvider.decorators,
            listeners = listenerContainer,
            messageTextTransformer = textTransformer,
        )
    }
}
