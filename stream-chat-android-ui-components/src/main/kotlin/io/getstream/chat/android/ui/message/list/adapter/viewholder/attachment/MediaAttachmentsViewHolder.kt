package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import com.getstream.sdk.chat.adapter.MessageListItem
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.MessageListListenerContainer
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentClickListener
import io.getstream.chat.android.ui.message.list.adapter.view.internal.AttachmentLongClickListener

internal class MediaAttachmentsViewHolder(
    private val binding: StreamUiItemImageAttachmentBinding,
    container: MessageListListenerContainer?,
) : AttachmentViewHolder(binding.root) {

    private val mediaAttachmentGroupView = binding.mediaAttachmentGroupView
    private var message: Message? = null

    init {
        if (container != null) {
            setupListeners(container)
        }
    }

    private fun setupListeners(container: MessageListListenerContainer) {
        mediaAttachmentGroupView.attachmentClickListener = AttachmentClickListener { attachment ->
            message?.let { message ->
                container.attachmentClickListener.onAttachmentClick(message, attachment)
            }
        }
        mediaAttachmentGroupView.attachmentLongClickListener = AttachmentLongClickListener {
            message?.let { message ->
                container.messageLongClickListener.onMessageLongClick(message)
            }
        }
    }

    override fun bind(data: MessageListItem.MessageItem) {
        this.message = data.message
        mediaAttachmentGroupView.setupBackground(data)
        binding.mediaAttachmentGroupView.showAttachments(data.message.attachments)
    }
}
