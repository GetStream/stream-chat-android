package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.MediaAttachmentsViewHolder

//Use a different type of adapter
internal class MediaAttachmentsAdapter : SimpleListAdapter<AttachmentGroup, MediaAttachmentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaAttachmentsViewHolder {
        return MediaAttachmentsViewHolder(MediaAttachmentsGroupView(parent.context))
    }
}
