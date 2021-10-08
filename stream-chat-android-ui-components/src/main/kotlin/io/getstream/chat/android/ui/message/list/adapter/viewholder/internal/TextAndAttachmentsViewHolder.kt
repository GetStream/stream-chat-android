package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.databinding.StreamUiItemTextAndAttachmentsBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class TextAndAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer,
    private val markdown: ChatMarkdown,
    private val attachmentViewFactory: AttachmentViewFactory,
    private val style: MessageListItemStyle,
    internal val binding: StreamUiItemTextAndAttachmentsBinding = StreamUiItemTextAndAttachmentsBinding.inflate(
        parent.streamThemeInflater,
        parent,
        false
    ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    private var scope: CoroutineScope? = null

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
    private val modifiedListeners = MessageListListenerContainerImpl(
        messageClickListener = { listeners.messageClickListener.onMessageClick(data.message) },
        messageLongClickListener = { listeners.messageLongClickListener.onMessageLongClick(data.message) },
        messageRetryListener = { listeners.messageRetryListener.onRetryMessage(data.message) },
        threadClickListener = { listeners.threadClickListener.onThreadClick(data.message) },
        attachmentClickListener = { _, attachment ->
            listeners.attachmentClickListener.onAttachmentClick(data.message, attachment)
        },
        attachmentDownloadClickListener = listeners.attachmentDownloadClickListener::onAttachmentDownloadClick,
        reactionViewClickListener = { listeners.reactionViewClickListener.onReactionViewClick(data.message) },
        userClickListener = { listeners.userClickListener.onUserClick(data.message.user) },
        giphySendListener = { _, action ->
            listeners.giphySendListener.onGiphySend(data.message, action)
        },
        linkClickListener = listeners.linkClickListener::onLinkClick
    )

    init {
        binding.run {
            root.setOnClickListener {
                listeners.messageClickListener.onMessageClick(data.message)
            }
            reactionsView.setReactionClickListener {
                listeners.reactionViewClickListener.onReactionViewClick(data.message)
            }
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }
            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            avatarView.setOnClickListener {
                listeners.userClickListener.onUserClick(data.message.user)
            }
            LongClickFriendlyLinkMovementMethod.set(
                textView = messageText,
                longClickTarget = root,
                onLinkClicked = listeners.linkClickListener::onLinkClick
            )
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        markdown.setText(binding.messageText, data.message.text)

        if (diff?.attachments != false) {
            setupAttachment(data)
        }

        setupUploads(data)
    }

    private fun setupAttachment(data: MessageListItem.MessageItem) {
        with(binding.attachmentsContainer) {
            removeAllViews()
            addView(attachmentViewFactory.createAttachmentView(data, modifiedListeners, style, binding.root))
        }
    }

    private fun clearScope() {
        scope?.cancel()
        scope = null
    }

    override fun unbind() {
        clearScope()
        super.unbind()
    }

    private fun setupUploads(data: MessageListItem.MessageItem) {
        val totalAttachmentsCount = data.message.attachments.size
        val completedAttachmentsCount =
            data.message.attachments.count { it.uploadState == null || it.uploadState == Attachment.UploadState.Success }
        if (completedAttachmentsCount == totalAttachmentsCount) {
            binding.sentFiles.isVisible = false
        } else {
            binding.sentFiles.text =
                context.getString(
                    R.string.stream_ui_message_list_attachment_uploading,
                    completedAttachmentsCount,
                    totalAttachmentsCount
                )
        }
    }

    override fun onDetachedFromWindow() {
        clearScope()
    }

    override fun onAttachedToWindow() {
        setupUploads(data)
    }
}
