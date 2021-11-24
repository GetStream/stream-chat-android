package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.isBottomPosition
import io.getstream.chat.android.ui.ChatUI.markdown
import io.getstream.chat.android.ui.common.extensions.hasText
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.extensions.isReply
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.databinding.StreamUiItemGiphyAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.BackgroundDecorator
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator

internal class GiphyAttachmentViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val markdown: ChatMarkdown,
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

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        markdown.setText(binding.messageText, data.message.text)

        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = if (data.isTheirs) 0f else 1f
        }

        imageCorners(binding.mediaAttachmentView, data)

        modifiedListeners(listeners)?.let { listeners ->
            setListeners(binding.mediaAttachmentView, listeners, data)
        }

        binding.mediaAttachmentView.showGiphy(
            data.message.attachments.first(),
            containerView = binding.messageContainer
        )
    }

    private fun setListeners(
        mediaAttachmentView: MediaAttachmentView,
        listeners: MessageListListenerContainer,
        data: MessageListItem.MessageItem,
    ) {
        mediaAttachmentView.setOnLongClickListener {
            listeners.messageLongClickListener.onMessageLongClick(data.message)
            true
        }
    }

    private fun imageCorners(mediaAttachmentView: MediaAttachmentView, data: MessageListItem.MessageItem) {
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
