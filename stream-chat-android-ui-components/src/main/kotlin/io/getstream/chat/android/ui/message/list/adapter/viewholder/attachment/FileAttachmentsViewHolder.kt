package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.databinding.StreamUiItemFileAttachmentGroupBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentDownloadClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

internal class FileAttachmentsViewHolder(
    private val binding: StreamUiItemFileAttachmentGroupBinding,
    container: MessageListListenerContainer?,
) : AttachmentViewHolder(binding.root) {

    private val fileAttachmentsView = binding.fileAttachmentsView

    private var message: Message? = null

    init {
        if (container != null) {
            setupListeners(container)
        }
    }

    private fun setupListeners(container: MessageListListenerContainer) {
        fileAttachmentsView.attachmentLongClickListener = AttachmentLongClickListener {
            message?.let(container.messageLongClickListener::onMessageLongClick)
        }
        fileAttachmentsView.attachmentClickListener = AttachmentClickListener { attachment ->
            message?.let { message ->
                container.attachmentClickListener.onAttachmentClick(message, attachment)
            }
        }
        fileAttachmentsView.attachmentDownloadClickListener =
            AttachmentDownloadClickListener(container.attachmentDownloadClickListener::onAttachmentDownloadClick)
    }

    override fun bind(data: MessageListItem.MessageItem) {
        this.message = data.message
        binding.fileAttachmentsView.setAttachments(data.message.attachments)
    }
}
