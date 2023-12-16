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

import android.view.View
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
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.SYSTEM_MESSAGE
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_PLACEHOLDER
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.THREAD_SEPARATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.MessageListItemViewType.TYPING_INDICATOR
import io.getstream.chat.android.ui.feature.messages.list.adapter.internal.MessageListItemViewTypeMapper
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.internal.DecoratorProvider
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.CustomAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.DateDividerViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.ErrorMessageViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.FileAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.GiphyAttachmentViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.GiphyViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.LinkAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.LoadingMoreViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.MediaAttachmentsViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.MessageDeletedViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.MessagePlainTextViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.SystemMessageViewHolder
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.internal.ThreadSeparatorViewHolder
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
    protected var listenerContainer: MessageListListenerContainer? = null
        private set

    /**
     * Setter for [listenerContainer].
     */
    internal fun setListenerContainer(listenerContainer: MessageListListenerContainer?) {
        this.listenerContainer = listenerContainer
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
    public open fun getItemViewType(item: MessageListItem): Int {
        return MessageListItemViewTypeMapper.getViewTypeValue(item, attachmentFactoryManager)
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
            CUSTOM_ATTACHMENTS -> createCustomAttachmentsViewHolder(parentView)
            LOADING_INDICATOR -> createLoadingMoreViewHolder(parentView)
            THREAD_SEPARATOR -> createThreadSeparatorViewHolder(parentView)
            TYPING_INDICATOR -> createEmptyMessageItemViewHolder(parentView)
            GIPHY -> createGiphyMessageItemViewHolder(parentView)
            SYSTEM_MESSAGE -> createSystemMessageItemViewHolder(parentView)
            ERROR_MESSAGE -> createErrorMessageItemViewHolder(parentView)
            THREAD_PLACEHOLDER -> createEmptyMessageItemViewHolder(parentView)
            LINK_ATTACHMENTS -> createLinkAttachmentsViewHolder(parentView)
            GIPHY_ATTACHMENT -> createGiphyAttachmentViewHolder(parentView)
            FILE_ATTACHMENTS -> createFileAttachmentsViewHolder(parentView)
            MEDIA_ATTACHMENT -> createMediaAttachmentsViewHolder(parentView)
            else -> throw IllegalArgumentException("Unhandled MessageList view type: $viewType")
        }
    }

    /**
     * Creates the custom attachments view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that displays messages with custom attachments.
     */
    private fun createCustomAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return CustomAttachmentsViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            textTransformer,
            attachmentFactoryManager,
        )
    }

    /**
     * Creates the Giphy view holder, that holds various a Giphy image.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that holds a Giphy image in various quality types.
     */
    private fun createGiphyAttachmentViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return GiphyAttachmentViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            markdown = textTransformer,
        )
    }

    /**
     * Creates a ViewHolder for messages containing image and/or video attachments and no other type
     * of attachments.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that displays messages with image and/or video attachments.
     */
    private fun createMediaAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<out MessageListItem> {
        return MediaAttachmentsViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            textTransformer,
            audioRecordViewStyle,
        )
    }

    /**
     * Creates a date divider view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the date divider.
     */
    private fun createDateDividerViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.DateSeparatorItem> {
        return DateDividerViewHolder(parentView, decoratorProvider.decorators, style)
    }

    /**
     * Creates a loading more view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows information about the deleted message.
     */
    private fun createLoadingMoreViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.LoadingMoreIndicatorItem> {
        return LoadingMoreViewHolder(parentView, style)
    }

    /**
     * Creates the deleted message view holder, that's visible only to the user that deleted the message.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows information about the deleted message.
     */
    private fun createMessageDeletedViewHolder(
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
    private fun createPlainTextViewHolder(
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
    private fun createThreadSeparatorViewHolder(
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
    private fun createGiphyMessageItemViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return GiphyViewHolder(
            parentView,
            decoratorProvider.decorators,
            listenerContainer,
            giphyViewHolderStyle,
        )
    }

    /**
     * Creates the system message view holder.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that shows the system message.
     */
    private fun createSystemMessageItemViewHolder(
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
    private fun createErrorMessageItemViewHolder(
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
     * Creates a ViewHolder for messages containing file attachments.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that represents the message with file attachments.
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

    /**
     * Creates a ViewHolder for messages containing link attachments and no other type
     * of attachments.
     *
     * @param parentView The parent container.
     * @return The [BaseMessageItemViewHolder] that displays messages with link attachments.
     */
    private fun createLinkAttachmentsViewHolder(
        parentView: ViewGroup,
    ): BaseMessageItemViewHolder<MessageListItem.MessageItem> {
        return LinkAttachmentsViewHolder(
            parent = parentView,
            decorators = decoratorProvider.decorators,
            listeners = listenerContainer,
            style = style,
            messageTextTransformer = textTransformer,
        )
    }
}
