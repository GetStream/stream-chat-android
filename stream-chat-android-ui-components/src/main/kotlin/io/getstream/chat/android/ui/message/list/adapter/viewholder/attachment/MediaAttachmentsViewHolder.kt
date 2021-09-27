package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.message.list.adapter.attachments.AttachmentGroup
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView

internal class MediaAttachmentsViewHolder(
    private val view: MediaAttachmentsGroupView
): SimpleListAdapter.ViewHolder<AttachmentGroup>(view.rootView) {

    override fun bind(item: AttachmentGroup) {
        view.showAttachments(item.attachments)
    }
}
