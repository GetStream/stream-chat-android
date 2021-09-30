package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.attachments.AttachmentGroup

internal class MediaAttachmentsViewHolder(
    private val binding: StreamUiItemImageAttachmentBinding,
): SimpleListAdapter.ViewHolder<AttachmentGroup>(binding.root) {

    override fun bind(item: AttachmentGroup) {
        binding.mediaAttachmentView.showAttachments(item.attachments)
    }

    override fun unbind() {
        super.unbind()
    }
}
