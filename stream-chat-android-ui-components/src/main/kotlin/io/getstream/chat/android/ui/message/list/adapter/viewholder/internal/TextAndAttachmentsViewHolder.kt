package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.ListenerDelegate
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.R
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.common.markdown.ChatMarkdown
import io.getstream.chat.android.ui.databinding.StreamUiItemTextAndAttachmentsBinding
import io.getstream.chat.android.ui.message.list.MessageListItemStyle
import io.getstream.chat.android.ui.message.list.MessageListView
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainerImpl
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FileAttachmentsView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView
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

    private val messageClickListener: MessageListView.MessageClickListener by ListenerDelegate(
        listeners.messageClickListener
    ) { realListener ->
        MessageListView.MessageClickListener(realListener()::onMessageClick)
    }

    private val messageLongClickListener: MessageListView.MessageLongClickListener by ListenerDelegate(
        listeners.messageLongClickListener
    ) { realListener ->
        MessageListView.MessageLongClickListener(realListener()::onMessageLongClick)
    }

    private val messageRetryListener: MessageListView.MessageRetryListener by ListenerDelegate(
        listeners.messageRetryListener
    ) { realListener ->
        MessageListView.MessageRetryListener(realListener()::onRetryMessage)
    }

    private val threadClickListener: MessageListView.ThreadClickListener by ListenerDelegate(
        listeners.threadClickListener
    ) { realListener ->
        MessageListView.ThreadClickListener(realListener()::onThreadClick)
    }

    private val attachmentClickListener: MessageListView.AttachmentClickListener by ListenerDelegate(
        listeners.attachmentClickListener
    ) { realListener ->
        MessageListView.AttachmentClickListener(realListener()::onAttachmentClick)
    }

    private val attachmentDownloadClickListener: MessageListView.AttachmentDownloadClickListener by ListenerDelegate(
        listeners.attachmentDownloadClickListener
    ) { realListener ->
        MessageListView.AttachmentDownloadClickListener(realListener()::onAttachmentDownloadClick)
    }

    private val reactionViewClickListener: MessageListView.ReactionViewClickListener by ListenerDelegate(
        listeners.reactionViewClickListener
    ) { realListener ->
        MessageListView.ReactionViewClickListener(realListener()::onReactionViewClick)
    }

    private val userClickListener: MessageListView.UserClickListener by ListenerDelegate(
        listeners.userClickListener
    ) { realListener ->
        MessageListView.UserClickListener(realListener()::onUserClick)
    }

    private val giphySendListener: MessageListView.GiphySendListener by ListenerDelegate(
        listeners.giphySendListener
    ) { realListener ->
        MessageListView.GiphySendListener(realListener()::onGiphySend)
    }

    private val linkClickListener: MessageListView.LinkClickListener by ListenerDelegate(
        listeners.linkClickListener
    ) { realListener ->
        MessageListView.LinkClickListener(realListener()::onLinkClick)
    }

    private val newListeners = MessageListListenerContainerImpl(
        messageClickListener = messageClickListener,
        messageLongClickListener = messageLongClickListener,
        messageRetryListener = messageRetryListener,
        threadClickListener = threadClickListener,
        attachmentClickListener = attachmentClickListener,
        attachmentDownloadClickListener = attachmentDownloadClickListener,
        reactionViewClickListener = reactionViewClickListener,
        userClickListener = userClickListener,
        giphySendListener = giphySendListener,
        linkClickListener = linkClickListener,
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
            setupAttachment(data, newListeners)
        }

        setupUploads(data)
    }

    private fun setupAttachment(data: MessageListItem.MessageItem, listeners: MessageListListenerContainer) {
        with(binding.attachmentsContainer) {
            removeAllViews()
            addView(attachmentViewFactory.createAttachmentView(data, listeners, style, binding.root))
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
