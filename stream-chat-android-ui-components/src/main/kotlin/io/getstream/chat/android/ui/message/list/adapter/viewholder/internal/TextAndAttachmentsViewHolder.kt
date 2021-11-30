package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import io.getstream.chat.android.ui.message.list.adapter.attachments.AttachmentsAdapter
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewHolderFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

internal class TextAndAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer?,
    private val markdown: ChatMarkdown,
    private val attachmentViewFactory: AttachmentViewFactory?,
    attachmentViewHolderFactory: AttachmentViewHolderFactory?, // TODO make non-nullable when AttachmentViewFactory is removed
    recycledViewPool: RecyclerView.RecycledViewPool,
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
    // TODO do we need the same thing for the new attachment implementation?
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

    // TODO make non-nullable when AttachmentViewFactory is removed
    private val attachmentsAdapter: AttachmentsAdapter? = attachmentViewHolderFactory?.let { factory ->
        AttachmentsAdapter(factory)
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

        // TODO remove this null check when AttachmentViewFactory is removed
        if (attachmentsAdapter != null) {
            binding.attachmentsRecycler.adapter = attachmentsAdapter
            binding.attachmentsRecycler.layoutManager = LinearLayoutManager(context)
            binding.attachmentsRecycler.setRecycledViewPool(recycledViewPool)
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        binding.messageText.isVisible = data.message.text.isNotEmpty()
        markdown.setText(binding.messageText, data.message.text)

        setupAttachment(data)
        setupUploads(data)
    }

    private fun setupAttachment(data: MessageListItem.MessageItem) {
        if (attachmentViewFactory != null) {
            // Use legacy factory if set
            with(binding.attachmentsContainer) {
                removeAllViews()
                addView(attachmentViewFactory.createAttachmentView(data, modifiedListeners(listeners), style, binding.root))
            }
        } else {
            // TODO remove this safe call when AttachmentViewFactory is removed
            attachmentsAdapter?.submitList(listOf(data))
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
