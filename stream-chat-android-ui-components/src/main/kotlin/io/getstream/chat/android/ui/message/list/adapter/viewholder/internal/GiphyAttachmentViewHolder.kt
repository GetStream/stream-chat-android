package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.constrainViewEndToEndOfView
import com.getstream.sdk.chat.utils.extensions.constrainViewStartToEndOfView
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import com.getstream.sdk.chat.utils.extensions.updateConstraints
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.extensions.hasText
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.isReply
import io.getstream.chat.android.ui.databinding.StreamUiItemGiphyAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.GiphyMediaAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer

/**
 * Represents the Giphy attachment holder, when the Giphy is already sent.
 *
 * @param parent The parent container.
 * @param decorators List of decorators for various parts of the holder.
 * @param listeners The listeners for various user interactions.
 * @param markdown Markdown renderer, used for the message text.
 * @param binding The binding that holds all the View references.
 */
internal class GiphyAttachmentViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val markdown: ChatMessageTextTransformer,
    internal val binding: StreamUiItemGiphyAttachmentBinding = StreamUiItemGiphyAttachmentBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    /**
     * We override the Message passed to listeners here with the up-to-date Message
     * object from the [data] property of the base ViewHolder.
     *
     * This is required because these listeners will be invoked by the AttachmentViews,
     * which don't always have an up-to-date Message object in them. This is due to the
     * optimization that we don't re-create the AttachmentViews when the attachments
     * of the Message are unchanged. However, other properties (like reactions) might
     * change, and these listeners should receive a fully up-to-date Message.
     */
    private fun modifiedListeners(listeners: MessageListListenerContainer?): MessageListListenerContainer? {
        return listeners?.let { container ->
            MessageListListenerContainerImpl(
                messageClickListener = { container.messageClickListener.onMessageClick(data.message) },
                messageLongClickListener = { container.messageLongClickListener.onMessageLongClick(data.message) },
                messageRetryListener = { container.messageRetryListener.onRetryMessage(data.message) },
                threadClickListener = { container.threadClickListener.onThreadClick(data.message) },
                attachmentClickListener = { _, attachment ->
                    container.attachmentClickListener.onAttachmentClick(data.message, attachment)
                },
                attachmentDownloadClickListener = container.attachmentDownloadClickListener::onAttachmentDownloadClick,
                reactionViewClickListener = { container.reactionViewClickListener.onReactionViewClick(data.message) },
                userClickListener = { container.userClickListener.onUserClick(data.message.user) },
                giphySendListener = { _, action ->
                    container.giphySendListener.onGiphySend(data.message, action)
                },
                linkClickListener = container.linkClickListener::onLinkClick
            )
        }
    }

    /**
     * Binds the data required to represent a Giphy attachment. Shows the Giphy, applies decorations and the transformations
     * for text and the container shape.
     *
     * Loads the Giphy once things are set up.
     *
     * @param data The data that holds all the information required to show a Giphy.
     * @param diff The difference from the previous draw.
     */
    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        markdown.transformAndApply(binding.messageText, data)

        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = if (data.isTheirs) 0f else 1f
        }

        imageCorners(binding.mediaAttachmentView, data)

        modifiedListeners(listeners)?.let { listeners ->
            setListeners(binding.mediaAttachmentView, listeners, data)
        }

        val attachment = data.message.attachments.first()
        binding.mediaAttachmentView.showGiphy(attachment = attachment)
    }

    /**
     * Sets the listeners that are valid for the Giphy container.
     *
     * @param mediaAttachmentView The Giphy image container.
     * @param listeners The set of listeners available.
     * @param data The data used to propagate events through the listeners.
     */
    private fun setListeners(
        mediaAttachmentView: GiphyMediaAttachmentView,
        listeners: MessageListListenerContainer,
        data: MessageListItem.MessageItem,
    ) {
        mediaAttachmentView.setOnLongClickListener {
            listeners.messageLongClickListener.onMessageLongClick(data.message)
            true
        }

        mediaAttachmentView.setOnClickListener {
            listeners.attachmentClickListener.onAttachmentClick(data.message, data.message.attachments.first())
        }
    }

    /**
     * Decorates the image corners of the [GiphyMediaAttachmentView].
     *
     * @param mediaAttachmentView The View to decorate the corners of.
     * @param data The data that holds all the information about the Giphy message.
     */
    private fun imageCorners(mediaAttachmentView: GiphyMediaAttachmentView, data: MessageListItem.MessageItem) {
        val topLeftCorner = if (data.message.isReply()) 0f else BackgroundDecorator.DEFAULT_CORNER_RADIUS
        val topRightCorner = if (data.message.isReply()) 0f else BackgroundDecorator.DEFAULT_CORNER_RADIUS
        val bottomRightCorner = if (data.message.hasText() || (data.isMine && data.isBottomPosition())) {
            0f
        } else {
            BackgroundDecorator.DEFAULT_CORNER_RADIUS
        }
        val bottomLeftCorner = if (data.message.hasText() || (data.isTheirs && data.isBottomPosition())) {
            0f
        } else {
            BackgroundDecorator.DEFAULT_CORNER_RADIUS
        }

        mediaAttachmentView.setImageShapeByCorners(topLeftCorner, topRightCorner, bottomRightCorner, bottomLeftCorner)
    }
}
