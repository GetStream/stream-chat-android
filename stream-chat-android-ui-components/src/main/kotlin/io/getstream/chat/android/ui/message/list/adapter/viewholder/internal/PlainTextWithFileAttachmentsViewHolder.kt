package io.getstream.chat.android.ui.message.list.adapter.viewholder.internal

import android.view.ViewGroup
import androidx.core.view.isVisible
import com.getstream.sdk.chat.adapter.MessageListItem
import com.getstream.sdk.chat.utils.extensions.inflater
import io.getstream.chat.android.client.extensions.uploadId
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.core.internal.coroutines.DispatcherProvider
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
import io.getstream.chat.android.ui.message.list.adapter.viewholder.decorator.internal.Decorator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

internal class PlainTextWithFileAttachmentsViewHolder(
    parent: ViewGroup,
    decorators: List<Decorator>,
    listeners: MessageListListenerContainer,
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
            fileAttachmentsView.attachmentClickListener = AttachmentClickListener {
                listeners.attachmentClickListener.onAttachmentClick(data.message, it)
            }
            footnote.setOnThreadClickListener {
                listeners.threadClickListener.onThreadClick(data.message)
            }
            fileAttachmentsView.attachmentDownloadClickListener = AttachmentDownloadClickListener {
                listeners.attachmentDownloadClickListener.onAttachmentDownloadClick(it)
            }

            root.setOnLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
                true
            }
            fileAttachmentsView.attachmentLongClickListener = AttachmentLongClickListener {
                listeners.messageLongClickListener.onMessageLongClick(data.message)
            }
            linkAttachmentView.apply {
                setLinkPreviewClickListener { url ->
                    listeners.linkClickListener.onLinkClick(url)
                }
                setLongClickTarget(root)
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

        binding.messageText.text = data.message.text
        binding.fileAttachmentsView.setAttachments(
            data.message
                .attachments
                .filter { it.hasLink().not() }
        )

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

    private fun clearScope() {
        scope?.cancel()
        scope = null
    }

    override fun unbind() {
        super.unbind()
        clearScope()
    }
}
