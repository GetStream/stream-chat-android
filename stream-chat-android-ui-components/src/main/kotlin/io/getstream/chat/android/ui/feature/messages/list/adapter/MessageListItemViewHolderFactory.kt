/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ui.feature.messages.list.adapter

import android.view.ViewGroup
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.messages.common.AudioRecordPlayerViewStyle
import io.getstream.chat.android.ui.feature.messages.list.GiphyViewHolderStyle
import io.getstream.chat.android.ui.feature.messages.list.MessageListItemStyle
import io.getstream.chat.android.ui.feature.messages.list.MessageReplyStyle
import io.getstream.chat.android.ui.feature.messages.list.MessageViewStyle
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.CUSTOM_ATTACHMENTS
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.DATE_DIVIDER
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.ERROR_MESSAGE
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.FILE_ATTACHMENTS
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.GIPHY
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.GIPHY_ATTACHMENT
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.LINK_ATTACHMENTS
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.LOADING_INDICATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.MEDIA_ATTACHMENT
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.MESSAGE_DELETED
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.PLAIN_TEXT
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.POLL
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.START_OF_THE_CHANNEL
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.SYSTEM_MESSAGE
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_PLACEHOLDER
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.TYPING_INDICATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.UNREAD_SEPARATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemAdapter
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.DecoratorProvider
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.DateDividerViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.GiphyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MediaAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessageDeletedViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.impl.PollViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.EmptyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.ErrorMessageViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.LoadingMoreViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.SystemMessageViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.ThreadSeparatorViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.UnreadSeparatorViewHolder
import io.getstream.chat.android.ui.helper.transformer.ChatMessageTextTransformer

/**
 * A factory class designed to create ViewHolders for the RecyclerView
 * inside [io.getstream.chat.android.ui.feature.messages.list.MessageListView].
 */
public open class MessageListItemViewHolderFactory {

    /**
     * Provides a list of decorators to be used by the various ViewHolders.
     */
    internal lateinit var decoratorProvider: DecoratorProvider

    /**
     * A manager for the registered custom attachment factories.
     */
    protected lateinit var attachmentFactoryManager: AttachmentFactoryManager
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
     * Sets the style for the Media ViewHolder.
     */
    private lateinit var audioRecordViewStyle: MessageViewStyle<AudioRecordPlayerViewStyle>

    /**
     * A container containing listeners used by the ViewHolders for
     * setting reactions, opening message options, etc.
     */
    @Deprecated(
        message = "Use MessageListListeners instead",
        replaceWith = ReplaceWith("MessageListListeners"),
        level = DeprecationLevel.WARNING,
    )
    protected var listenerContainer: MessageListListenerContainer? = null
        private set

    /**
     * A container containing listeners used by the ViewHolders for
     * setting reactions, opening message options, etc.
     */
    protected var listeners: MessageListListeners? = null
        private set

    /**
     * Setter for [listeners].
     */
    internal fun setListeners(listeners: MessageListListeners?) {
        this.listeners = listeners
        this.listenerContainer = listeners?.let { MessageListListenersAdapter(it) }
    }

