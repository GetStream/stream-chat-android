package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.adapters.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding

internal class MediaAttachmentsViewHolder(
    private val binding: StreamUiItemImageAttachmentBinding,
) : SimpleListAdapter.ViewHolder<List<Attachment>>(binding.root) {

    override fun bind(item: List<Attachment>) {
        binding.mediaAttachmentView.showAttachments(item)
    }
}
