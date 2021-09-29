package io.getstream.chat.android.ui.message.list.adapter.attachments

import android.view.ViewGroup
import io.getstream.chat.android.ui.common.extensions.internal.streamThemeInflater
import io.getstream.chat.android.ui.common.internal.SimpleListAdapter
import io.getstream.chat.android.ui.databinding.StreamUiItemImageAttachmentBinding
import io.getstream.chat.android.ui.message.list.adapter.view.internal.MediaAttachmentsGroupView
import io.getstream.chat.android.ui.message.list.adapter.viewholder.attachment.MediaAttachmentsViewHolder

internal class MediaAttachmentsAdapter : SimpleListAdapter<AttachmentGroup, MediaAttachmentsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaAttachmentsViewHolder {
        return StreamUiItemImageAttachmentBinding
            .inflate(parent.streamThemeInflater, parent, false)
            .let(::MediaAttachmentsViewHolder)
    }
}