    /**
     * Setter for [attachmentFactoryManager].
     */
    internal fun setAttachmentFactoryManager(attachmentFactoryManager: AttachmentFactoryManager) {
        this.attachmentFactoryManager = attachmentFactoryManager
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
     * Setter for [audioRecordViewStyle].
     */
    internal fun setAudioRecordViewStyle(style: MessageViewStyle<AudioRecordPlayerViewStyle>) {
        this.audioRecordViewStyle = style
    }

    /**
     * Transforms the message text.
     */
    private val textTransformer: ChatMessageTextTransformer by lazy { ChatUI.messageTextTransformer }

    /**
     * Returns a view type value based on the type and contents of the given [item].
     * The view type returned here will be used as a parameter in [createViewHolder].
     *
     * For built-in view types, see [MessageListItemViewType] and its constants.
     */
    public open fun getItemViewType(item: MessageListItem): Int = MessageListItemViewTypeMapper.getViewTypeValue(item, attachmentFactoryManager)

    /**
     * Returns a view type value based on the type of the given [viewHolder].
     * The view type returned here will be used in [MessageListItemAdapter.onBindViewHolder]
     * to check if the ViewHolder is of the correct type before binding the item.
     *
     * For built-in view types, see [MessageListItemViewType] and its constants.
     */
    public open fun getItemViewType(viewHolder: BaseMessageItemViewHolder<out MessageListItem>): Int = when (viewHolder) {
        is DateDividerViewHolder -> DATE_DIVIDER
        is MessageDeletedViewHolder -> MESSAGE_DELETED
        is MessagePlainTextViewHolder -> PLAIN_TEXT
        is CustomAttachmentsViewHolder -> CUSTOM_ATTACHMENTS
        is LoadingMoreViewHolder -> LOADING_INDICATOR
        is ThreadSeparatorViewHolder -> THREAD_SEPARATOR
        is GiphyViewHolder -> GIPHY
        is SystemMessageViewHolder -> SYSTEM_MESSAGE
        is ErrorMessageViewHolder -> ERROR_MESSAGE
        is EmptyViewHolder -> viewHolder.viewType
        is LinkAttachmentsViewHolder -> LINK_ATTACHMENTS
        is GiphyAttachmentViewHolder -> GIPHY_ATTACHMENT
        is FileAttachmentsViewHolder -> FILE_ATTACHMENTS
        is MediaAttachmentsViewHolder -> MEDIA_ATTACHMENT
        is UnreadSeparatorViewHolder -> UNREAD_SEPARATOR
        is PollViewHolder -> POLL
        else -> throw IllegalArgumentException("Unhandled MessageList view holder: $viewHolder")
    }

    /**
     * Creates a new ViewHolder to be used in the Message List.
     * The [viewType] parameter is determined by [getItemViewType].
     */
    public open fun createViewHolder(
        parentView: ViewGroup,
        viewType: Int,
    ): BaseMessageItemViewHolder<out MessageListItem> = when (viewType) {
        DATE_DIVIDER -> createDateDividerViewHolder(parentView)
        MESSAGE_DELETED -> createMessageDeletedViewHolder(parentView)
        PLAIN_TEXT -> createPlainTextViewHolder(parentView)
        CUSTOM_ATTACHMENTS -> createCustomAttachmentsViewHolder(parentView)
        LOADING_INDICATOR -> createLoadingMoreViewHolder(parentView)
        THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
        TYPING_INDICATOR -> createEmptyMessageItemViewHolder(parentView, viewType)
        GIPHY -> createGiphyMessageItemViewHolder(parentView)
        SYSTEM_MESSAGE -> createSystemMessageItemViewHolder(parentView)
        ERROR_MESSAGE -> createErrorMessageItemViewHolder(parentView)
        THREAD_PLACEHOLDER -> createEmptyMessageItemViewHolder(parentView, viewType)
        LINK_ATTACHMENTS -> createLinkAttachmentsViewHolder(parentView)
        GIPHY_ATTACHMENT -> createGiphyAttachmentViewHolder(parentView)
        FILE_ATTACHMENTS -> createFileAttachmentsViewHolder(parentView)
        MEDIA_ATTACHMENT -> createMediaAttachmentsViewHolder(parentView)
        UNREAD_SEPARATOR -> createUnreadSeparatorViewHolder(parentView)
        START_OF_THE_CHANNEL -> createEmptyMessageItemViewHolder(parentView, viewType)
        POLL -> createPollItemViewHolder(parentView)
        else -> throw IllegalArgumentException("Unhandled MessageList view type: $viewType")
    }

    /**
     * Creates the custom attachments view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that displays messages with custom attachments.
     */
    private fun createCustomAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> = CustomAttachmentsViewHolder(
        parentView,
        decoratorProvider.decorators,
        listeners,
        textTransformer,
        attachmentFactoryManager,
    )

    /**
     * Creates the Giphy view holder, that holds various a Giphy image.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that holds a Giphy image in various quality types.
     */
    private fun createGiphyAttachmentViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> = GiphyAttachmentViewHolder(
        parentView,
        decoratorProvider.decorators,
        listeners,
        markdown = textTransformer,
    )

    /**
     * Creates a ViewHolder for messages containing image and/or video attachments and no other type
     * of attachments.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that displays messages with image and/or video attachments.
     */
    private fun createMediaAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> = MediaAttachmentsViewHolder(
        parentView,
        decoratorProvider.decorators,
        listeners,
        textTransformer,
        audioRecordViewStyle,
    )

    private fun createPollItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = PollViewHolder(
        parentView,
        decoratorProvider.decorators,
        listeners,
        style,
    )
    private fun createUnreadSeparatorViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.UnreadSeparatorItem> = UnreadSeparatorViewHolder(
        parentView,
        decoratorProvider.decorators,
        listeners,
        style,
    )

    /**
     * Creates a date divider view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the date divider.
     */
    private fun createDateDividerViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.DateSeparatorItem> = DateDividerViewHolder(parentView, decoratorProvider.decorators, style)

    /**
     * Creates a loading more view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows information about the deleted message.
     */
    private fun createLoadingMoreViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.LoadingMoreIndicatorItem> = LoadingMoreViewHolder(parentView, style)

    /**
     * Creates the deleted message view holder, that's visible only to the user that deleted the message.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows information about the deleted message.
     */
    private fun createMessageDeletedViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = MessageDeletedViewHolder(parentView, decoratorProvider.decorators, style)

    /**
     * Creates a text  view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that holds just text.
     */
    private fun createPlainTextViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = MessagePlainTextViewHolder(
        parentView,
        decoratorProvider.decorators,
        listeners,
        textTransformer,
    )

    /**
     * Creates a thread separator view holder when in a Thread.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the thread separator.
     */
    private fun createThreadSeparatorViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.ThreadSeparatorItem> = ThreadSeparatorViewHolder(parentView, decoratorProvider.decorators, style)

    /**
     * Creates the Giphy message view holder, that holds a Giphy that hasn't been sent yet and can be shuffled and canceled.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that can holds the Giphy preview and options.
     */
    private fun createGiphyMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = GiphyViewHolder(
        parentView,
        decoratorProvider.decorators,
        listeners,
        giphyViewHolderStyle,
    )

    /**
     * Creates the system message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows the system message.
     */
    private fun createSystemMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = SystemMessageViewHolder(parentView, style)

    /**
     * Creates the error message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows the error.
     */
    private fun createErrorMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = ErrorMessageViewHolder(parentView, style)

    /**
     * Creates the empty message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the empty message.
     */
    private fun createEmptyMessageItemViewHolder(
        parentView: ViewGroup,
        viewType: Int,
    ): BaseMessageItemViewHolder<MessageListItem> = EmptyViewHolder(parentView, viewType)

    /**
     * Creates a ViewHolder for messages containing file attachments.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the message with file attachments.
     */
    private fun createFileAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = FileAttachmentsViewHolder(
        parent = parentView,
        decorators = decoratorProvider.decorators,
        listeners = listeners,
        messageTextTransformer = textTransformer,
    )

    /**
     * Creates a ViewHolder for messages containing link attachments and no other type
     * of attachments.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that displays messages with link attachments.
     */
    private fun createLinkAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> = LinkAttachmentsViewHolder(
        parent = parentView,
        decorators = decoratorProvider.decorators,
        listeners = listeners,
        style = style,
        messageTextTransformer = textTransformer,
    )
}
