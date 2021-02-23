package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.getstream.sdk.chat.ChatMarkdown
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
import io.getstream.chat.android.ui.common.extensions.internal.dpToPx
import io.getstream.chat.android.ui.common.extensions.internal.hasLink
import io.getstream.chat.android.ui.common.internal.AttachmentUtils
import io.getstream.chat.android.ui.common.internal.LongClickFriendlyLinkMovementMethod
import io.getstream.chat.android.ui.databinding.StreamUiItemMessagePlainTextWithFileAttachmentsBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListItemPayloadDiff
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.internal.DecoratedBaseMessageItemViewHolder
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.FileAttachmentsView
import io.getstream.chat.android.ui.message.list.adapter.view.internal.LinkAttachmentView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.AttachmentViewFactory
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class PlainTextWithFileAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    private val listeners: MessageListListenerContainer,
    private val markdown: ChatMarkdown,
    private val attachmentViewFactory: AttachmentViewFactory,
    internal val binding: StreamUiItemMessagePlainTextWithFileAttachmentsBinding =
        StreamUiItemMessagePlainTextWithFileAttachmentsBinding.inflate(
            parent.inflater,
            parent,
            false
        ),
) : DecoratedBaseMessageItemViewHolder<MessageListItem.MessageItem>(binding.root, decorators) {

    private var scope: CoroutineScope? = null

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

            LongClickFriendlyLinkMovementMethod.set(
                textView = messageText,
                longClickTarget = root,
                onLinkClicked = { url -> listeners.linkClickListener.onLinkClick(url) }
            )
        }
    }

    override fun bindData(data: MessageListItem.MessageItem, diff: MessageListItemPayloadDiff?) {
        super.bindData(data, diff)

        markdown.setText(binding.messageText, data.message.text)
        setupAttachments(data)

        val uploadIdList: List<String> = data.message
            .attachments
            .filter { attachment -> attachment.uploadState == Attachment.UploadState.InProgress }
            .mapNotNull { attachment -> attachment.uploadId }

        val needUpload = uploadIdList.isNotEmpty()

        if (needUpload) {
            clearScope()
            val scope = CoroutineScope(DispatcherProvider.Main)
            this.scope = scope

            scope.launch {
                AttachmentUtils.trackFilesSent(context, uploadIdList, binding.sentFiles)
            }
        } else {
            binding.sentFiles.isVisible = false
        }
    }

    private fun setupAttachments(data: MessageListItem.MessageItem) {
        binding.attachmentsContainer.removeAllViews()

        val attachments = data.message.attachments.filterNot(Attachment::hasLink)
        val fileAttachmentView = attachmentViewFactory.createAttachmentsView(attachments, context)
        binding.attachmentsContainer.addView(fileAttachmentView)
        (fileAttachmentView as? FileAttachmentsView)?.run {
            setPadding(FILE_ATTACHMENT_VIEW_PADDING)
            setAttachments(attachments)
            attachmentLongClickListener = AttachmentLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
            }
            attachmentClickListener = AttachmentClickListener {
                listeners.attachmentClickListener.onAttachmentClick(data.message, it)
            }
            attachmentDownloadClickListener = AttachmentDownloadClickListener {
                listeners.attachmentDownloadClickListener.onAttachmentDownloadClick(it)
            }
        }

        val linkAttachment = data.message.attachments.firstOrNull(Attachment::hasLink)
        if (linkAttachment != null) {
            val linkAttachmentView = attachmentViewFactory.createLinkAttachmentView(linkAttachment, context)
            binding.attachmentsContainer.addView(linkAttachmentView)
            (linkAttachmentView as? LinkAttachmentView)?.run {
                setPadding(LINK_VIEW_PADDING)
                showLinkAttachment(linkAttachment)
                setLinkPreviewClickListener { url ->
                    listeners.linkClickListener.onLinkClick(url)
                }
                setLongClickTarget(binding.root)
            }
        }
    }

    private fun clearScope() {
        scope?.cancel()
        scope = null
    }

    override fun unbind() {
        super.unbind()
        clearScope()
    }

    private companion object {
        private val FILE_ATTACHMENT_VIEW_PADDING = 4.dpToPx()
        private val LINK_VIEW_PADDING = 8.dpToPx()
    }
}
