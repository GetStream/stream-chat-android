package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import androidx.core.view.updateLayoutParams
import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import io.getstream.chat.android.ui.transformer.ChatMessageTextTransformer

internal class ImageAttachmentViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val messageTextTransformer: ChatMessageTextTransformer,
    internal val binding: StreamUiItemImageAttachmentBinding = StreamUiItemImageAttachmentBinding.inflate(
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

    init {
        binding.run {
            listeners?.let { container ->
                root.setOnClickListener {
                    container.messageClickListener.onMessageClick(data.message)
                }
                reactionsView.setReactionClickListener {
                    container.reactionViewClickListener.onReactionViewClick(data.message)
                }
                footnote.setOnThreadClickListener {
                    container.threadClickListener.onThreadClick(data.message)
                }
                root.setOnLongClickListener {
                    container.messageLongClickListener.onMessageLongClick(data.message)
                    true
                }
                avatarView.setOnClickListener {
                    container.userClickListener.onUserClick(data.message.user)
                }
                LongClickFriendlyLinkMovementMethod.set(
                    textView = messageText,
                    longClickTarget = root,
                    onLinkClicked = container.linkClickListener::onLinkClick
                )
            }
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        messageTextTransformer.transformAndApply(binding.messageText, data)

        val listeners = modifiedListeners(listeners)
        if (diff?.attachments != false || diff.positions) {
            binding.imageAttachmentView.setPadding(1.dpToPx())
            binding.imageAttachmentView.setupBackground(data)
            binding.imageAttachmentView.attachmentClickListener = AttachmentClickListener {
                listeners?.attachmentClickListener?.onAttachmentClick(data.message, it)
            }
            binding.imageAttachmentView.attachmentLongClickListener = AttachmentLongClickListener {
                listeners?.messageLongClickListener?.onMessageLongClick(data.message)
            }
            binding.imageAttachmentView.showAttachments(data.message.attachments)
        }

        binding.messageContainer.updateLayoutParams<ConstraintLayout.LayoutParams> {
            horizontalBias = if (data.isTheirs) 0f else 1f
        }

        setupUploads(data)
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

    override fun onAttachedToWindow() {
        setupUploads(data)
    }
}
