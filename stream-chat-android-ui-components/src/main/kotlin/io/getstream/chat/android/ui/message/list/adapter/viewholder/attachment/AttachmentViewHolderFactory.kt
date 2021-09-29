package io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment

import android.view.ViewGroup
import io.getstream.chat.android.client.models.Attachment
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.message.list.adapter.attachments.AttachmentGroup

public interface AttachmentViewHolderFactory {

    public fun setUp(attachments: List<Attachment>)

    public fun attachmentViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): SimpleListAdapter.ViewHolder<AttachmentGroup>
}
